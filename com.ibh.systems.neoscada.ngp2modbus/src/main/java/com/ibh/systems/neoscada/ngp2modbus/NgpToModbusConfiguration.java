package com.ibh.systems.neoscada.ngp2modbus;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NgpToModbusConfiguration
{

    List<CfgPort> ports = new ArrayList<CfgPort> ();

    private NgpToModbusConfiguration ( InputStream is )
    {
    }

    public static NgpToModbusConfiguration read ( InputStream is )
    {
        Gson gson = new GsonBuilder ().create ();
        return gson.fromJson ( new BufferedReader ( new InputStreamReader ( is ) ), NgpToModbusConfiguration.class );
    }

    public List<CfgPort> getPorts ()
    {
        return ports;
    }

    public void setPorts ( List<CfgPort> ports )
    {
        this.ports = ports;
    }

    @Override
    public String toString ()
    {
        Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();
        return gson.toJson ( this );
    }
}
