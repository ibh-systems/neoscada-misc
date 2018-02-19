package com.ibh.systems.neoscada.ngp2modbus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.scada.core.ConnectionInformation;
import org.eclipse.scada.core.client.AutoReconnectController;
import org.eclipse.scada.core.client.ConnectionFactory;
import org.eclipse.scada.da.client.Connection;
import org.eclipse.scada.da.client.DataItem;
import org.eclipse.scada.da.client.ItemManagerImpl;
import org.eclipse.scada.utils.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemAndConnectionManager
{
    private final static Logger logger = LoggerFactory.getLogger ( ItemAndConnectionManager.class );

    private Map<String, ItemManagerImpl> connections = new TreeMap<String, ItemManagerImpl> ();

    private Map<String, AutoReconnectController> controllers = new TreeMap<String, AutoReconnectController> ();

    private Map<String, DataItem> items = new TreeMap<String, DataItem> ();

    public DataItem addItem ( String itemUri ) throws URISyntaxException
    {
        if ( items.containsKey ( itemUri ) )
        {
            return items.get ( itemUri );
        }
        final Pair<String, String> connectionItem = parseItemUri ( itemUri );
        final ItemManagerImpl itemManager;
        if ( connections.containsKey ( connectionItem.first ) )
        {
            itemManager = connections.get ( connectionItem.first );
        }
        else
        {
            final ConnectionInformation ci = ConnectionInformation.fromURI ( connectionItem.first );
            final Connection connection = (Connection)ConnectionFactory.create ( ci );
            itemManager = new ItemManagerImpl ( connection );
            connections.put ( connectionItem.first, itemManager );
        }
        DataItem dataItem = new DataItem ( connectionItem.second, itemManager );
        items.put ( itemUri, dataItem );
        return dataItem;
    }

    public DataItem removeItem ( String itemUri ) throws URISyntaxException
    {
        if ( getItem ( itemUri ) != null )
        {
            return null;
        }
        // final Pair<String, String> connectionItem = parseItemUri ( itemUri );
        DataItem dataItem = items.remove ( itemUri );
        dataItem.unregister ();
        // ItemManagerImpl itemManager = connections.get ( connectionItem.first );
        // TODO: dispose old item managers
        return dataItem;
    }

    public DataItem getItem ( String itemUri )
    {
        if ( itemUri == null )
        {
            return null;
        }
        return items.get ( itemUri );
    }

    public synchronized void start ()
    {
        logger.debug ( "starting connections ..." );
        for ( Entry<String, ItemManagerImpl> entry : connections.entrySet () )
        {
            AutoReconnectController controller = new AutoReconnectController ( entry.getValue ().getConnection () );
            controllers.put ( entry.getKey (), controller );
            controller.connect ();
        }
    }

    public synchronized void dispose ()
    {
        logger.debug ( "disposing auto reconnect controllers ..." );
        // first, close connections
        for ( Entry<String, AutoReconnectController> entry : controllers.entrySet () )
        {
            entry.getValue ().dispose ( true );
        }
        controllers.clear ();
        logger.debug ( "remove items ..." );
        // unregister and remove items
        for ( Entry<String, DataItem> entry : items.entrySet () )
        {
            entry.getValue ().deleteObservers ();
            entry.getValue ().unregister ();
        }
        items.clear ();
        logger.debug ( "remove item managers ..." );
        // and now also remove item & item manager references
        connections.clear ();
    }

    private Pair<String, String> parseItemUri ( String uri ) throws URISyntaxException
    {
        URI itemUri = new URI ( uri );
        final String connectionUri = itemUri.getScheme () + ":" + itemUri.getSchemeSpecificPart ();
        String itemId = itemUri.getFragment ();
        return new Pair<String, String> ( connectionUri, itemId );
    }

    public Connection getConnection ( String itemUri ) throws URISyntaxException
    {
        Pair<String, String> connectionItem = parseItemUri ( itemUri );
        ItemManagerImpl itemManager = connections.get ( connectionItem.first );
        if ( itemManager != null )
        {
            return itemManager.getConnection ();
        }
        return null;
    }
}
