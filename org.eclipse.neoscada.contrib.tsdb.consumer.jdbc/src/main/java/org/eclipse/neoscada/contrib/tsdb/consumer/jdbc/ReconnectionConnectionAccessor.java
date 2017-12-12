package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.sql.SQLTransientConnectionException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.scada.utils.osgi.jdbc.ConnectionAccessor;
import org.eclipse.scada.utils.osgi.jdbc.DataSourceConnectionAccessor;
import org.eclipse.scada.utils.osgi.jdbc.task.ConnectionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectionConnectionAccessor implements ConnectionAccessor, Runnable
{
    private static final Logger logger = LoggerFactory.getLogger ( ReconnectionConnectionAccessor.class );

    private DataSourceConnectionAccessor connectionAccessor;

    private PooledDataSourceFactoryAdapter adaptedDataSource;

    private final Properties databaseProperties;

    private ScheduledFuture<?> checkFuture;

    public ReconnectionConnectionAccessor ( final ScheduledExecutorService scheduler, final PooledDataSourceFactoryAdapter adaptedDataSource, final Properties databaseProperties, final long checkInterval )
    {
        this.adaptedDataSource = adaptedDataSource;
        this.databaseProperties = databaseProperties;
        checkFuture = scheduler.scheduleWithFixedDelay ( this, 0, checkInterval, TimeUnit.SECONDS );
    }

    @Override
    public <R> R doWithConnection ( ConnectionTask<R> connectionTask ) throws Exception
    {
        return Optional.ofNullable ( this.connectionAccessor ) //
                .orElseThrow ( () -> new SQLTransientConnectionException ( "connection not available" ) ) //
                .doWithConnection ( connectionTask );
    }

    @Override
    public void dispose ()
    {
        cleanUp ();
        Optional.ofNullable ( this.checkFuture ) //
                .ifPresent ( c -> c.cancel ( true ) );
    }

    @Override
    public void run ()
    {
        checkConnection ();
    }

    private void checkConnection ()
    {
        logger.trace ( "checkConnection ()" );
        try
        {
            if ( this.connectionAccessor == null )
            {
                logger.trace ( "checkConnection () - connectionAccessor is null" );
                this.connectionAccessor = new DataSourceConnectionAccessor ( this.adaptedDataSource, this.databaseProperties );
                logger.trace ( "checkConnection () - connectionAccessor successfully created" );
            }
        }
        catch ( Exception e )
        {
            logger.trace ( "checkConnection () - failed", e );
            this.connectionAccessor = null;
        }
    }

    public void cleanUp ()
    {
        Optional.ofNullable ( this.connectionAccessor ) //
                .ifPresent ( c -> c.dispose () );
        this.connectionAccessor = null;
    }
}
