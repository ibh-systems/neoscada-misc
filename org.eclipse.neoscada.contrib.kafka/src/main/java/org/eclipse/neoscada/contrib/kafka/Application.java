package org.eclipse.neoscada.contrib.kafka;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
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

        @SuppressWarnings ( "serial" )
        Type listOfConfigurations = new TypeToken<ArrayList<Configuration>>(){}.getType();
        final List<Configuration> configurations = gson.fromJson ( new FileReader ( configFile ), listOfConfigurations );
        Multiplexer multiplexer = new Multiplexer ( configurations );
        multiplexer.start ();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { multiplexer.stop (); }
        });
    }
}
