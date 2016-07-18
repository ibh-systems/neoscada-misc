package com.ibh.systems.neoscada.ngp2modbus;

import java.io.File;
import java.io.FileInputStream;

public class Application
{

    public static void main ( String[] args ) throws Throwable
    {

        // default configuration
        String configFile = "/etc/eclipsescada/ngp2modbus/ngp2modbus.json";

        // config file would be the first command line parameter
        if ( args.length > 0 )
        {
            configFile = args[0];
        }

        // is config file accessible at all?
        if ( !new File ( configFile ).canRead () )
        {
            System.err.println ( "config file '" + configFile + "' does not exists or can not be read!" );
            System.exit ( 1 );
        }

        // check if config file is valid
        try
        {
            FileInputStream is = new FileInputStream ( configFile );
            NgpToModbusConfiguration config = NgpToModbusConfiguration.read ( is );
            System.out.println ( config );
        }
        catch ( final Exception e )
        {
            e.printStackTrace ();
            System.exit ( 2 );
        }

        System.out.println ( "Staring Modbus converter with config file " + configFile );

        // Instantiate and start hive & driver
        System.setProperty ( "java.net.preferIPv4Stack", "true" );
        final Ngp2Modbus ngp2modbus = new Ngp2Modbus ( configFile );
        ngp2modbus.start ();
    }
}
