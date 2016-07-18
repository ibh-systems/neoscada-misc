package com.ibh.systems.neoscada.ngp2modbus;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class JsvcStarter implements Daemon
{

    private Ngp2Modbus m;

    @Override
    public void destroy ()
    {
        m.stop ();
        m = null;
    }

    @Override
    public void init ( DaemonContext daemonContext ) throws DaemonInitException, Exception
    {
        String configFile = "/etc/eclipsescada/ngp2modbus/ngp2modbus.json";

        // config file would be the first command line parameter
        if ( daemonContext.getArguments ().length > 0 )
        {
            configFile = daemonContext.getArguments ()[0];
        }

        // is config file accessible at all?
        if ( !new File ( configFile ).canRead () )
        {
            String msg = "config file '" + configFile + "' does not exists or can not be read!";
            System.err.println ( msg );
            throw new DaemonInitException ( msg );
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
            throw new DaemonInitException ( "config validation failed", e );
        }

        System.out.println ( "Staring Modbus converter with config file " + configFile );
        m = new Ngp2Modbus ( configFile );
    }

    @Override
    public void start () throws Exception
    {
        m.start ();
    }

    @Override
    public void stop () throws Exception
    {
        m.stop ();
    }
}
