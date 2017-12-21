package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.neoscada.contrib.tsdb.api.DaConsumer;
import org.eclipse.neoscada.contrib.tsdb.api.DaProducer;
import org.eclipse.neoscada.contrib.tsdb.api.ValueChangeEvent;
import org.eclipse.scada.core.Variant;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component ( configurationPid = "$",
        service = DaConsumer.class,
        immediate = true,
        property = { "osgi.command.scope=tsdb", "osgi.command.function=kafka" } )
public class DaConsumerImpl implements DaConsumer
{
    private static final Logger logger = LoggerFactory.getLogger ( DaConsumerImpl.class );

    private static final Gson gson = new GsonBuilder () //
            .serializeNulls ().serializeSpecialFloatingPointValues () //
            .registerTypeAdapter ( Variant.class, new VariantSerializer () ) //
            .create ();

    private static final int MEGA_BYTE = 1024 * 1024;

    private ScheduledExecutorService scheduler;

    private DaProducer producer;

    private Producer<String, String> kafkaProducer;

    private final Properties kafkaProperties = new Properties ();

    private Invocable invocable;

    @Activate
    void activate () throws Exception
    {
        logger.debug ( "activated!" );

        final String configFileLocation = System.getProperty ( "org.eclipse.neoscada.contrib.tsdb.config", "config.json" );
        final File configFile = new File ( configFileLocation );
        logger.debug ( "reading config file '{}'", configFile );
        final Configuration configuration = gson.fromJson ( new FileReader ( configFile ), Configuration.class );
        logger.debug ( "read configuration with properties: {}", configuration );

        ScriptEngine engine = new ScriptEngineManager ().getEngineByMimeType ( "text/javascript" );
        if ( configuration.getJavaScriptFile () != null )
        {
            engine.eval ( new FileReader ( new File ( configuration.getJavaScriptFile () ) ) );
        }
        else
        {
            engine.eval ( configuration.getJavaScript () );
        }
        invocable = (Invocable)engine;

        this.scheduler = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( DaConsumerImpl.class.getName () + "-%d" ).build () );
        setupKafka ( configuration );
        setupQueue ( configuration );
    }

    private void setupKafka ( Configuration configuration ) throws Exception
    {
        kafkaProperties.put ( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getKafkaUrl () );
        kafkaProperties.put ( ProducerConfig.ACKS_CONFIG, "all" );
        kafkaProperties.put ( ProducerConfig.RETRIES_CONFIG, 0 );
        kafkaProperties.put ( ProducerConfig.BATCH_SIZE_CONFIG, 16384 );
        kafkaProperties.put ( ProducerConfig.LINGER_MS_CONFIG, 1 );
        kafkaProperties.put ( ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * MEGA_BYTE );
        kafkaProperties.put ( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        kafkaProperties.put ( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );

        try
        {
            this.kafkaProducer = new KafkaProducer<> ( kafkaProperties );
        }
        catch ( Exception e )
        {
            logger.error ( "could not create Kafka producer", e );
            throw e;
        }
    }

    private void setupQueue ( final Configuration configuration )
    {
        this.scheduler.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                flushQueue ( configuration, producer.getQueue ().orElse ( new ArrayDeque<> ( 0 ) ) );
            }
        }, configuration.getFlushInterval (), configuration.getFlushInterval (), TimeUnit.SECONDS );
    }

    protected void flushQueue ( final Configuration configuration, Deque<ValueChangeEvent> queue )
    {
        int size = queue.size ();
        for ( int i = 0; i < size; i++ )
        {
            ValueChangeEvent vce = queue.pollLast ();
            if ( vce != null )
            {
                storeToKafka ( configuration, vce );
            }
        }
    }

    protected void storeToKafka ( final Configuration configuration, final ValueChangeEvent valueChangeEvent )
    {
        try
        {
            final String topic = toTopicName ( valueChangeEvent.getId () );
            kafkaProducer.send ( new ProducerRecord<String, String> ( topic, gson.toJson ( valueChangeEvent ) ) );
        }
        catch ( Exception e )
        {
            logger.warn ( "storing event to Kafka failed", e );
            logger.trace ( "Event was: {}", valueChangeEvent );
        }
    }

    private String toTopicName ( String itemId )
    {
        Object result;
        try
        {
            result = invocable.invokeFunction ( "toTopic", itemId );
            return (String)result;
        }
        catch ( Exception e )
        {
            logger.error ( "failed to execute javascript function 'toTopic' ", e );
            return null;
        }
    }

    public void kafka ()
    {
        Optional.ofNullable ( kafkaProducer ).ifPresent ( new Consumer<Producer<String, String>> () {
            @Override
            public void accept ( Producer<String, String> producer )
            {
                producer.metrics () //
                        .entrySet () //
                        .stream ()//
                        .forEach ( entry -> {
                            if ( entry.getValue ().value () < 1 && entry.getValue ().value () != 0.0 && !Double.isInfinite ( entry.getValue ().value () ) )
                            {
                                System.out.println ( String.format ( "%-30s      %1.3f (%s)", entry.getKey ().name (), entry.getValue ().value (), entry.getKey ().description () ) );
                            }
                            else
                            {
                                System.out.println ( String.format ( "%-30s %9.0f (%s)", entry.getKey ().name (), entry.getValue ().value (), entry.getKey ().description () ) );
                            }
                        } );
            }
        } );
    }

    @Deactivate
    void deactivate () throws Exception
    {
        Optional.ofNullable ( this.kafkaProducer ).ifPresent ( kafkaProducer -> kafkaProducer.close () );
        Optional.ofNullable ( this.scheduler ).ifPresent ( scheduler -> scheduler.shutdown () );
    }

    @Override
    @Reference ( cardinality = ReferenceCardinality.MANDATORY )
    public void setProducer ( final DaProducer producer )
    {
        logger.trace ( "producer set" );
        this.producer = producer;
    }
}
