package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.sql.SQLTransientConnectionException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.scada.utils.osgi.jdbc.ConnectionAccessor;
import org.eclipse.scada.utils.osgi.jdbc.DataSourceConnectionAccessor;
import org.eclipse.scada.utils.osgi.jdbc.task.ConnectionTask;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectionConnectionAccessor implements ConnectionAccessor, Runnable
{
    private static final Logger logger = LoggerFactory.getLogger ( ReconnectionConnectionAccessor.class );

    private DataSourceConnectionAccessor connectionAccessor;

    private final PooledDataSourceFactory pooledDataSourceFactory;

    private final DataSourceFactory dataSourceFactory;

    private final Properties databaseProperties;

    public ReconnectionConnectionAccessor ( final ScheduledExecutorService scheduler, final PooledDataSourceFactory pooledDataSourceFactory, final DataSourceFactory dataSourceFactory, final Properties databaseProperties, final long checkInterval )
    {
        this.pooledDataSourceFactory = pooledDataSourceFactory;
        this.dataSourceFactory = dataSourceFactory;
        this.databaseProperties = databaseProperties;
        scheduler.scheduleWithFixedDelay ( this, 0, checkInterval, TimeUnit.SECONDS );
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
        Optional.ofNullable ( this.connectionAccessor ) //
                .ifPresent ( c -> c.dispose () );
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
                this.connectionAccessor = new DataSourceConnectionAccessor ( new PooledDataSourceFactoryAdapter ( this.pooledDataSourceFactory, this.dataSourceFactory ), this.databaseProperties );
                logger.trace ( "checkConnection () - connectionAccessor successfully created" );
            }
            logger.trace ( "checkConnection () - check validity" );
            if ( !this.connectionAccessor.getConnection ().isValid ( 1 ) )
            {
                logger.trace ( "checkConnection () - validity check failed!" );
                throw new SQLTransientConnectionException ( "connection not available" );
            }
        }
        catch ( Exception e )
        {
            logger.trace ( "checkConnection () - failed", e );
            this.connectionAccessor = null;
        }
    }
}
