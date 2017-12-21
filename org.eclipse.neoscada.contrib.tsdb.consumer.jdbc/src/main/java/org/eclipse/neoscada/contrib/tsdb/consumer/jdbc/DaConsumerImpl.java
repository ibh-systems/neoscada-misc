package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLTransientException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.neoscada.contrib.tsdb.api.DaConsumer;
import org.eclipse.neoscada.contrib.tsdb.api.DaProducer;
import org.eclipse.neoscada.contrib.tsdb.api.ValueChangeEvent;
import org.eclipse.scada.utils.osgi.jdbc.task.CommonConnectionTask;
import org.eclipse.scada.utils.osgi.jdbc.task.ConnectionContext;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 */
@Component ( configurationPid = "$", service = DaConsumer.class, immediate = true )
public class DaConsumerImpl implements DaConsumer
{
    private static final Logger logger = LoggerFactory.getLogger ( DaConsumerImpl.class );

    private static final Gson gson = new GsonBuilder ().create ();

    private ScheduledExecutorService scheduler;

    private DaProducer producer;

    private DataSourceFactory dataSourceFactory;

    private ReconnectionConnectionAccessor dsca;

    private PooledDataSourceFactory pooledDataSourceFactory;

    private PooledDataSourceFactoryAdapter adaptedDataSource;

    private Invocable invocable;

    private long lastValue = 0;

    @Activate
    void activate () throws Exception
    {
        logger.debug ( "activated!" );

        final String configFileLocation = System.getProperty ( "org.eclipse.neoscada.contrib.tsdb.config", "config.json" );
        final File configFile = new File ( configFileLocation );
        logger.debug ( "reading config file '{}'", configFile );
        final Configuration configuration = gson.fromJson ( new FileReader ( configFile ), Configuration.class );
        logger.debug ( "read configuration with properties: {}", configuration );

        ScriptEngine engine = // new ScriptEngineManager ().getEngineByName ( "nashorn" );
                new ScriptEngineManager ().getEngineByMimeType ( "text/javascript" );
        if ( configuration.getJavaScriptFile () != null )
        {
            engine.eval ( new FileReader ( new File ( configuration.getJavaScriptFile () ) ) );
        }
        else
        {
            engine.eval ( configuration.getJavaScript () );
        }
        invocable = (Invocable)engine;

        this.scheduler = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "DaConsumerImpl-%d" ).build () );
        setupDatabase ( configuration );
        setupQueue ( configuration );
    }

    private void setupDatabase ( Configuration configuration ) throws Exception
    {
        Properties databaseProperties = new Properties ();
        if ( configuration.getDatabaseConnectionUrl () != null )
        {
            databaseProperties.put ( DataSourceFactory.JDBC_URL, configuration.getDatabaseConnectionUrl () );
        }
        if ( configuration.getDatabaseName () != null )
        {
            databaseProperties.put ( DataSourceFactory.JDBC_DATABASE_NAME, configuration.getDatabaseName () );
        }

        if ( configuration.getDatabaseHost () != null )
        {
            databaseProperties.put ( DataSourceFactory.JDBC_SERVER_NAME, configuration.getDatabaseHost () );
        }
        if ( configuration.getDatabaseUser () != null )
        {
            databaseProperties.put ( DataSourceFactory.JDBC_USER, configuration.getDatabaseUser () );
        }
        if ( configuration.getDatabasePassword () != null )
        {
            databaseProperties.put ( DataSourceFactory.JDBC_PASSWORD, configuration.getDatabasePassword () );
        }
        if ( configuration.getDatabaseDriver () != null )
        {
            // databaseProperties.put ( DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
            // configuration.getDatabaseDriver () );
        }
        this.adaptedDataSource = new PooledDataSourceFactoryAdapter ( pooledDataSourceFactory, dataSourceFactory );
        dsca = new ReconnectionConnectionAccessor ( this.scheduler, this.adaptedDataSource, databaseProperties, 30 );
    }

    private void setupQueue ( final Configuration configuration )
    {
        this.scheduler.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                flushQueue ( configuration, producer.getQueue ().orElse ( new ArrayDeque<> ( 0 ) ) );
            }
        }, configuration.getFlushInterval (), configuration.getFlushInterval (), TimeUnit.SECONDS );
    }

    protected void flushQueue ( final Configuration configuration, Deque<ValueChangeEvent> queue )
    {
        int size = queue.size ();
        logger.trace ( "queue size = {}", size );
        List<ValueChangeEvent> elementsToStore = new ArrayList<> ( size );
        for ( int i = 0; i < size; i++ )
        {
            ValueChangeEvent vce = queue.pollLast ();
            if ( vce != null )
            {
                elementsToStore.add ( vce );
            }
        }
        storeToDatabase ( configuration, elementsToStore );
    }

    protected void storeToDatabase ( final Configuration configuration, final Collection<ValueChangeEvent> valueChangeEvents )
    {
        logger.trace ( "storeToDatabase (), {} events", valueChangeEvents.size () );
        if ( valueChangeEvents.size () == 0 )
        {
            // we don't try to do heavy database stuff if we don't have to store any events
            // at all
            return;
        }
        try
        {
            long timeStart = System.currentTimeMillis ();
            dsca.doWithConnection ( new CommonConnectionTask<Void> () {
                @Override
                protected Void performTask ( ConnectionContext connectionContext ) throws Exception
                {
                    for ( ValueChangeEvent vce : valueChangeEvents )
                    {
                        Long v = vce.getValue ().getValue ().asLong ( -1l );
                        if ( lastValue + 1 != v )
                        {
                            System.err.println ( "lastValue was " + lastValue );
                            System.err.println ( "nextValue was " + v );
                        }
                        lastValue = v;
                        final String sql = buildSql ( configuration, vce );
                        if ( sql == null )
                        {
                            logger.trace ( "skipping insert for {}", vce );
                            continue;
                        }
                        final List<Object> parameters = buildParameters ( configuration, vce );
                        logger.trace ( "storeToDatabase - sql: '{}' with parameters {}", sql, parameters );
                        connectionContext.update ( sql, parameters.toArray () );
                    }
                    return null;
                }
            } );
            long timeEnd = System.currentTimeMillis ();
            logger.trace ( "database operation took {}ms", ( timeEnd - timeStart ) );
        }
        catch ( Exception e )
        {
            logger.trace ( "could not store events: {}", valueChangeEvents );
            logger.warn ( "insert into database failed", e );
            if ( e instanceof SQLTransientException )
            {
                logger.warn ( "clean up old connection" );
                dsca.cleanUp ();
            }
        }
    }

    protected List<Object> buildParameters ( final Configuration configuration, ValueChangeEvent vce )
    {
        final List<Object> parameters = new ArrayList<> ( 10 );
        parameters.add ( new Timestamp ( vce.getValue ().getTimestamp ().getTimeInMillis () ) );
        if ( configuration.isStoreEntryTimestamp () )
        {
            parameters.add ( new Timestamp ( System.currentTimeMillis () ) );
        }
        if ( configuration.isStoreName () )
        {
            parameters.add ( vce.getId () );
        }
        switch ( vce.getValue ().getValue ().getType () )
        {
            case NULL:
                parameters.add ( null );
                break;
            case BOOLEAN:
                if ( configuration.isStoreBooleanAsInteger () )
                {
                    parameters.add ( vce.getValue ().getValue ().asInteger ( 0 ) );
                }
                else
                {
                    parameters.add ( vce.getValue ().getValue ().asBoolean () );
                }
                break;
            case INT32:
                parameters.add ( vce.getValue ().getValue ().asInteger ( 0 ) );
                break;
            case INT64:
                parameters.add ( vce.getValue ().getValue ().asLong ( 0l ) );
                break;
            case DOUBLE:
                parameters.add ( vce.getValue ().getValue ().asDouble ( 0.0 ) );
                break;
            default:
                parameters.add ( vce.getValue ().getValue ().asString ( "" ) );
        }
        if ( configuration.isStoreError () )
        {
            parameters.add ( vce.getValue ().isError () );
        }
        if ( configuration.isStoreAlarm () )
        {
            parameters.add ( vce.getValue ().isAlarm () );
        }
        if ( configuration.isStoreWarning () )
        {
            parameters.add ( vce.getValue ().isWarning () );
        }
        if ( configuration.isStoreManual () )
        {
            parameters.add ( vce.getValue ().isManual () );
        }
        if ( configuration.isStoreBlocked () )
        {
            parameters.add ( vce.getValue ().isBlocked () );
        }
        if ( configuration.isStoreHeartbeat () )
        {
            parameters.add ( vce.isHeartbeat () );
        }
        return parameters;
    }

    private String buildSql ( final Configuration configuration, final ValueChangeEvent vce )
    {
        final String table = toTableName ( vce.getId () );
        if ( table == null )
        {
            logger.trace ( "buildSql () - could not determine table name for item '{}'!", vce.getId () );
            return null;
        }
        if ( vce.getValue ().getTimestamp () == null )
        {
            logger.trace ( "buildSql () - item '{}' does not have a timestamp!", vce.getId () );
            return null;
        }
        StringBuilder sb = new StringBuilder ();
        sb.append ( "INSERT INTO " );
        sb.append ( table );
        sb.append ( " (ts" );
        if ( configuration.isStoreEntryTimestamp () )
        {
            sb.append ( ", tse" );
        }
        if ( configuration.isStoreName () )
        {
            sb.append ( ", tag" );
        }
        sb.append ( ", value" );
        if ( configuration.isStoreError () )
        {
            sb.append ( ", error" );
        }
        if ( configuration.isStoreAlarm () )
        {
            sb.append ( ", alarm" );
        }
        if ( configuration.isStoreWarning () )
        {
            sb.append ( ", warning" );
        }
        if ( configuration.isStoreManual () )
        {
            sb.append ( ", manual" );
        }
        if ( configuration.isStoreBlocked () )
        {
            sb.append ( ", blocked" );
        }
        if ( configuration.isStoreHeartbeat () )
        {
            sb.append ( ", heartbeat" );
        }
        sb.append ( ") VALUES (?" );
        if ( configuration.isStoreEntryTimestamp () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreName () )
        {
            sb.append ( ", ?" );
        }
        sb.append ( ", ?" );
        if ( configuration.isStoreError () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreAlarm () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreWarning () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreManual () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreBlocked () )
        {
            sb.append ( ", ?" );
        }
        if ( configuration.isStoreHeartbeat () )
        {
            sb.append ( ", ?" );
        }
        sb.append ( ");" );
        return sb.toString ();
    }

    private String toTableName ( String itemId )
    {
        Object result;
        try
        {
            result = invocable.invokeFunction ( "toTableName", itemId );
            return (String)result;
        }
        catch ( Exception e )
        {
            logger.error ( "failed to execute javascript function 'toTableName' ", e );
            return null;
        }
    }

    @Deactivate
    void deactivate () throws Exception
    {
        Optional.ofNullable ( this.dsca ).ifPresent ( dsca -> dsca.dispose () );
        Optional.ofNullable ( this.scheduler ).ifPresent ( scheduler -> scheduler.shutdown () );
    }

    @Override
    @Reference ( cardinality = ReferenceCardinality.MANDATORY )
    public void setProducer ( final DaProducer producer )
    {
        logger.trace ( "producer set" );
        this.producer = producer;
    }

    @Reference ( cardinality = ReferenceCardinality.MANDATORY )
    public void setDataSourceFactory ( final DataSourceFactory dataSourceFactory )
    {
        logger.trace ( "dataSourceFactory set" );
        this.dataSourceFactory = dataSourceFactory;
    }

    @Reference ( cardinality = ReferenceCardinality.MANDATORY, target = "(pool=hikari)" )
    public void setPooledDataSourceFactory ( PooledDataSourceFactory pooledDataSourceFactory )
    {
        logger.trace ( "pooledDataSourceFactory set" );
        this.pooledDataSourceFactory = pooledDataSourceFactory;
    }
}
