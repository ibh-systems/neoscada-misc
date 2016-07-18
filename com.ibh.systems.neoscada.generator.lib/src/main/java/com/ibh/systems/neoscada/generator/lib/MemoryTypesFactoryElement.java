package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MemoryTypesFactoryElement extends AbstractFactoryElement
{

    private final Map<String, String> variables = new TreeMap<> ();

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MEMORY_TYPES_FACTORY;
    }

    public MemoryTypesFactoryElement ( final String id, final Map<String, String> variables )
    {
        super ( id );
        this.variables.putAll ( variables );

    }

    public Map<String, String> getVariables ()
    {
        return Collections.unmodifiableMap ( this.variables );
    }
}
