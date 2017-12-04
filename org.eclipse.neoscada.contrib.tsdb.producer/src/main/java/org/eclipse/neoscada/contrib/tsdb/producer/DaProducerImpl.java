package org.eclipse.neoscada.contrib.tsdb.producer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.neoscada.contrib.tsdb.api.DaProducer;
import org.eclipse.neoscada.contrib.tsdb.api.ValueChangeEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.pushstream.PushEventSource;
import org.osgi.util.pushstream.PushStreamProvider;
import org.osgi.util.pushstream.PushbackPolicyOption;
import org.osgi.util.pushstream.QueuePolicyOption;
import org.osgi.util.pushstream.SimplePushEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Component ( configurationPid = "$",
        service = DaProducer.class,
        property = { "osgi.command.scope=tsdb", "osgi.command.function=connections" },
        immediate = true )
public class DaProducerImpl implements DaProducer
{

    private static final Logger logger = LoggerFactory.getLogger ( DaProducerImpl.class );

    private static final Gson gson = new GsonBuilder ().create ();

    private final SortedSet<ConfiguredConnection> configuredConnections = new ConcurrentSkipListSet<> ();

    private ScheduledExecutorService scheduler;

    final PushStreamProvider pushStreamProvider = new PushStreamProvider ();

    private SimplePushEventSource<ValueChangeEvent> pushEventSource;

    @Activate
    void activate () throws JsonIOException, JsonSyntaxException, FileNotFoundException
    {
        final String configFileLocation = System.getProperty ( "org.eclipse.neoscada.contrib.tsdb.config", "config.json" );
        final File configFile = new File ( configFileLocation );
        logger.debug ( "reading config file '{}'", configFile );
        final Configuration configuration = gson.fromJson ( new FileReader ( configFile ), Configuration.class );
        logger.debug ( "read configuration with {} connections", configuration.getConnections ().size () );

        synchronized ( this )
        {
            this.scheduler = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( "DaProducer-%d" ).build () );
            pushEventSource = pushStreamProvider.buildSimpleEventSource ( ValueChangeEvent.class ) //
                    .withParallelism ( 1 ) //
                    .withPushbackPolicy ( PushbackPolicyOption.LINEAR, TimeUnit.SECONDS.toNanos ( 30 ) ) //
                    .withQueuePolicy ( QueuePolicyOption.DISCARD_OLDEST ) //
                    .withBuffer ( new LinkedBlockingQueue<> ( configuration.getQueueSize () ) )//
                    .build ();
            configuration.getConnections ().stream ().forEach ( connectionConfig -> configureConnection ( connectionConfig, pushEventSource ) );
            if ( configuration.getHeartBeat () > 0 )
            {
                this.scheduler.scheduleAtFixedRate ( new Runnable () {
                    @Override
                    public void run ()
                    {
                        configuredConnections.stream () //
                                .forEach ( configuredConnection -> configuredConnection.triggerHeartBeats () );
                    }
                }, configuration.getHeartBeat (), configuration.getHeartBeat (), TimeUnit.SECONDS );
            }
        }
    }

    private void configureConnection ( ConnectionConfiguration connectionConfiguration, SimplePushEventSource<ValueChangeEvent> pushEventSource )
    {
        logger.debug ( "configure new connection" );

        try
        {
            this.configuredConnections.add ( new ConfiguredConnection ( connectionConfiguration, pushEventSource ) );
        }
        catch ( Exception e )
        {
            logger.error ( "failed to configure connection {}", connectionConfiguration.getNgpUrl (), e );
        }
    }

    @Deactivate
    void deactivate ()
    {
        logger.trace ( "deactivate ..." );
        synchronized ( this )
        {
            this.configuredConnections.parallelStream ().forEach ( configuration -> configuration.dispose () );
            Optional.ofNullable ( this.scheduler ).ifPresent ( scheduler -> scheduler.shutdown () );
            this.configuredConnections.clear ();
            Optional.ofNullable ( this.pushEventSource ).ifPresent ( SimplePushEventSource::close );
        }
        logger.trace ( "deactivate finished" );
    }

    /*
     * command: list available connections 
     */
    public String connections ()
    {
        StringBuilder sb = new StringBuilder ();
        sb.append ( this.configuredConnections.stream ().map ( ConfiguredConnection::toString ).collect ( Collectors.joining ( "\n" ) ) );
        return sb.toString ();
    }

    @Override
    public Optional<PushEventSource<ValueChangeEvent>> getPushEventSource ()
    {
        return Optional.ofNullable ( this.pushEventSource );
    }
}
