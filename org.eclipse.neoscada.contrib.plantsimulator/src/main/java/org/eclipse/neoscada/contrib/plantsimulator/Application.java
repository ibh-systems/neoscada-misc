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
package org.eclipse.neoscada.contrib.plantsimulator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application
{
    private final static Logger logger = LoggerFactory.getLogger ( Application.class );

    private static final String GENERATE = "GENERATE";

    private static final String RUN = "RUN";

    @Argument ( usage = "GENERATE | RUN" )
    private String argument = GENERATE;

    @Option ( name = "-p", aliases = "--port", usage = "start port" )
    private int port = 10000;

    @Option ( name = "-s", aliases = "--seed", usage = "seed for generation" )
    private int seed = 0;

    @Option ( name = "-n", aliases = "--number-of-plants", usage = "number of plants to generate" )
    private int numOfPlants = 1;

    @Option ( name = "-f", aliases = "--portfolio", usage = "plants to simulate" )
    private String portfolio;

    public static void main ( String[] args ) throws Exception
    {
        System.setProperty ( "java.net.preferIPv4Stack", "true" );
        new Application ().run ( args );
    }

    private void run ( String[] args ) throws Exception
    {
        CmdLineParser parser = new CmdLineParser ( this );

        try
        {
            parser.parseArgument ( args );
            switch ( this.argument )
            {
                case GENERATE:
                    new PlantConfigGenerator ().run ( seed, numOfPlants, port );
                    break;
                case RUN:
                    InputStream is;
                    if ( this.portfolio == null )
                    {
                        is = new GZIPInputStream ( getClass ().getClassLoader ().getResourceAsStream ( "portfolio.json.gz" ) );
                    }
                    else
                    {
                        is = new FileInputStream ( this.portfolio );
                    }
                    WeatherProvider wp = new WeatherProvider();
                    new PlantSimulator ().run ( wp, is, numOfPlants );
                    break;
                default:
                    parser.printUsage ( System.err );
            }
        }
        catch ( CmdLineException e )
        {
            System.err.println ( e.getMessage () );
            System.err.println ( "java -jar plantsimulator.jar [options...] arguments..." );
            // print the list of available options
            parser.printUsage ( System.err );
            System.err.println ();
        }
    }
}
