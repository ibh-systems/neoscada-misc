package com.ibh.systems.neoscada.generator.lib;

import java.net.URI;

public class DaConnectionFactoryElement extends AbstractFactoryElement
{

    private final URI uri;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_DA_CONNECTION_FACTORY;
    }

    public DaConnectionFactoryElement ( final String id, final URI uri )
    {
        super ( id );
        this.uri = uri;
    }

    public URI getUri ()
    {
        return this.uri;
    }
}
