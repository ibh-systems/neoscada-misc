package com.ibh.systems.neoscada.generator.lib;

public class HistoricalItemFactoryElement extends AbstractFactoryElement
{
    private final String dataSourceId;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_HISTORICAL_ITEM_FACTORY;
    }

    public HistoricalItemFactoryElement ( final String id, final String dataSourceId )
    {
        super ( id );
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceId ()
    {
        return this.dataSourceId;
    }

}
