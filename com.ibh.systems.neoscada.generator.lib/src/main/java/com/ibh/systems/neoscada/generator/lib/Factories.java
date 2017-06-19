package com.ibh.systems.neoscada.generator.lib;

public class Factories
{

    public static final DaConnectionFactory DEFAULT_DA_CONNECTION_FACTORY = new DaConnectionFactory ();

    public static final DataSourceDataItemFactory DEFAULT_DATA_SOURCE_DATA_ITEM_FACTORY = new DataSourceDataItemFactory ();

    public static final HistoricalItemFactory DEFAULT_HISTORICAL_ITEM_FACTORY = new HistoricalItemFactory ();

    public static final Iec60870ConnectionFactory DEFAULT_IEC_60870_CONNECTION_FACTORY = new Iec60870ConnectionFactory ();

    public static final Iec60870ExporterFactory DEFAULT_IEC_60870_EXPORTER_FACTORY = new Iec60870ExporterFactory ();

    public static final ModbusExporterFactory DEFAULT_MODBUS_EXPORTER_FACTORY = new ModbusExporterFactory ();

    public static final RestExporterFactory DEFAULT_REST_EXPORTER_FACTORY = new RestExporterFactory ();

    public static final OpcXmlDaConnectionFactory DEFAULT_OPCXMLDA_CONNECTION_FACTORY = new OpcXmlDaConnectionFactory ();

    public static final MasterItemFactory DEFAULT_MASTER_ITEM_FACTORY = new MasterItemFactory ();

    public static final ConstantItemFactory DEFAULT_CONSTANT_ITEM_FACTORY = new ConstantItemFactory ();

    public static final PersistentItemFactory DEFAULT_PERSISTENT_ITEM_FACTORY = new PersistentItemFactory ();

    public static final TransientItemFactory DEFAULT_TRANSIENT_ITEM_FACTORY = new TransientItemFactory ();

    public static final ScriptItemFactory DEFAULT_SCRIPT_ITEM_FACTORY = new ScriptItemFactory ();

    public static final DataItemDataSourceFactory DEFAULT_DATA_ITEM_DATA_SOURCE_FACTORY = new DataItemDataSourceFactory ();

    public static final ScaleHandlerFactory DEFAULT_SCALE_HANDLER_FACTORY = new ScaleHandlerFactory ();

    public static final ManualHandlerFactory DEFAULT_MANUAL_HANDLER_FACTORY = new ManualHandlerFactory ();

    public static final ItemSummaryHandlerFactory DEFAULT_ITEM_SUMMARY_HANDLER_FACTORY = new ItemSummaryHandlerFactory ();

    public static final ModbusMasterDeviceFactory DEFAULT_MODBUS_MASTER_DEVICE_FACTORY = new ModbusMasterDeviceFactory ();

    public static final ModbusSlaveDeviceFactory DEFAULT_MODBUS_SLAVE_DEVICE_FACTORY = new ModbusSlaveDeviceFactory ();

    public static final MemoryTypesFactory DEFAULT_MEMORY_TYPES_FACTORY = new MemoryTypesFactory ();

    public static final SecOsgiManagerFactory DEFAULT_SEC_OSGI_MANAGER_FACTORY = new SecOsgiManagerFactory ();

    public static class DaConnectionFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "da.connection";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class DataSourceDataItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "da.datasource.dataitem";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class Iec60870ConnectionFactory extends AbstractFactory
    {
        public static final String FACTORY_NAME = "org.openscada.da.server.iec60870.connection";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }

    }

    public static class Iec60870ExporterFactory extends AbstractFactory
    {
        public static final String FACTORY_NAME = "org.openscada.da.server.exporter.iec60870.device";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }

    }

    public static class ModbusExporterFactory extends AbstractFactory
    {
        public static final String FACTORY_NAME = "org.openscada.da.server.exporter.modbus.device";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }

    }

    public static class RestExporterFactory extends AbstractFactory
    {
        public static final String FACTORY_NAME = "org.eclipse.scada.da.server.exporter.rest.context";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }

    }

    public static class OpcXmlDaConnectionFactory extends AbstractFactory
    {
        public static final String FACTORY_NAME = "org.openscada.da.server.opc.xmlda.server";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }

    }

    public static class MasterItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "master.item";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ConstantItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.datasource.constant";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class PersistentItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.datasource.ds";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class TransientItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.datasource.memory";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ScriptItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.datasource.script";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class DataItemDataSourceFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "da.dataitem.datasource";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class HistoricalItemFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "historical.item.factory";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ScaleHandlerFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.scale.input";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ManualHandlerFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.manual";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ItemSummaryHandlerFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "da.master.handler.sum";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ModbusMasterDeviceFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.server.osgi.modbus.masterDevice";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class ModbusSlaveDeviceFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.server.osgi.modbus.slaveDevice";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class MemoryTypesFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.da.server.common.memory.types";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

    public static class SecOsgiManagerFactory extends AbstractFactory
    {

        public static final String FACTORY_NAME = "org.eclipse.scada.sec.osgi.manager";

        @Override
        public String getId ()
        {
            return FACTORY_NAME;
        }
    }

}
