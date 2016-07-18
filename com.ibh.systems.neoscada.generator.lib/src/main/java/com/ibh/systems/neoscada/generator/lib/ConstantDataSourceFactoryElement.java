package com.ibh.systems.neoscada.generator.lib;

public class ConstantDataSourceFactoryElement extends AbstractFactoryElement
{

    private final String value;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_CONSTANT_ITEM_FACTORY;
    }

    public ConstantDataSourceFactoryElement ( final String id, final String value )
    {
        super ( id );
        this.value = value;
    }

    public String getValue ()
    {
        return this.value;
    }
}
