package com.ibh.systems.neoscada.generator.lib;

public class DataItemExportFactoryElement extends AbstractFactoryElement
{

    private final String itemId;

    private final String dataSourceId;

    private final String ioDirections;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_DATA_ITEM_DATA_SOURCE_FACTORY;
    }

    public DataItemExportFactoryElement ( final String id, final String dataSourceId, final String itemId, final String ioDirections )
    {
        super ( id );
        this.dataSourceId = dataSourceId;
        this.itemId = itemId;
        this.ioDirections = ioDirections;
    }

    public String getItemId ()
    {
        return this.itemId;
    }

    public String getDataSourceId ()
    {
        return this.dataSourceId;
    }

    public String getIoDirections ()
    {
        return this.ioDirections;
    }
}
