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
package org.eclipse.neoscada.contrib.iec104torest;

import java.io.File;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

public class Application
{
    final static Logger logger = LoggerFactory.getLogger ( Application.class );

    public static void main ( String[] args ) throws Exception
    {
        // default configuration
        String configFile = null;

        // config file would be the first command line parameter
        if ( args.length > 0 )
        {
            configFile = args[0];
        }
        Configuration cfg = new Configuration ();
        if ( configFile != null )
        {
            logger.info ( "Reading configuration from {}", configFile );
            File cf = new File ( configFile );
            if ( cf.canRead () )
            {
                FileReader fr = new FileReader ( cf );
                cfg = new GsonBuilder ().create ().fromJson ( fr, Configuration.class );
            }
            else
            {
                logger.warn ( "{} not found or can not be read!", configFile );
            }
        }
        else
        {
            logger.info ( "Using standard configuration" );
        }
        logger.info ( "Connecting to {}", cfg.getIec104Address () );
        logger.info ( "Providing HTTP interface on {}", cfg.getHttpAddress () );

        final Iec104ToRest exporter = new Iec104ToRest ( cfg );
        exporter.start ();
        Runtime.getRuntime ().addShutdownHook ( new Thread () {
            @Override
            public void run ()
            {
                exporter.stop ();
            }
        } );
    }
}
