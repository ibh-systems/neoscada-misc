package com.ibh.systems.neoscada.generator.lib;

public class ManualHandlerFactoryElement extends AbstractFactoryElement
{

    private final String masterId;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MANUAL_HANDLER_FACTORY;
    }

    public ManualHandlerFactoryElement ( final String id, final String masterId )
    {
        super ( id );
        this.masterId = masterId;
    }

    public String getMasterId ()
    {
        return this.masterId;
    }
}
