package com.ibh.systems.neoscada.generator.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ProxyDataSourceFactoryElement extends AbstractFactoryElement
{

    private final List<String> sources;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_PROXY_ITEM_FACTORY;
    }

    public ProxyDataSourceFactoryElement ( final String id, String... sources )
    {
        super ( id );
        this.sources = Arrays.asList ( sources );
    }

    public ProxyDataSourceFactoryElement ( final String id, Collection<String> sources )
    {
        super ( id );
        this.sources = new ArrayList<> ( sources );
    }

    public List<String> getValue ()
    {
        return new ArrayList<> ( this.sources );
    }
}
