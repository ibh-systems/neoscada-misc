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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ServerStatus
{
    private final static Logger logger = LoggerFactory.getLogger ( ServerStatus.class );

    private List<ScadaAliveCheck> neoscada = new LinkedList<> ();

    private List<Iec104AliveCheck> iec104 = new LinkedList<> ();

    private transient ScheduledExecutorService scheduler = null;

    public List<ScadaAliveCheck> getNeoscada ()
    {
        return neoscada;
    }

    public void setNeoscada ( List<ScadaAliveCheck> neoscada )
    {
        this.neoscada = neoscada;
    }

    public List<Iec104AliveCheck> getIec104 ()
    {
        return iec104;
    }

    public void setIec104 ( List<Iec104AliveCheck> iec104 )
    {
        this.iec104 = iec104;
    }

    public JsonElement renderStatus ( Gson gson )
    {
        JsonArray arr = new JsonArray ();
        for ( ScadaAliveCheck service : neoscada )
        {
            JsonObject o = new JsonObject ();
            o.add ( "name", new JsonPrimitive ( service.getName () ) );
            o.add ( "memory", gson.toJsonTree ( service.getMemory () ) );
            o.add ( "threads", gson.toJsonTree ( service.getThreadInformation () ) );
            o.add ( "queuesize", new JsonPrimitive ( service.getSumQueueSize () ) );
            o.add ( "loadAverage", gson.toJsonTree ( service.getLoadAverage () ) );
            if ( service.getDaUrl () != null && !service.getDaUrl ().isEmpty () )
            {
                JsonObject s = new JsonObject ();
                s.addProperty ( "connection", "" + service.getDaConnectionState () );
                JsonArray a = new JsonArray ();
                for ( ScadaItem item : service.getItems () )
                {
                    JsonObject i = new JsonObject ();
                    i.add ( "tag", new JsonPrimitive ( item.getTag () ) );
                    i.add ( "value", new JsonPrimitive ( "" + item.getDaItemValue () ) );
                    i.add ( "valueError", new JsonPrimitive ( item.isValueError () ) );
                    i.add ( "toggleError", new JsonPrimitive ( item.isToggleError () ) );
                    i.add ( "timestampError", new JsonPrimitive ( item.isTimestampError () ) );
                    i.add ( "ll", new JsonPrimitive ( item.isLlError () ) );
                    i.add ( "l", new JsonPrimitive ( item.isLError () ) );
                    i.add ( "h", new JsonPrimitive ( item.isHError () ) );
                    i.add ( "hh", new JsonPrimitive ( item.isHhError () ) );
                    a.add ( i );
                }
                s.add ( "items", a );
                o.add ( "scada", s );
            }
            arr.add ( o );
        }
        for ( Iec104AliveCheck service : iec104 )
        {
            JsonObject o = new JsonObject ();
            o.add ( "name", new JsonPrimitive ( service.getName () ) );
            o.addProperty ( "connection", "" + service.getConnectionState () );
            JsonArray a = new JsonArray ();
            for ( ScadaItem item : service.getItems () )
            {
                JsonObject i = new JsonObject ();
                i.add ( "tag", new JsonPrimitive ( item.getTag () ) );
                i.add ( "value", new JsonPrimitive ( "" + item.getDaItemValue () ) );
                i.add ( "valueError", new JsonPrimitive ( item.isValueError () ) );
                i.add ( "toggleError", new JsonPrimitive ( item.isToggleError () ) );
                i.add ( "timestampError", new JsonPrimitive ( item.isTimestampError () ) );
                i.add ( "ll", new JsonPrimitive ( item.isLlError () ) );
                i.add ( "l", new JsonPrimitive ( item.isLError () ) );
                i.add ( "h", new JsonPrimitive ( item.isHError () ) );
                i.add ( "hh", new JsonPrimitive ( item.isHhError () ) );
                a.add ( i );
            }
            o.add ( "items", a );
            arr.add ( o );
        }
        return arr;
    }

    public String renderHealth ()
    {
        StringBuilder sb = new StringBuilder ();
        for ( ScadaAliveCheck service : neoscada )
        {
            sb.append ( "  " + service.getName () + " " + service.toStatus () + "\n" );
            if ( service.isDeadlock () )
            {
                sb.append ( "    !!! DEADLOCK !!!\n" );
            }
            if ( service.isMemoryCriticalThreshold () )
            {
                sb.append ( "    nearly out of memory! Is " + String.format ( "%5.2f", service.getFreeMemoryPercent () ) + "% free\n" );
            }
            else if ( service.isMemoryWarningThreshold () )
            {
                sb.append ( "    not much memory left, is " + String.format ( "%5.2f", service.getFreeMemoryPercent () ) + "% free\n" );
            }
            else
            {
                sb.append ( "    memory ok at " + String.format ( "%5.2f", service.getFreeMemoryPercent () ) + "% free\n" );
            }
            if ( service.isLoadAverageCriticalThreshold () )
            {
                sb.append ( "    system load is to high! Is " + service.getLoadAverage () + "\n" );
            }
            else if ( service.isLoadAverageWarningThreshold () )
            {
                sb.append ( "    system load is questionable, is " + service.getLoadAverage () + "\n" );
            }
            else
            {
                sb.append ( "    system load ok at " + service.getLoadAverage () + "\n" );
            }
            if ( service.isQueueSizesWarningThreshold () )
            {
                sb.append ( "    Queue Size exceeded: " + service.getMaxQueueSize () + "\n" );
                for ( QueueSize qs : service.getQueueSizes () )
                {
                    if ( qs.getSize () > 1 )
                    {
                        sb.append ( "      Queue: " + qs + "\n" );
                    }
                }
            }
            if ( service.isDisconnected () )
            {
                sb.append ( "    NeoSCADA DA Connection to server failed!\n" );
            }
            for ( ScadaItem item : service.getItems () )
            {
                if ( item.isProblem () )
                {
                    sb.append ( "    NeoSCADA DA item check " + item.getTag () + " failed!\n" );
                }
            }
        }
        for ( Iec104AliveCheck service : iec104 )
        {
            sb.append ( "  " + service.getName () + " " + service.toStatus () + "\n" );
            if ( service.isDisconnected () )
            {
                sb.append ( "    IEC104 Connection to server failed!\n" );
            }
            for ( ScadaItem item : service.getItems () )
            {
                if ( item.isProblem () )
                {
                    sb.append ( "    IEC104 signal check " + item.getTag () + " failed!\n" );
                }
            }
        }
        return sb.toString ();
    }

    public boolean isWarning ()
    {
        boolean warn = false;
        for ( ScadaAliveCheck service : neoscada )
        {
            warn |= service.isWarning ();
        }
        return warn;
    }

    public boolean isCritical ()
    {
        boolean crit = false;
        for ( ScadaAliveCheck service : neoscada )
        {
            crit |= service.isCritical ();
        }
        return crit;
    }

    public void initialize ( String hostName )
    {
        scheduler = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "ServerConfig-Scheduler-%d" ).build () );
        if ( neoscada != null && neoscada.size () > 0 )
        {
            for ( ScadaAliveCheck scadaAliveCheck : neoscada )
            {
                logger.debug ( "initializing scada {}", scadaAliveCheck.getName () );
                scadaAliveCheck.initialize ( hostName, scheduler );
            }
        }
        if ( iec104 != null && iec104.size () > 0 )
        {
            for ( Iec104AliveCheck iec104AliveCheck : iec104 )
            {
                logger.debug ( "initializing iec104 {}", iec104AliveCheck.getName () );
                iec104AliveCheck.initialize ( hostName, scheduler );
            }
        }
    }
}
