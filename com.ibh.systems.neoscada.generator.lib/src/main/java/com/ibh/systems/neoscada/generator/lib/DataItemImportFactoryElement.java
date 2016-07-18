package com.ibh.systems.neoscada.generator.lib;

public class DataItemImportFactoryElement extends AbstractFactoryElement
{

    private final String connectionId;

    private final String itemId;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_DATA_SOURCE_DATA_ITEM_FACTORY;
    }

    public DataItemImportFactoryElement ( final String id, final String connectionId, final String itemId )
    {
        super ( id );
        this.connectionId = connectionId;
        this.itemId = itemId;
    }

    public String getConnectionId ()
    {
        return this.connectionId;
    }

    public String getItemId ()
    {
        return this.itemId;
    }
}
