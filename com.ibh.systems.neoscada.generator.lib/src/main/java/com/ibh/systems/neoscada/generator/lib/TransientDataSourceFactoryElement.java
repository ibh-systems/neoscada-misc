package com.ibh.systems.neoscada.generator.lib;

public class TransientDataSourceFactoryElement extends AbstractFactoryElement
{

    private final String value;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_TRANSIENT_ITEM_FACTORY;
    }

    public TransientDataSourceFactoryElement ( final String id, final String value )
    {
        super ( id );
        this.value = value;
    }

    public String getValue ()
    {
        return this.value;
    }
}
