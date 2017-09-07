package org.eclipse.neoscada.contrib.kafka;

import java.io.File;
import java.io.FileReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.scada.core.ConnectionInformation;
import org.eclipse.scada.core.Variant;
import org.eclipse.scada.core.client.AutoReconnectController;
import org.eclipse.scada.core.client.ConnectionFactory;
import org.eclipse.scada.core.client.ConnectionState;
import org.eclipse.scada.core.client.ConnectionStateListener;
import org.eclipse.scada.da.client.BrowseOperationCallback;
import org.eclipse.scada.da.client.Connection;
import org.eclipse.scada.da.client.DataItem;
import org.eclipse.scada.da.client.DataItemValue;
import org.eclipse.scada.da.client.ItemManagerImpl;
import org.eclipse.scada.da.core.Location;
import org.eclipse.scada.da.core.browser.DataItemEntry;
import org.eclipse.scada.da.core.browser.Entry;
import org.eclipse.scada.da.core.browser.FolderEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KafkaIngester implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger ( KafkaIngester.class );

    private static final Gson gson = new GsonBuilder ().serializeNulls ().serializeSpecialFloatingPointValues ().registerTypeAdapter(Variant.class, new VariantSerializer()).create ();

    private final Configuration configuration;

    private final ScheduledExecutorService executorService;

    private Producer<String, String> producer;

    private ItemManagerImpl itemManager;

    private Connection connection;

    private Invocable invocable;

    private ConcurrentMap<String, ItemEntry> subscribedItems = new ConcurrentSkipListMap<> ();

    private final Properties kafkaProperties = new Properties ();

    public KafkaIngester ( Configuration configuration )
    {
        kafkaProperties.put ( "bootstrap.servers", configuration.getZookeeperUrl () );
        kafkaProperties.put ( "acks", "all" );
        kafkaProperties.put ( "retries", 0 );
        kafkaProperties.put ( "batch.size", 16384 );
        kafkaProperties.put ( "linger.ms", 1 );
        kafkaProperties.put ( "buffer.memory", 33554432 );
        kafkaProperties.put ( "key.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        kafkaProperties.put ( "value.serializer", "org.apache.kafka.common.serialization.StringSerializer" );

        this.configuration = configuration;
        this.executorService = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( KafkaIngester.class.getName () + "-%d" ).build () );
        logger.trace ( "constructor finished" );
    }

    private void walkFolders ( final Set<String> items, final Location root, final AtomicInteger browseTasks )
    {
        browseTasks.incrementAndGet ();
        connection.browse ( root, new BrowseOperationCallback () {
            @Override
            public void failed ( String error )
            {
                logger.warn ( "failed to browse '{}' with error: {} ", root, error );
                browseTasks.decrementAndGet ();
            }

            @Override
            public void error ( Throwable e )
            {
                logger.warn ( "failed to browse '{}' with exception", root, e );
                browseTasks.decrementAndGet ();
            }

            @Override
            public void complete ( Entry[] entries )
            {
                for ( final Entry entry : entries )
                {
                    if ( entry instanceof DataItemEntry )
                    {
                        DataItemEntry dataItemEntry = (DataItemEntry)entry;
                        if ( includeItem ( dataItemEntry.getId () ) )
                        {
                            items.add ( dataItemEntry.getId () );
                        }
                    }
                    if ( entry instanceof FolderEntry )
                    {
                        Location location = new Location ( root, entry.getName () );
                        walkFolders ( items, location, browseTasks );
                    }
                }
                browseTasks.decrementAndGet ();
            }
        } );
    }

    protected void reconfigure ()
    {
        Set<String> items = new ConcurrentSkipListSet<> ();
        Set<String> itemsToAdd = new ConcurrentSkipListSet<> ();
        Set<String> itemsToRemove = new ConcurrentSkipListSet<> ();
        // unfortunately, we don't have a future as a return value for browse,
        // so we have to determine when we are finished with browsing
        // differently
        final CountDownLatch waitForFolderBrowse = new CountDownLatch ( 1 );
        final AtomicInteger browseTasks = new AtomicInteger ( 0 );
        walkFolders ( items, Location.ROOT, browseTasks );
        connection.getExecutor ().scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                if ( browseTasks.get () == 0 )
                {
                    waitForFolderBrowse.countDown ();
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS );
        try
        {
            waitForFolderBrowse.await ();
        }
        catch ( InterruptedException e )
        {
            logger.warn ( "interuppted while browsing {} ", Location.ROOT, e );
        }
        // now we have all items collected
        itemsToAdd.addAll ( Sets.difference ( items, subscribedItems.keySet () ) );
        itemsToRemove.addAll ( Sets.difference ( subscribedItems.keySet (), items ) );
        // first remove
        for ( String item : itemsToRemove )
        {
            ItemEntry itemEntry = subscribedItems.remove ( item );
            itemEntry.getDataItem ().deleteObservers ();
            itemEntry.getDataItem ().unregister ();
        }
        for ( final String item : itemsToAdd )
        {
            // do not subscribe to items we don't want to include anyway
            boolean include = includeItem ( item );
            if ( !include )
            {
                continue;
            }
            
            final DataItem dataItem = new DataItem ( item, itemManager );
            final ItemEntry itemEntry = new ItemEntry ( item, dataItem );
            dataItem.addObserver ( new Observer () {
                @Override
                public void update ( final Observable observable, final Object update )
                {
                    final DataItemValue div = (DataItemValue)update;
                    handleValueUpdate ( item, div, false );
                }
            } );
            subscribedItems.put ( item, itemEntry );
        }
    }

    protected void handleValueUpdate ( String itemId, DataItemValue value, boolean heartbeat )
    {
        if ( !includeItem ( itemId ) )
        {
            return;
        }
        final String topic = toTopicName ( itemId );
        final String modifiedItemId = toKafkaName ( itemId );
        producer.send ( new ProducerRecord<String, String> ( topic, gson.toJson ( new ValueChangeEvent ( modifiedItemId, value, heartbeat ) ) ) );
    }

    protected void storeHeartbeats ()
    {
        logger.debug ( "storeHeartbeats ()" );
        try
        {
            for ( ItemEntry itemEntry : subscribedItems.values () )
            {
                handleValueUpdate ( itemEntry.getId (), itemEntry.getDataItem ().getSnapshotValue (), true );
            }
        }
        catch ( Exception e )
        {
            logger.error ( "aquiring of dbconnection failed", e );
        }
    }

    private boolean includeItem ( String id )
    {
        Object result;
        try
        {
            result = invocable.invokeFunction ( "filterTag", id );
            return (Boolean)result;
        }
        catch ( Exception e )
        {
            logger.error ( "failed to execute javascript function 'filterTag' ", e );
            return false;
        }
    }

    private String toKafkaName ( String itemId )
    {
        Object result;
        try
        {
            result = invocable.invokeFunction ( "toName", itemId );
            return (String)result;
        }
        catch ( Exception e )
        {
            logger.error ( "failed to execute javascript function 'toName' ", e );
            return "ERROR_on_" + itemId;
        }
    }

    private String toTopicName ( String itemId )
    {
        Object result;
        try
        {
            result = invocable.invokeFunction ( "toTopic", itemId );
            return (String)result;
        }
        catch ( Exception e )
        {
            logger.error ( "failed to execute javascript function 'toTopic' ", e );
            return null;
        }
    }

    public void start ()
    {
        logger.info ( "started KafkaIngesterService" );
        this.executorService.submit ( this );
    }

    public void stop ()
    {
        if ( producer != null )
        {
            producer.close ();
        }
        logger.info ( "stopped KafkaIngesterService" );
    }

    @Override
    public void run ()
    {
        try
        {
            this.producer = new KafkaProducer<> ( kafkaProperties );

            try
            {
                Class.forName ( "org.eclipse.scada.da.client.ngp.ConnectionImpl" );
            }
            catch ( ClassNotFoundException e )
            {
                logger.error ( "ConnectionImpl not found", e );
                System.exit ( 1 );
            }
            final ConnectionInformation ci = ConnectionInformation.fromURI ( configuration.getNgpUrl () );
            try
            {
                connection = (Connection)ConnectionFactory.create ( ci );
            }
            catch ( Throwable e )
            {
                logger.error ( "failed to create connection uri", e );
            }
            if ( connection == null )
            {
                System.err.println ( "Unable to find a connection driver for specified URI" );
                System.exit ( 1 );
            }

            final CountDownLatch latch = new CountDownLatch ( 1 );
            connection.addConnectionStateListener ( new ConnectionStateListener () {
                @Override
                public void stateChange ( org.eclipse.scada.core.client.Connection connection, ConnectionState state, Throwable error )
                {
                    logger.info ( "Connection state is now: " + state );
                    if ( state == ConnectionState.BOUND )
                    {
                        executorService.submit ( new Runnable () {
                            @Override
                            public void run ()
                            {
                                try
                                {
                                    latch.await ();
                                    reconfigure ();
                                }
                                catch ( InterruptedException e )
                                {
                                    logger.error ( "failed to execute initial reconfigure ", e );
                                }
                            }
                        } );
                    }
                }
            } );

            final AutoReconnectController controller = new AutoReconnectController ( connection );
            controller.connect ();
            itemManager = new ItemManagerImpl ( connection );
            ScriptEngine engine = //new ScriptEngineManager ().getEngineByName ( "nashorn" );
                    new ScriptEngineManager ().getEngineByMimeType ( "text/javascript" );
            if ( configuration.getJavaScriptFile () != null && ( !configuration.getJavaScriptFile ().isEmpty () ) )
            {
                engine.eval ( new FileReader ( new File ( configuration.getJavaScriptFile () ) ) );
            }
            else
            {
                engine.eval ( configuration.getJavaScript () );
            }
            invocable = (Invocable)engine;

            latch.countDown ();

            executorService.scheduleAtFixedRate ( new Runnable () {
                @Override
                public void run ()
                {
                    reconfigure ();
                }
            }, configuration.getCheckInterval (), configuration.getCheckInterval (), TimeUnit.SECONDS );

            executorService.scheduleAtFixedRate ( new Runnable () {
                @Override
                public void run ()
                {
                    storeHeartbeats ();
                }
            }, configuration.getHeartBeat (), configuration.getHeartBeat (), TimeUnit.SECONDS );
        }
        catch ( Throwable th )
        {
            logger.error ( "something bad happened", th );
        }
    }
}
