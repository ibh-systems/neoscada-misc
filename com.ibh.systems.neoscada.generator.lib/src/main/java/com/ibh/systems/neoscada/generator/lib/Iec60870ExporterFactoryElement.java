package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Iec60870ExporterFactoryElement extends AbstractFactoryElement
{

    private final int port;

    private final Map<String, String> exports = new TreeMap<> ();

    private final boolean floatWithTimestamps;

    private final boolean boolWithTimestamps;

    private final int t1;

    private final int t2;

    private final int t3;

    private final int k;

    private final int w;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_IEC_60870_EXPORTER_FACTORY;
    }

    public Iec60870ExporterFactoryElement ( final String id, final int port, final Map<String, String> exports )
    {
        this ( id, port, true, true, exports );
    }

    public Iec60870ExporterFactoryElement ( final String id, final int port, final boolean floatWithTimestamps, final boolean boolWithTimestamps, final Map<String, String> exports )
    {
        this ( id, port, floatWithTimestamps, boolWithTimestamps, 15000, 10000, 20000, 15, 10, exports );
    }

    public Iec60870ExporterFactoryElement ( final String id, final int port, final boolean floatWithTimestamps, final boolean boolWithTimestamps, final int t1, final int t2, final int t3, final int k, final int w, final Map<String, String> exports )
    {
        super ( id );
        this.port = port;
        this.floatWithTimestamps = floatWithTimestamps;
        this.boolWithTimestamps = boolWithTimestamps;
        this.exports.putAll ( exports );
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.k = k;
        this.w = w;
    }

    public int getPort ()
    {
        return this.port;
    }

    public boolean isFloatWithTimestamps ()
    {
        return this.floatWithTimestamps;
    }

    public boolean isBoolWithTimestamps ()
    {
        return this.boolWithTimestamps;
    }

    public int getT1 ()
    {
        return this.t1;
    }

    public int getT2 ()
    {
        return this.t2;
    }

    public int getT3 ()
    {
        return this.t3;
    }

    public int getK ()
    {
        return this.k;
    }

    public int getW ()
    {
        return this.w;
    }

    public Map<String, String> getExports ()
    {
        return Collections.unmodifiableMap ( this.exports );
    }
}
