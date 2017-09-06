package org.eclipse.neoscada.contrib.kafka;

import java.io.File;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Application
{
    private static final Logger logger = LoggerFactory.getLogger ( Application.class );
    
    private static final Gson gson = new GsonBuilder ().create ();

    public static void main ( String[] args ) throws Exception
    {
        String configFileLocation = System.getProperty ( "org.eclipse.neoscada.contrib.kafka.config", "config.json" );
        File configFile = new File ( configFileLocation );
        logger.info ( "reading config file '{}'", configFile );

        final Configuration configuration = gson.fromJson ( new FileReader ( configFile ), Configuration.class );
        KafkaIngester kafkaIngester = new KafkaIngester ( configuration );
        kafkaIngester.start ();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { kafkaIngester.stop (); }
        });
    }
}
