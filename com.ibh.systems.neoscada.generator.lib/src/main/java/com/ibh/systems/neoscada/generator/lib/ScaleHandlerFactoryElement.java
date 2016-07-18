package com.ibh.systems.neoscada.generator.lib;

public class ScaleHandlerFactoryElement extends AbstractFactoryElement
{

    private final String masterId;

    private final double factor;

    private final double offset;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_SCALE_HANDLER_FACTORY;
    }

    public ScaleHandlerFactoryElement ( final String id, final String masterId, final double factor, final double offset )
    {
        super ( id );
        this.masterId = masterId;
        this.factor = factor;
        this.offset = offset;
    }

    public String getMasterId ()
    {
        return this.masterId;
    }

    public double getFactor ()
    {
        return this.factor;
    }

    public double getOffset ()
    {
        return this.offset;
    }

    // "DE.EMS.BIOMASS.POOL_SUM_KW.V.local.scale": {
    // "info.level.1": "EMS",
    // "info.itemDescription": "",
    // "master.id": "DE.EMS.BIOMASS.POOL_SUM_KW.V.master",
    // "info.level.0": "DE",
    // "active": "true",
    // "factor": "1000.0",
    // "offset": "0.0",
    // "handlerPriority": "200",
    // "info.level.2": "BIOMASS",
    // "info.item": "DE.EMS.BIOMASS.POOL_SUM_KW.V"
    // },
}
