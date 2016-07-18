package com.ibh.systems.neoscada.generator.lib;

public class ModbusMasterDeviceFactoryElement extends AbstractFactoryElement
{

    private final String host;

    private final int port;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MODBUS_MASTER_DEVICE_FACTORY;
    }

    public ModbusMasterDeviceFactoryElement ( final String id, final String host, final int port )
    {
        super ( id );
        this.host = host;
        this.port = port;
    }

    public String getHost ()
    {
        return this.host;
    }

    public int getPort ()
    {
        return this.port;
    }
}
