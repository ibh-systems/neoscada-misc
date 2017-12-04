package org.eclipse.neoscada.contrib.tsdb.producer;

import java.io.File;
import java.io.FileReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.neoscada.contrib.tsdb.api.ValueChangeEvent;
import org.eclipse.scada.core.ConnectionInformation;
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
import org.osgi.util.pushstream.SimplePushEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ConfiguredConnection implements Comparable<ConfiguredConnection>
{
    private static final Logger logger = LoggerFactory.getLogger ( ConfiguredConnection.class );

    private ConnectionConfiguration configuration;

    private AutoReconnectController autoReconnectController;

    private Connection connection;

    private Throwable lastError;

    private ScheduledExecutorService executorService;

    private ItemManagerImpl itemManager;

    private Invocable invocable;

    private ConcurrentMap<String, ItemEntry> subscribedItems = new ConcurrentSkipListMap<> ();

    private SimplePushEventSource<ValueChangeEvent> pushEventSource;

    public ConfiguredConnection ( ConnectionConfiguration configuration, SimplePushEventSource<ValueChangeEvent> pushEventSource ) throws Exception
    {
        this.configuration = configuration;
        this.pushEventSource = pushEventSource;
        this.executorService = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( ConfiguredConnection.class.getName () + "-%d" ).build () );

        try
        {
            Class.forName ( "org.eclipse.scada.da.client.ngp.ConnectionImpl" );
        }
        catch ( ClassNotFoundException e )
        {
            logger.error ( "ConnectionImpl not found", e );
            throw new RuntimeException ( e );
        }
        final ConnectionInformation ci = ConnectionInformation.fromURI ( configuration.getNgpUrl () );
        try
        {
            this.connection = (Connection)ConnectionFactory.create ( ci );
        }
        catch ( Throwable e )
        {
            logger.error ( "failed to create connection uri", e );
            throw new RuntimeException ( e );
        }
        if ( this.connection == null )
        {
            throw new RuntimeException ( "Unable to find a connection driver for specified URI" );
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
                                rebrowse ();
                            }
                            catch ( InterruptedException e )
                            {
                                logger.error ( "failed to execute initial reconfigure ", e );
                            }
                        }
                    } );
                }
                ConfiguredConnection.this.lastError = error;
            }
        } );

        autoReconnectController = new AutoReconnectController ( connection );
        autoReconnectController.connect ();
        itemManager = new ItemManagerImpl ( connection );
        ScriptEngine engine = //new ScriptEngineManager ().getEngineByName ( "nashorn" );
                new ScriptEngineManager ().getEngineByMimeType ( "text/javascript" );
        if ( configuration.getJavaScriptFile () != null && ( !configuration.getJavaScriptFile ().isEmpty () ) )
        {
            logger.trace ( "for connection {} read JavaScript file: '{}'", configuration.getNgpUrl (), configuration.getJavaScriptFile () );
            engine.eval ( new FileReader ( new File ( configuration.getJavaScriptFile () ) ) );
        }
        else
        {
            logger.trace ( "for connection {} use supplied JavaScript: '{}'", configuration.getNgpUrl (), configuration.getJavaScript () );
            engine.eval ( configuration.getJavaScript () );
        }
        invocable = (Invocable)engine;

        latch.countDown ();

        executorService.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                rebrowse ();
            }
        }, configuration.getCheckInterval (), configuration.getCheckInterval (), TimeUnit.SECONDS );
    }

    protected void rebrowse ()
    {
        logger.trace ( "rebrowse ()" );
        Set<String> items = new ConcurrentSkipListSet<> ();
        Set<String> itemsToAdd = new ConcurrentSkipListSet<> ();
        Set<String> itemsToRemove = new ConcurrentSkipListSet<> ();

        // unfortunately, we don't have a future as a return value for browse,
        // so we have to determine when we are finished with browsing
        // differently
        final CountDownLatch waitForFolderBrowse = new CountDownLatch ( 1 );
        final AtomicInteger browseTasks = new AtomicInteger ( 0 );

        walkFolders ( items, Location.ROOT, browseTasks );

        ScheduledFuture<?> browseFuture = connection.getExecutor ().scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                final int remaining = browseTasks.get ();
                logger.trace ( "rebrowse () - scheduled check ({} tasks remaining)", remaining );
                if ( remaining == 0 )
                {
                    waitForFolderBrowse.countDown ();
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS );
        try
        {
            waitForFolderBrowse.await ();
            logger.trace ( "rebrowse () - browsing finished ..." );
        }
        catch ( InterruptedException e )
        {
            logger.warn ( "interrupted while browsing {} ", Location.ROOT, e );
        }
        Optional.ofNullable ( browseFuture ).ifPresent ( c -> c.cancel ( true ) );

        // now we have all items collected
        itemsToAdd.addAll ( Sets.difference ( items, subscribedItems.keySet () ) );
        logger.trace ( "rebrowse () - items to add: {}", itemsToAdd );
        itemsToRemove.addAll ( Sets.difference ( subscribedItems.keySet (), items ) );
        logger.trace ( "rebrowse () - items to remove: {}", itemsToRemove );

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
            // although that check should be redundant, since it is checked already in walkFolders
            boolean include = includeItem ( item );
            if ( !include )
            {
                logger.trace ( "rebrowse () - skip subscription to item: {}", item );
                continue;
            }
            logger.trace ( "rebrowse () - subscribe to item: {}", item );

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

    private void walkFolders ( final Set<String> items, final Location root, final AtomicInteger browseTasks )
    {
        logger.trace ( "walkFolders () starting at {}", root );
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

    public void triggerHeartBeats ()
    {
        logger.debug ( "triggerHeartBeats ()" );
        executorService.submit ( new Runnable () {
            @Override
            public void run ()
            {
                storeHeartbeats ();
            }
        } );
    }

    protected void handleValueUpdate ( String itemId, DataItemValue value, boolean heartbeat )
    {
        logger.trace ( "handleValueUpdate () - {} = {} (hb: {}) ", new Object[] { itemId, value, heartbeat } );
        if ( !includeItem ( itemId ) )
        {
            return;
        }
        final String modifiedItemId = toFinalName ( itemId );
        pushEventSource.publish ( new ValueChangeEvent ( modifiedItemId, value, heartbeat ) );
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
            logger.error ( "publishing value update to queue failed", e );
        }
    }

    /**
     * TODO: memoize result
     */
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

    /**
     * TODO: memoize result
     */
    private String toFinalName ( String itemId )
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

    public ConnectionConfiguration getConfiguration ()
    {
        return this.configuration;
    }

    public void dispose ()
    {
        this.executorService.shutdown ();
        this.autoReconnectController.dispose ();
        this.connection.dispose ();
    }

    @Override
    public String toString ()
    {
        return String.format ( "%s %s (%s)", this.configuration.getNgpUrl (), this.connection.getState (), Optional.ofNullable ( lastError ).orElse ( new RuntimeException ( "no error" ) ).getMessage () );
    }

    @Override
    public int compareTo ( ConfiguredConnection o )
    {
        return this.configuration.getNgpUrl ().compareTo ( o.configuration.getNgpUrl () );
    }
}
