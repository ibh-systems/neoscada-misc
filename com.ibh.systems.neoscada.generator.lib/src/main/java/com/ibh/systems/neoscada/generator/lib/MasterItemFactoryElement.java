package com.ibh.systems.neoscada.generator.lib;

public class MasterItemFactoryElement extends AbstractFactoryElement
{

    private final String dataSourceId;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MASTER_ITEM_FACTORY;
    }

    public MasterItemFactoryElement ( final String id, final String dataSourceId )
    {
        super ( id );
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceId ()
    {
        return this.dataSourceId;
    }
}
