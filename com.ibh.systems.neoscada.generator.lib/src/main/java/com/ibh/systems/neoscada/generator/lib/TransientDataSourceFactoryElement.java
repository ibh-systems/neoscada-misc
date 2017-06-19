package com.ibh.systems.neoscada.generator.lib;

public class TransientDataSourceFactoryElement extends AbstractFactoryElement
{
    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_TRANSIENT_ITEM_FACTORY;
    }

    public TransientDataSourceFactoryElement ( final String id )
    {
        super ( id );
    }
}
