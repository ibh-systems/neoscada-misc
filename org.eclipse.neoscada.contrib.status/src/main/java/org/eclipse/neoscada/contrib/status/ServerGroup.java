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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ServerGroup
{
    public static class Serializer implements JsonDeserializer<ServerGroup>
    {
        @Override
        public ServerGroup deserialize ( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
        {
            ServerGroup serverGroup = new ServerGroup ();
            for ( Entry<String, JsonElement> entry : json.getAsJsonObject ().entrySet () )
            {
                serverGroup.serverConfigs.put ( entry.getKey (), (ServerStatus)context.deserialize ( entry.getValue (), ServerStatus.class ) );
            }
            return serverGroup;
        }
    }

    private Map<String, ServerStatus> serverConfigs = new LinkedHashMap<> ();

    private final Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();

    public void initialize ()
    {
        for ( Entry<String, ServerStatus> entry : serverConfigs.entrySet () )
        {
            entry.getValue ().initialize ( entry.getKey () );
        }
    }

    public String renderStatus ()
    {
        JsonObject jsonObject = new JsonObject ();
        for ( Entry<String, ServerStatus> entry : serverConfigs.entrySet () )
        {
            jsonObject.add ( entry.getKey (), entry.getValue ().renderStatus ( gson ) );
        }
        return gson.toJson ( jsonObject );
    }

    public String renderHealth ()
    {
        StringBuilder sb = new StringBuilder ();
        boolean warning = false;
        boolean critical = false;
        for ( Entry<String, ServerStatus> entry : serverConfigs.entrySet () )
        {
            warning |= entry.getValue ().isWarning ();
            critical |= entry.getValue ().isCritical ();
            sb.append ( entry.getKey () + ":\n" );
            sb.append ( entry.getValue ().renderHealth () );
        }
        if ( critical )
        {
            return "CRITICAL\n\n" + sb.toString ();
        }
        else if ( warning )
        {
            return "WARNING\n\n" + sb.toString ();
        }
        return "OK\n\n" + sb.toString ();
    }

    public void createGraphiteData ( Collection<GraphiteData> toSend, Date t, String graphitePrefix, String environment )
    {
        String prefix = ( graphitePrefix == null || graphitePrefix.trim ().isEmpty () ) ? "" : graphitePrefix + ".";
        for ( Entry<String, ServerStatus> entry : serverConfigs.entrySet () )
        {
            for ( ScadaAliveCheck c : entry.getValue ().getNeoscada () )
            {
                String tags = String.format ( ";env=%s;server=%s;service=%s", environment, entry.getKey (), c.getName () );
                toSend.add ( new GraphiteData ( prefix + "neoscada.disconnected" + tags, toValue ( c.isDisconnected () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "neoscada.warning" + tags, toValue ( c.isWarning () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "neoscada.critical" + tags, toValue ( c.isCritical () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "neoscada.maxqueuesize" + tags, "" + c.getMaxQueueSize (), t ) );
                for ( ScadaItem item : c.getItems () )
                {
                    toSend.add ( new GraphiteData ( prefix + "neoscada.item.problem." + toMetric ( item.getTag () ) + tags, toValue ( item.isProblem () ), t ) );
                    toSend.add ( new GraphiteData ( prefix + "neoscada.item.value." + toMetric ( item.getTag () ) + tags, "" + item.getDaItemValue ().getValue ().asDouble ( 0.0 ), t ) );
                }

                toSend.add ( new GraphiteData ( prefix + "jmx.threads.deadlock" + tags, toValue ( c.getMemory ().isDeadlock () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.os.loadaverage" + tags, "" + c.getLoadAverage (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.free.percent" + tags, "" + c.getFreeMemoryPercent (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.heap.init" + tags, "" + c.getMemory ().getHeapMemoryUsage ().getInit (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.heap.max" + tags, "" + c.getMemory ().getHeapMemoryUsage ().getMax (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.heap.commited" + tags, "" + c.getMemory ().getHeapMemoryUsage ().getCommitted (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.heap.used" + tags, "" + c.getMemory ().getHeapMemoryUsage ().getUsed (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.nonheap.init" + tags, "" + c.getMemory ().getNonHeapMemoryUsage ().getInit (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.nonheap.max" + tags, "" + c.getMemory ().getNonHeapMemoryUsage ().getMax (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.nonheap.commited" + tags, "" + c.getMemory ().getNonHeapMemoryUsage ().getCommitted (), t ) );
                toSend.add ( new GraphiteData ( prefix + "jmx.memory.nonheap.used" + tags, "" + c.getMemory ().getNonHeapMemoryUsage ().getUsed (), t ) );
            }
            for ( Iec104AliveCheck c : entry.getValue ().getIec104 () )
            {
                String tags = String.format ( ";env=%s;server=%s;service=%s", environment, entry.getKey (), c.getName () );
                toSend.add ( new GraphiteData ( prefix + "iec104.disconnected" + tags, toValue ( c.isDisconnected () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "iec104.warning" + tags, toValue ( c.isWarning () ), t ) );
                toSend.add ( new GraphiteData ( prefix + "iec104.critical" + tags, toValue ( c.isCritical () ), t ) );
                for ( ScadaItem item : c.getItems () )
                {
                    toSend.add ( new GraphiteData ( prefix + "iec104.item.problem." + toMetric ( item.getTag () ) + tags, toValue ( item.isProblem () ), t ) );
                    toSend.add ( new GraphiteData ( prefix + "iec104.item.value." + toMetric ( item.getTag () ) + tags, "" + item.getDaItemValue ().getValue ().asDouble ( 0.0 ), t ) );
                }
            }
        }
    }

    private String toMetric ( String tag )
    {
        return tag.replace ( " ", "_" );
    }

    private String toValue ( boolean v )
    {
        return v ? "1" : "0";
    }
}
