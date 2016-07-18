package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ModbusExporterFactoryElement extends AbstractFactoryElement
{

    private final int port;

    private final int slaveId;

    private final String dataOrder;

    private final Map<String, ModbusItemDefinition> items = new TreeMap<> ();

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_MODBUS_EXPORTER_FACTORY;
    }

    public ModbusExporterFactoryElement ( final String id, final int port, final int slaveId, final String dataOrder, final Map<String, ModbusItemDefinition> items )
    {
        super ( id );
        this.port = port;
        this.slaveId = slaveId;
        this.dataOrder = dataOrder;
        this.items.putAll ( items );
    }

    public ModbusExporterFactoryElement ( final String id, final int port, final int slaveId, final Map<String, ModbusItemDefinition> items )
    {
        super ( id );
        this.port = port;
        this.slaveId = slaveId;
        this.dataOrder = "BIG_ENDIAN";
        this.items.putAll ( items );
    }

    public int getPort ()
    {
        return this.port;
    }

    public int getSlaveId ()
    {
        return this.slaveId;
    }

    public String getDataOrder ()
    {
        return this.dataOrder;
    }

    public Map<String, ModbusItemDefinition> getItems ()
    {
        return Collections.unmodifiableMap ( this.items );
    }

    public static ModbusItemDefinition createItemDefinition ( final int address, final String type )
    {
        return new ModbusItemDefinition ( address, type, null );
    }

    public static ModbusItemDefinition createItemDefinition ( final int address, final String type, final Double scale )
    {
        return new ModbusItemDefinition ( address, type, scale );
    }

    public static class ModbusItemDefinition implements Comparable<ModbusItemDefinition>
    {
        private final int address;

        private final String type;

        private final Double scale;

        private ModbusItemDefinition ( final int address, final String type, final Double scale )
        {
            this.address = address;
            this.type = type;
            this.scale = scale;
        }

        public int getAddress ()
        {
            return this.address;
        }

        public String getType ()
        {
            return this.type;
        }

        public Double getScale ()
        {
            return this.scale;
        }

        @Override
        public int compareTo ( final ModbusItemDefinition o )
        {
            return Integer.compare ( this.address, o.address );
        }
    }
}
