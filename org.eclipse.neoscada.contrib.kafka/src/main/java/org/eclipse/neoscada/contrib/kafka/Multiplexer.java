package org.eclipse.neoscada.contrib.kafka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Multiplexer
{
    private static final Logger logger = LoggerFactory.getLogger ( Multiplexer.class );

    private KafkaIngester[] ingesters = new KafkaIngester[] {};

    public Multiplexer ( List<Configuration> configurations )
    {
        ingesters = new KafkaIngester[configurations.size ()];
        int i = 0;
        for ( Configuration configuration : configurations )
        {
            ingesters[i] = new KafkaIngester ( configuration );
            i++;
        }
    }

    public void start ()
    {
        if ( ingesters == null || ingesters.length == 0 )
        {
            throw new RuntimeException ( "no ingesters configured!" );
        }
        int i = 0;
        for ( KafkaIngester kafkaIngester : ingesters )
        {
            if ( kafkaIngester != null )
            {
                kafkaIngester.start ();
            }
            else
            {
                logger.error ( "Kafka Ingester No: {} was not set", i );
            }
            i++;
        }
    }

    public void stop ()
    {
        if ( ingesters == null || ingesters.length == 0 )
        {
            throw new RuntimeException ( "no ingesters configured!" );
        }
        int i = 0;
        for ( KafkaIngester kafkaIngester : ingesters )
        {
            if ( kafkaIngester != null )
            {
                kafkaIngester.stop ();
            }
            else
            {
                logger.error ( "Kafka Ingester No: {} was not set", i );
            }
            i++;
        }
    }
}
