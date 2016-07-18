package com.ibh.systems.neoscada.generator.lib;

public class PersistentDataSourceFactoryElement extends AbstractFactoryElement
{

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_PERSISTENT_ITEM_FACTORY;
    }

    public PersistentDataSourceFactoryElement ( final String id )
    {
        super ( id );
    }
}
