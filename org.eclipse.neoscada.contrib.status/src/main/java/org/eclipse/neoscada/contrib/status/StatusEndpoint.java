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

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class StatusEndpoint
{
    private final static Logger logger = LoggerFactory.getLogger ( StatusEndpoint.class );

    private static final Charset UTF8 = Charset.forName ( "UTF-8" );

    private static final boolean ignoreGraphite = Boolean.getBoolean ( "org.eclipse.neoscada.contrib.status.graphite.dryRun" );

    private final Map<String, ServerGroup> cfg;

    private final int port;

    private String graphiteHost;

    private int graphitePort;

    private String graphitePrefix;

    private ScheduledExecutorService graphiteScheduler;

    public StatusEndpoint ( Map<String, ServerGroup> cfg, int port, String graphiteHost, int graphitePort, String graphitePrefix )
    {
        this.cfg = cfg;
        this.port = port;
        this.graphiteHost = graphiteHost;
        this.graphitePort = graphitePort;
        this.graphitePrefix = graphitePrefix;
    }

    public void run ()
    {
        for ( Entry<String, ServerGroup> entry : cfg.entrySet () )
        {
            entry.getValue ().initialize ();
        }
        startHttpServer ();
        if ( graphiteHost != null && !graphiteHost.trim ().isEmpty () )
        {
            logger.info ( "graphite host is given, use following settings: {}:{}#{}", new Object[] { graphiteHost, graphitePort, graphitePrefix } );
            startGraphiteSender ();
        }
    }

    private void startHttpServer ()
    {
        Undertow server = Undertow.builder ().addHttpListener ( port, "0.0.0.0" ).setHandler ( new HttpHandler () {
            @Override
            public void handleRequest ( final HttpServerExchange exchange ) throws Exception
            {
                exchange.getResponseHeaders ().put ( Headers.CONTENT_TYPE, "text/plain" );
                String result = createStatusMap ( exchange.getRelativePath () );
                if ( result == null )
                {
                    exchange.setStatusCode ( 404 );
                }
                else
                {
                    if ( result.startsWith ( "CRIT" ) )
                    {
                        exchange.setStatusCode ( 500 );
                    }
                    exchange.getResponseSender ().send ( result, UTF8 );
                }
            }
        } ).build ();
        logger.info ( "starting server ..." );
        server.start ();
    }

    private void startGraphiteSender ()
    {
        this.graphiteScheduler = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "GraphiteSender-Scheduler-%d" ).build () );
        this.graphiteScheduler.scheduleWithFixedDelay ( () -> sendToGraphite (), 30, 30, TimeUnit.SECONDS );
    }

    private void sendToGraphite ()
    {
        try
        {
            Date t = new Date ();
            TreeSet<GraphiteData> toSend = new TreeSet<GraphiteData> ();
            createGraphiteData ( toSend, t );
            if ( !ignoreGraphite )
            {
                Socket socket = new Socket ( graphiteHost, graphitePort );
                Writer writer = new OutputStreamWriter ( socket.getOutputStream () );
                for ( GraphiteData d : toSend )
                {
                    writer.write ( d.toString () + "\n" );
                }
                writer.flush ();
                socket.close ();
            }
            else
            {
                for ( GraphiteData d : toSend )
                {
                    logger.info ( "Send to Graphite: '{}'", d );
                }
            }
        }
        catch ( Exception e )
        {
            logger.error ( e.getMessage (), e );
        }
    }

    private void createGraphiteData ( Collection<GraphiteData> toSend, Date t )
    {
        for ( String environment : cfg.keySet () )
        {
            cfg.get ( environment ).createGraphiteData ( toSend, t, this.graphitePrefix, environment );
        }
    }

    protected String createStatusMap ( String path )
    {
        String[] parts = path.split ( "/" );
        if ( parts.length >= 3 )
        {
            ServerGroup serverGroup = cfg.get ( parts[1] );
            if ( serverGroup == null )
            {
                return null;
            }
            if ( "status".equals ( parts[2] ) )
            {
                return serverGroup.renderStatus ();
            }
            else
            {
                return serverGroup.renderHealth ();
            }
        }
        return null;
    }
}
