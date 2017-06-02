package com.ibh.systems.neoscada.iec104example;

import java.util.Arrays;

import org.eclipse.neoscada.protocol.iec60870.server.Server;
import org.eclipse.neoscada.protocol.iec60870.server.data.DataModel;
import org.eclipse.neoscada.protocol.iec60870.server.data.DataModule;
import org.eclipse.neoscada.protocol.iec60870.server.data.DataModuleOptions;

public class TestSlave
{
    @SuppressWarnings ( "resource" )
    public static void main ( String[] args )
    {
        DataModuleOptions options = new DataModuleOptions.Builder ().build ();
        DataModel dataModel = new SineDataModel ( 5 );
        DataModule d = new DataModule ( options, dataModel );
        new Server ( 2404, Arrays.asList ( d ) );
    }
}
