/*******************************************************************************
 * Copyright (c) 2017 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.neoscada.contrib.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scada.core.Variant;
import org.eclipse.scada.core.data.SubscriptionState;
import org.eclipse.scada.da.client.DataItemValue;
import org.openscada.protocol.iec60870.ProtocolOptions;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.asdu.types.QualityInformation;
import org.openscada.protocol.iec60870.asdu.types.Value;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.openscada.protocol.iec60870.client.AutoConnectClient.ModulesFactory;
import org.openscada.protocol.iec60870.client.AutoConnectClient.StateListener;
import org.openscada.protocol.iec60870.client.ClientModule;
import org.openscada.protocol.iec60870.client.data.DataListener;
import org.openscada.protocol.iec60870.client.data.DataModule;
import org.openscada.protocol.iec60870.client.data.DataModuleOptions;
import org.openscada.protocol.iec60870.client.data.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec104AliveCheck
{
    private final static Logger logger = LoggerFactory.getLogger ( Iec104AliveCheck.class );

    private int port;

    private String name;

    private List<ScadaItem> items = new ArrayList<> ();

    private Map<String, ScadaItem> itemsByName = new ConcurrentHashMap<> ();

    private AtomicReference<AutoConnectClient.State> connectionStateRef = new AtomicReference<AutoConnectClient.State> ( AutoConnectClient.State.DISCONNECTED );
    
    @SuppressWarnings ( "unused" )
    private AutoConnectClient iecClient; 

    public AutoConnectClient.State getConnectionState ()
    {
        return connectionStateRef.get ();
    }

    public int getPort ()
    {
        return port;
    }

    public void setPort ( int port )
    {
        this.port = port;
    }

    public String getName ()
    {
        return name;
    }

    public void setName ( String name )
    {
        this.name = name;
    }

    public List<ScadaItem> getItems ()
    {
        return items;
    }

    public void setItems ( List<ScadaItem> items )
    {
        this.items = items;
    }

    public void initialize ( final String hostName, ScheduledExecutorService scheduler )
    {
        for ( ScadaItem item : items )
        {
            itemsByName.put ( item.getTag (), item );
        }
        ProtocolOptions options = new ProtocolOptions.Builder ().build ();
        final DataProcessor dataProcessor = new DataProcessor ( scheduler, new DataListener () {
            @Override
            public void update ( ASDUAddress commonAddress, InformationObjectAddress objectAddress, Value<?> value )
            {
                String addr = formatAddress ( commonAddress, objectAddress );
                ScadaItem item = itemsByName.get ( addr );
                if ( item != null )
                {
                    QualityInformation q = value.getQualityInformation ();
                    long ts = value.getTimestamp ();
                    Variant v = Variant.valueOf ( value.getValue () );
                    Map<String, Variant> attr = new HashMap<> ();
                    attr.put ( "timestamp", Variant.valueOf ( ts ) );
                    if ( !q.isValid () )
                    {
                        attr.put ( "error", Variant.TRUE );
                    }
                    if ( q.isSubstituted () )
                    {
                        attr.put ( "manual", Variant.TRUE );
                    }
                    DataItemValue div = new DataItemValue ( v, attr, SubscriptionState.CONNECTED );
                    item.setDaItemValue ( div );
                }

            }

            @Override
            public void started ()
            {
                logger.info ( "GA sent ..." );
            }

            @Override
            public void disconnected ()
            {
                logger.info ( "disconnected ...." );
                markAsInvalid ();
            }
        } );
        // use standard options, cause doesn't matter, we don't write
        final DataModuleOptions dataModuleOptions = new DataModuleOptions.Builder ().build ();
        ModulesFactory modulesFactory = new ModulesFactory () {
            @Override
            public List<ClientModule> createModules ()
            {
                List<ClientModule> result = new ArrayList<> ();
                result.add ( new DataModule ( dataProcessor, dataModuleOptions ) );
                return result;
            }
        };
        iecClient = new AutoConnectClient ( hostName, port, options, modulesFactory, new StateListener () {
            @Override
            public void stateChanged ( AutoConnectClient.State state, Throwable e )
            {
                connectionStateRef.set ( state );
            }
        } );
        scheduler.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                for ( final ScadaItem item : items )
                {
                    item.evaluateToggleState ();
                }
            }
        }, 1, 1, TimeUnit.SECONDS );
    }

    protected void markAsInvalid ()
    {
        for ( ScadaItem item : items )
        {
            item.setDaItemValue ( DataItemValue.DISCONNECTED );
        }
    }

    protected String formatAddress ( ASDUAddress commonAddress, InformationObjectAddress objectAddress )
    {
        return String.format ( "%d-%d", commonAddress.toArray ()[0], commonAddress.toArray ()[1] ) + "-" + String.format ( "%d-%d-%s", objectAddress.toArray ()[0], objectAddress.toArray ()[1], objectAddress.toArray ()[2] );
    }

    private boolean isValueWarning ()
    {
        boolean v = false;
        for ( ScadaItem item : items )
        {
            v |= item.isHError ();
            v |= item.isLError ();
            v |= item.isTimestampError ();
            v |= item.isToggleError ();
        }
        return v;
    }

    private boolean isValueCritical ()
    {
        boolean v = false;
        for ( ScadaItem item : items )
        {
            v |= item.isValueError ();
            v |= item.isHhError ();
            v |= item.isLlError ();
        }
        return v;
    }

    public boolean isWarning ()
    {
        return isDisconnected () || isValueWarning ();
    }

    public boolean isCritical ()
    {
        return isDisconnected () || isValueCritical ();
    }

    public String toStatus ()
    {
        if ( isCritical () )
        {
            return "CRITICAL";
        }
        else if ( isWarning () )
        {
            return "WARNING";
        }
        return "OK";
    }

    public boolean isDisconnected ()
    {
        return connectionStateRef.get () != AutoConnectClient.State.CONNECTED;
    }
}
