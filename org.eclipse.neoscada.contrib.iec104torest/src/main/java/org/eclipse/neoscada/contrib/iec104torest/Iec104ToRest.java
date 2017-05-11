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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.QualityInformation;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;
import org.eclipse.neoscada.protocol.iec60870.client.AutoConnectClient;
import org.eclipse.neoscada.protocol.iec60870.client.AutoConnectClient.ModulesFactory;
import org.eclipse.neoscada.protocol.iec60870.client.AutoConnectClient.StateListener;
import org.eclipse.neoscada.protocol.iec60870.client.ClientModule;
import org.eclipse.neoscada.protocol.iec60870.client.data.DataListener;
import org.eclipse.neoscada.protocol.iec60870.client.data.DataModule;
import org.eclipse.neoscada.protocol.iec60870.client.data.DataModuleOptions;
import org.eclipse.neoscada.protocol.iec60870.client.data.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Iec104ToRest
{
    private final static Logger logger = LoggerFactory.getLogger ( Iec104ToRest.class );

    private static final Charset UTF8 = Charset.forName ( "UTF-8" );

    private final static SimpleDateFormat exportDf = new SimpleDateFormat ( "yyyyMMddHHmm00" );

    static
    {
        exportDf.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
    }

    private InetSocketAddress iecServer;

    private InetSocketAddress httpAddress;

    private ExecutorService executor;

    private ScheduledExecutorService exportExecutor;

    private Map<String, String> mapping = new TreeMap<> (); // IECADDRESS,TAG

    private Map<String, Value<?>> values = new ConcurrentSkipListMap<> (); // IECADDRESS,VALUE

    private Configuration cfg;

    private Undertow server;

    private AutoConnectClient iecClient;

    private ScheduledFuture<?> csvExporter;

    public Iec104ToRest ( Configuration cfg )
    {
        String[] iecServerParts = cfg.getIec104Address ().split ( ":" );
        String[] httpAddressParts = cfg.getHttpAddress ().split ( ":" );
        this.iecServer = InetSocketAddress.createUnresolved ( iecServerParts[0], Integer.parseInt ( iecServerParts[1] ) );
        this.httpAddress = InetSocketAddress.createUnresolved ( httpAddressParts[0], Integer.parseInt ( httpAddressParts[1] ) );
        this.mapping.putAll ( cfg.getAddressMapping () );
        this.cfg = cfg;
    }

    public void start ()
    {
        logger.info ( "start()" );
        this.executor = Executors.newSingleThreadExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "iec104-processor-%d" ).build () );
        this.exportExecutor = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "export-executor-%d" ).build () );
        startIecClient ();
        startCsvExporter ();
        startHttpServer ();
        logger.info ( "start() finished ..." );
    }

    public void stop ()
    {
        logger.info ( "stop()" );
        if ( iecClient != null )
        {
            logger.info ( "shut down iecClient" );
            iecClient.close ();
            iecClient = null;
        }
        if ( csvExporter != null )
        {
            logger.info ( "shut down csvExporter" );
            csvExporter.cancel ( false );
            csvExporter = null;
        }
        if ( this.executor != null )
        {
            logger.info ( "shut down executor" );
            this.executor.shutdown ();
        }
        if ( this.exportExecutor != null )
        {
            logger.info ( "shut down exportExecutor" );
            this.exportExecutor.shutdown ();
        }
        if ( server != null )
        {
            logger.info ( "shut down server" );
            server.stop ();
        }
        logger.info ( "stop finished" );
    }

    private void startIecClient ()
    {
        ProtocolOptions options = new ProtocolOptions.Builder ().build ();
        DataProcessor dataProcessor = new DataProcessor ( executor, new DataListener () {
            @Override
            public void update ( ASDUAddress commonAddress, InformationObjectAddress objectAddress, Value<?> value )
            {
                String addr = formatAddress ( commonAddress, objectAddress );
                values.put ( addr, value );
            }

            @Override
            public void started ()
            {
                logger.info ( "GA sent ..." );
            }

            @Override
            public void disconnected ()
            {
                logger.info ( "disconected ...." );
                markAsInvalid ();
            }
        } );
        // use standard options, cause doesn't matter, we don't write
        DataModuleOptions dataModuleOptions = new DataModuleOptions.Builder ().build ();
        ModulesFactory modulesFactory = new ModulesFactory () {
            @Override
            public List<ClientModule> createModules ()
            {
                return Arrays.asList ( new DataModule ( dataProcessor, dataModuleOptions ) );
            }
        };
        iecClient = new AutoConnectClient ( iecServer.getHostName (), iecServer.getPort (), options, modulesFactory, new StateListener () {
            @Override
            public void stateChanged ( AutoConnectClient.State state, Throwable e )
            {
                logger.info ( "state {}", state );
            }
        } );
    }

    private void startCsvExporter ()
    {
        long ts = System.currentTimeMillis ();
        final int p = cfg.getStorePeriod () * 60 * 1000;
        final long n = ( p == 0 ) ? 0 : p - ts % p;
        if ( cfg.getStorePeriod () > 0 )
        {
            csvExporter = exportExecutor.scheduleAtFixedRate ( new Runnable () {
                @Override
                public void run ()
                {
                    long ts = System.currentTimeMillis ();
                    long rounded = ( ts - ts % p );
                    try
                    {
                        exportToCsv ( rounded );
                    }
                    catch ( IOException e )
                    {
                        logger.warn ( "export to csv failed", e );
                    }
                }
            }, n, p, TimeUnit.MILLISECONDS );
        }
    }

    protected void exportToCsv ( long rounded ) throws IOException
    {
        Path target = Paths.get ( cfg.getStoreDirectory (), toFileName ( rounded ) );
        logger.info ( "write export to '{}'", target );
        try ( FileWriter fw = new FileWriter ( target.toFile () ); BufferedWriter bw = new BufferedWriter ( fw ) )
        {
            bw.write ( createMappedValues () );
        }
    }

    private String toFileName ( long rounded )
    {
        String d = exportDf.format ( new Date ( rounded ) );
        return d + cfg.getStoreSuffix ();
    }

    @SuppressWarnings ( { "unchecked", "rawtypes" } )
    protected void markAsInvalid ()
    {
        Set<Entry<String, Value<?>>> entries = new HashSet<> ( values.entrySet () );
        for ( Entry<String, Value<?>> entry : entries )
        {
            Value<?> v = entry.getValue ();
            Value<?> invalidValue = new Value ( v.getValue (), v.getTimestamp (), QualityInformation.INVALID );
            values.put ( entry.getKey (), invalidValue );
        }
    }

    protected String formatAddress ( ASDUAddress commonAddress, InformationObjectAddress objectAddress )
    {
        return String.format ( "%d-%d", commonAddress.toArray ()[0], commonAddress.toArray ()[1] ) + "-" + String.format ( "%d-%d-%s", objectAddress.toArray ()[0], objectAddress.toArray ()[1], objectAddress.toArray ()[2] );
    }

    /**
     * starts http server, blocks
     */
    private void startHttpServer ()
    {
        server = Undertow.builder ().addHttpListener ( httpAddress.getPort (), httpAddress.getHostName () ).setHandler ( new HttpHandler () {
            @Override
            public void handleRequest ( final HttpServerExchange exchange ) throws Exception
            {
                exchange.getResponseHeaders ().put ( Headers.CONTENT_TYPE, "text/plain; charset=utf-8" );
                exchange.getResponseHeaders ().put ( Headers.CONTENT_DISPOSITION, "inline; filename=\"" + toFileName ( System.currentTimeMillis () ) + "\"" );
                exchange.getResponseSender ().send ( createMappedValues (), UTF8 );
            }
        } ).build ();
        logger.info ( "starting server ..." );
        new Thread ( new Runnable () {
            @Override
            public void run ()
            {
                server.start ();
            }
        } ).run ();
    }

    protected String createMappedValues ()
    {
        StringBuilder sb = new StringBuilder ();
        if ( cfg.isMapByNames () )
        {
            for ( Entry<String, String> entry : mapping.entrySet () )
            {
                addLine ( sb, entry.getKey (), entry.getValue (), values.get ( entry.getKey () ) );
            }
        }
        else
        {
            for ( Entry<String, Value<?>> entry : values.entrySet () )
            {
                String tag = mapping.get ( entry.getKey () );
                addLine ( sb, entry.getKey (), tag, entry.getValue () );
            }
        }
        return sb.toString ();
    }

    private void addLine ( StringBuilder sb, String iecAddress, String tag, Value<?> value )
    {
        sb.append ( iecAddress );
        sb.append ( ";" );
        if ( tag != null )
        {
            sb.append ( tag );
        }
        sb.append ( ";" );
        if ( value != null )
        {
            sb.append ( value.getValue () );
            sb.append ( ";" );
            sb.append ( value.getTimestamp () );
            sb.append ( ";" );
            sb.append ( value.getQualityInformation ().isValid () && value.getQualityInformation ().isTopical () ? '0' : '1' );
            sb.append ( ";" );
            sb.append ( value.getQualityInformation ().isSubstituted () ? '1' : '0' );
        }
        else
        {
            sb.append ( ";;0;0" );
        }
        sb.append ( "\n" );
    }
}
