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

import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Application
{
    private final static Logger logger = LoggerFactory.getLogger ( Application.class );

    public static final int PORT = 8080;

    public static void main ( String[] args ) throws Exception
    {
        // default configuration
        String configFile = null;
        int port = PORT;

        // config file would be the first command line parameter
        if ( args.length > 0 )
        {
            configFile = args[0];
        }
        if ( args.length > 1 )
        {
            port = Integer.parseInt ( args[1] );
        }
        Map<String, ServerGroup> cfg = new LinkedHashMap<> ( 1 );
        if ( configFile != null )
        {
            logger.info ( "Reading configuration from " + configFile );
            File cf = new File ( configFile );
            if ( cf.canRead () )
            {
                FileReader fr = new FileReader ( cf );
                cfg = new GsonBuilder () //
                        .registerTypeAdapter ( ServerGroup.class, new ServerGroup.Serializer () )//
                        .create () //
                        .fromJson ( fr, new TypeToken<LinkedHashMap<String, ServerGroup>> () {}.getType () );
            }
        }
        else
        {
            logger.info ( "Using standard configuration" );
        }
        logger.info ( "Providing HTTP interface on " + port );

        new StatusEndpoint ( cfg, port ).run ();
    }
}
