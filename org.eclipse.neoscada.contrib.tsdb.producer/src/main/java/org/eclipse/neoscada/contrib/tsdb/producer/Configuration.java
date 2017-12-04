package org.eclipse.neoscada.contrib.tsdb.producer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int queueSize = 10000;

    private int heartBeat = 0; // in seconds
    
    private List<ConnectionConfiguration> connections = new ArrayList<> ();

    public int getHeartBeat ()
    {
        return heartBeat;
    }

    public void setHeartBeat ( int heartBeat )
    {
        this.heartBeat = heartBeat;
    }

    public void setQueueSize ( int queueSize )
    {
        this.queueSize = queueSize;
    }

    public int getQueueSize ()
    {
        return queueSize;
    }
    
    public List<ConnectionConfiguration> getConnections ()
    {
        return connections;
    }
    
    public void setConnections ( List<ConnectionConfiguration> connections )
    {
        this.connections = connections;
    }
}
