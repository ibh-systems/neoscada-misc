package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ModbusSlaveDeviceFactoryElement extends AbstractFactoryElement
{

    private final Map<String, String> blocks = new TreeMap<> ();

    private final int slaveId;

    private final String dataOrder;

    private final String masterId;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MODBUS_SLAVE_DEVICE_FACTORY;
    }

    public ModbusSlaveDeviceFactoryElement ( final String id, final String masterId, final int slaveId, final String dataOrder, final Map<String, String> blocks )
    {
        super ( id );
        this.masterId = masterId;
        this.slaveId = slaveId;
        this.dataOrder = dataOrder;
        this.blocks.putAll ( blocks );
    }

    public Map<String, String> getBlocks ()
    {
        return Collections.unmodifiableMap ( this.blocks );
    }

    public int getSlaveId ()
    {
        return this.slaveId;
    }

    public String getDataOrder ()
    {
        return this.dataOrder;
    }

    public String getMasterId ()
    {
        return this.masterId;
    }
}
