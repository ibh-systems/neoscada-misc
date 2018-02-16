package com.ibh.systems.neoscada.generator.lib;

public class Iec60870ConnectionFactoryElement extends AbstractFactoryElement
{

    private final String host;

    private final int port;

    private final String timeZone;
    
    private final Boolean delayStart;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_IEC_60870_CONNECTION_FACTORY;
    }

    public Iec60870ConnectionFactoryElement ( final String id, final String host, final int port )
    {
        super ( id );
        this.host = host;
        this.port = port;
        this.timeZone = null;
        this.delayStart = null;
    }

    public Iec60870ConnectionFactoryElement ( final String id, final String host, final int port, final String timeZone )
    {
        super ( id );
        this.host = host;
        this.port = port;
        this.timeZone = timeZone;
        this.delayStart = null;
    }

    public Iec60870ConnectionFactoryElement ( final String id, final String host, final int port, boolean delayStart )
    {
        super ( id );
        this.host = host;
        this.port = port;
        this.timeZone = null;
        this.delayStart = delayStart;
    }

    public Iec60870ConnectionFactoryElement ( final String id, final String host, final int port, final String timeZone, boolean delayStart )
    {
        super ( id );
        this.host = host;
        this.port = port;
        this.timeZone = timeZone;
        this.delayStart = delayStart;
    }

    public String getHost ()
    {
        return this.host;
    }

    public int getPort ()
    {
        return this.port;
    }

    public String getTimeZone ()
    {
        return this.timeZone;
    }
    
    public Boolean getDelayStart ()
    {
        return delayStart;
    }
}
