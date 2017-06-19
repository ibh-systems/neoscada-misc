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
}
