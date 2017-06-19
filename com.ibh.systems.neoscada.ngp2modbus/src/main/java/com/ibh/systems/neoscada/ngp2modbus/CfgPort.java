package com.ibh.systems.neoscada.ngp2modbus;

import java.util.ArrayList;
import java.util.List;

public class CfgPort
{
    private String bindAddress = "0.0.0.0";

    private int port = 502;

    private List<CfgSlave> slaves = new ArrayList<> ();

    public String getBindAddress ()
    {
        return bindAddress;
    }

    public void setBindAddress ( String bindAddress )
    {
        this.bindAddress = bindAddress;
    }

    public int getPort ()
    {
        return port;
    }

    public void setPort ( int port )
    {
        this.port = port;
    }

    public List<CfgSlave> getSlaves ()
    {
        return slaves;
    }

    public void setSlaves ( List<CfgSlave> slaves )
    {
        this.slaves = slaves;
    }
}
