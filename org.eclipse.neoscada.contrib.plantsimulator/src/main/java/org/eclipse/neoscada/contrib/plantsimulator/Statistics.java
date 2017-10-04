package org.eclipse.neoscada.contrib.plantsimulator;

import java.util.concurrent.atomic.AtomicInteger;

public class Statistics
{
    private AtomicInteger numberOfIecServers = new AtomicInteger ( 0 );

    public AtomicInteger getNumberOfIecServers ()
    {
        return numberOfIecServers;
    }
}
