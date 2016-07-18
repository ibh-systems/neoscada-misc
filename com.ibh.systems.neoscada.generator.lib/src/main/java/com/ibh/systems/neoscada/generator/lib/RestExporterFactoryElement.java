package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class RestExporterFactoryElement extends AbstractFactoryElement
{

    private final Map<String, String> exports = new TreeMap<> ();

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_REST_EXPORTER_FACTORY;
    }

    public RestExporterFactoryElement ( final String id, final Map<String, String> exports )
    {
        super ( id );
        this.exports.putAll ( exports );
    }

    public Map<String, String> getExports ()
    {
        return Collections.unmodifiableMap ( this.exports );
    }
}
