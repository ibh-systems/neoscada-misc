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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.scada.core.ConnectionInformation;
import org.eclipse.scada.core.client.AutoReconnectController;
import org.eclipse.scada.core.client.ConnectionFactory;
import org.eclipse.scada.core.client.ConnectionState;
import org.eclipse.scada.core.client.ConnectionStateListener;
import org.eclipse.scada.da.client.Connection;
import org.eclipse.scada.da.client.DataItem;
import org.eclipse.scada.da.client.DataItemValue;
import org.eclipse.scada.da.client.ItemManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

public class ScadaAliveCheck
{
    private final static Logger logger = LoggerFactory.getLogger ( ScadaAliveCheck.class );

    private String name;

    private String daUrl;

    private Integer jmxPort;

    private AtomicReference<JMXConnector> jmxcRef = new AtomicReference<JMXConnector> ( null );

    private AtomicReference<JvmMemoryUsage> memoryRef = new AtomicReference<JvmMemoryUsage> ( new JvmMemoryUsage () );

    private AtomicReference<ThreadInformation> threadRef = new AtomicReference<ThreadInformation> ( new ThreadInformation () );

    private AtomicDouble loadAverageRef = new AtomicDouble ( 0.0 );

    private AtomicReference<ConnectionState> daConnectionStateRef = new AtomicReference<ConnectionState> ( ConnectionState.CLOSED );

    private List<ScadaItem> items = new ArrayList<> ();

    private SortedSet<QueueSize> queueSizes = new TreeSet<> ();

    private long numOfCalls = 0l;

    public String getName ()
    {
        return name;
    }

    public void setName ( String name )
    {
        this.name = name;
    }

    public String getDaUrl ()
    {
        return daUrl;
    }

    public void setDaUrl ( String daUrl )
    {
        this.daUrl = daUrl;
    }

    public Integer getJmxPort ()
    {
        return jmxPort;
    }

    public void setJmxPort ( Integer jmxPort )
    {
        this.jmxPort = jmxPort;
    }

    public void initialize ( final String hostName, ScheduledExecutorService scheduler )
    {
        scheduler.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                loginOrRetrieveJmxData ( hostName );
            }
        }, 0, 10, TimeUnit.SECONDS );
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
        if ( daUrl != null )
        {
            connectToNeoScada ( hostName );
        }
    }

    private void connectToNeoScada ( String hostName )
    {
        try
        {
            Class.forName ( "org.eclipse.scada.da.client.ngp.ConnectionImpl" );
        }
        catch ( ClassNotFoundException e )
        {
            logger.error ( "could not connect to to scada", e );
            return;
        }

        final ConnectionInformation ci = ConnectionInformation.fromURI ( daUrl.replace ( "localhost", hostName ) );

        final Connection connection = (Connection)ConnectionFactory.create ( ci );
        if ( connection == null )
        {
            logger.error ( "Unable to find a connection driver for specified URI" );
            return;
        }

        connection.addConnectionStateListener ( new ConnectionStateListener () {
            @Override
            public void stateChange ( org.eclipse.scada.core.client.Connection connection, ConnectionState state, Throwable error )
            {
                daConnectionStateRef.set ( state );
            }
        } );

        final AutoReconnectController controller = new AutoReconnectController ( connection );
        controller.connect ();

        final ItemManagerImpl itemManager = new ItemManagerImpl ( connection );
        for ( final ScadaItem item : items )
        {
            final DataItem dataItem = new DataItem ( item.getTag (), itemManager );
            dataItem.addObserver ( new Observer () {
                @Override
                public void update ( final Observable observable, final Object update )
                {
                    final DataItemValue div = (DataItemValue)update;
                    item.setDaItemValue ( div );
                }
            } );
        }
    }

    protected void loginOrRetrieveJmxData ( String hostName )
    {
        numOfCalls += 1;
        if ( jmxcRef.get () == null )
        {
            try
            {
                JMXServiceURL jmxUrl = new JMXServiceURL ( "service:jmx:rmi:///jndi/rmi://" + hostName + ":" + jmxPort + "/jmxrmi" );
                logger.debug ( "connect to JMX server {}", jmxUrl );
                JMXConnector jmxc = JMXConnectorFactory.connect ( jmxUrl );
                jmxcRef.set ( jmxc );
            }
            catch ( IOException e )
            {
                logger.error ( "JMX Connection failed", e );
                jmxcRef.set ( null );
                memoryRef.set ( new JvmMemoryUsage () );
                loadAverageRef.set ( 99.0 );
            }
        }
        if ( jmxcRef.get () != null )
        {
            try
            {
                logger.debug ( "got JMX reference {}", jmxcRef );
                MBeanServerConnection connection = jmxcRef.get ().getMBeanServerConnection ();
                final MemoryMXBean remoteMemory = ManagementFactory.newPlatformMXBeanProxy ( connection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class );
                JvmMemoryUsage jvmMemory = new JvmMemoryUsage ();
                jvmMemory.setHeapMemoryUsage ( remoteMemory.getHeapMemoryUsage () );
                jvmMemory.setNonHeapMemoryUsage ( remoteMemory.getNonHeapMemoryUsage () );
                this.memoryRef.set ( jvmMemory );

                final OperatingSystemMXBean remoteOs = ManagementFactory.newPlatformMXBeanProxy ( connection, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class );
                this.loadAverageRef.set ( remoteOs.getSystemLoadAverage () );

                final ThreadMXBean threads = ManagementFactory.newPlatformMXBeanProxy ( connection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class );
                ThreadInformation ti = new ThreadInformation ( threadRef.get () );
                ti.setNumOfThreads ( threads.getThreadCount () );
                if ( numOfCalls % 10 == 0 )
                {
                    long[] dl = threads.findDeadlockedThreads ();
                    ti.setDeadlock ( dl != null && dl.length > 0 );
                }
                threadRef.set ( ti );

                SortedSet<QueueSize> qs = new TreeSet<> ();
                logger.trace ( "start querying attributes" );

                for ( ObjectInstance mbean : connection.queryMBeans ( null, null ) )
                {
                    if ( mbean.getClassName ().equals ( "org.eclipse.scada.utils.concurrent.ExecutorServiceExporterImpl" ) )
                    {
                        Integer queueSize = (Integer)connection.getAttribute ( mbean.getObjectName (), "QueueSize" );
                        qs.add ( new QueueSize ( mbean.getObjectName ().toString (), queueSize ) );
                    }
                }
                logger.trace ( "finished querying attributes" );
                this.queueSizes = qs;
            }
            catch ( Exception e )
            {
                logger.error ( "JMX Operation failed", e );
                if ( jmxcRef.get () != null )
                {
                    try
                    {
                        jmxcRef.get ().close ();
                    }
                    catch ( IOException e1 )
                    {
                        logger.debug ( "JMX Operation failed, but we don't care", e );
                    }
                }
                jmxcRef.set ( null );
                memoryRef.set ( new JvmMemoryUsage () );
                loadAverageRef.set ( 99.0 );
            }
        }
    }

    public JvmMemoryUsage getMemory ()
    {
        return memoryRef.get ();
    }

    public ThreadInformation getThreadInformation ()
    {
        return threadRef.get ();
    }

    public double getLoadAverage ()
    {
        return loadAverageRef.get ();
    }

    public ConnectionState getDaConnectionState ()
    {
        return daConnectionStateRef.get ();
    }

    public double getFreeMemoryPercent ()
    {
        JvmMemoryUsage m = memoryRef.get ();
        double max = Long.valueOf ( m.getHeapMemoryUsage ().getMax () ).doubleValue ();
        if ( max == 0.0 )
        {
            return 0.0;
        }
        double free = max - Long.valueOf ( m.getHeapMemoryUsage ().getUsed () ).doubleValue ();
        return ( free * 100 / max );
    }

    public int getMaxQueueSize ()
    {
        List<QueueSize> qs = new LinkedList<> ( this.queueSizes );
        int i = 0;
        for ( QueueSize s : qs )
        {
            i = Math.max ( i, s.getSize () );
        }
        return i;
    }

    public int getSumQueueSize ()
    {
        List<QueueSize> qs = new LinkedList<> ( this.queueSizes );
        int i = 0;
        for ( QueueSize s : qs )
        {
            i += s.getSize ();
        }
        return i;
    }

    public boolean isMemoryWarningThreshold ()
    {
        return getFreeMemoryPercent () < 5.0;
    }

    public boolean isMemoryCriticalThreshold ()
    {
        return getFreeMemoryPercent () < 0.5;
    }

    public boolean isQueueSizesWarningThreshold ()
    {
        return getMaxQueueSize () > 50;
    }

    public boolean isQueueSizesCriticalThreshold ()
    {
        return getMaxQueueSize () > 10000;
    }

    public boolean isLoadAverageWarningThreshold ()
    {
        return loadAverageRef.get () > 0.95;
    }

    public boolean isLoadAverageCriticalThreshold ()
    {
        return loadAverageRef.get () > 2.0;
    }

    public boolean isWarning ()
    {
        return isMemoryWarningThreshold () || isQueueSizesWarningThreshold () || isLoadAverageWarningThreshold () || isDisconnected () || isValueWarning ();
    }

    public boolean isCritical ()
    {
        return isMemoryCriticalThreshold () || isQueueSizesCriticalThreshold () || isLoadAverageCriticalThreshold () || isDisconnected () || isValueCritical () || isDeadlock();
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

    public boolean isDisconnected ()
    {
        if ( daUrl == null || daUrl.isEmpty () )
        {
            return false;
        }
        return daConnectionStateRef.get () != ConnectionState.BOUND;
    }

    public boolean isDeadlock ()
    {
        return threadRef.get ().isDeadlock ();
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

    public List<ScadaItem> getItems ()
    {
        return items;
    }

    public SortedSet<QueueSize> getQueueSizes ()
    {
        return queueSizes;
    }
}
