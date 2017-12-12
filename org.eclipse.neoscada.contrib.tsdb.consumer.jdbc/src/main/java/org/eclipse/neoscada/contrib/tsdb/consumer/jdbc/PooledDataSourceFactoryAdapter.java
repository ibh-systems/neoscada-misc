package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * this exists solely because DataSourceConnectionAccessor doesn't support a
 * DataSource itself, and it will only call createDataSource, so it is enough to
 * implement this
 */
public class PooledDataSourceFactoryAdapter implements DataSourceFactory
{
    private final PooledDataSourceFactory pooledDataSourceFactory;

    private final DataSourceFactory dataSourceFactory;

    private DataSource cachedDataSource;

    public PooledDataSourceFactoryAdapter ( PooledDataSourceFactory pooledDataSourceFactory, DataSourceFactory dataSourceFactory )
    {
        this.pooledDataSourceFactory = pooledDataSourceFactory;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public DataSource createDataSource ( Properties props ) throws SQLException
    {
        if ( cachedDataSource != null )
        {
            return cachedDataSource;
        }
        try
        {
            cachedDataSource = pooledDataSourceFactory.create ( dataSourceFactory, props );
            return cachedDataSource;
        }
        catch ( Exception e )
        {
            throw e;
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource ( Properties props ) throws SQLException
    {
        throw new UnsupportedOperationException ( "createConnectionPoolDataSource not implemented" );
    }

    @Override
    public XADataSource createXADataSource ( Properties props ) throws SQLException
    {
        throw new UnsupportedOperationException ( "createXADataSource not implemented" );
    }

    @Override
    public Driver createDriver ( Properties props ) throws SQLException
    {
        throw new UnsupportedOperationException ( "createDriver not implemented" );
    }
}
