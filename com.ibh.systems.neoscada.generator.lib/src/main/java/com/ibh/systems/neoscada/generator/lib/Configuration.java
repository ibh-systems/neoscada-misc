package com.ibh.systems.neoscada.generator.lib;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ibh.systems.neoscada.generator.lib.ModbusExporterFactoryElement.ModbusItemDefinition;

public class Configuration
{
    private final Map<Factory, Map<String, FactoryElement>> factories = new TreeMap<> ();

    public Factory addElement ( final FactoryElement element )
    {
        Map<String, FactoryElement> factoryEntries = this.factories.get ( element.getFactory () );
        if ( factoryEntries == null )
        {
            factoryEntries = new TreeMap<> ();
            this.factories.put ( element.getFactory (), factoryEntries );
        }
        if ( factoryEntries.get ( element.getId () ) != null )
        {
            throw new IllegalArgumentException ( "Element with Id '" + element.getId () + "' already exists!" );
        }
        factoryEntries.put ( element.getId (), element );
        return element.getFactory ();
    }

    public static class ConfigurationSerializer implements JsonSerializer<Configuration>
    {
        @Override
        public JsonElement serialize ( final Configuration src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            for ( final Entry<Factory, Map<String, FactoryElement>> entry : src.factories.entrySet () )
            {
                object.add ( entry.getKey ().getId (), context.serialize ( entry.getValue () ) );
            }
            return object;
        }
    }

    public static class DaConnectionFactoryElementSerializer implements JsonSerializer<DaConnectionFactoryElement>
    {
        @Override
        public JsonElement serialize ( final DaConnectionFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "connection.uri", "" + src.getUri () );
            return object;
        }
    }

    public static class DataSourceDataItemFactoryElementSerializer implements JsonSerializer<DataItemImportFactoryElement>
    {
        @Override
        public JsonElement serialize ( final DataItemImportFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "connection.id", src.getConnectionId () );
            object.addProperty ( "item.id", "" + src.getItemId () );
            return object;
        }
    }

    public static class ConstantDataSourceFactoryElementSerializer implements JsonSerializer<ConstantDataSourceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ConstantDataSourceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "value", "" + src.getValue () );
            return object;
        }
    }

    public static class TransientDataSourceFactoryElementSerializer implements JsonSerializer<TransientDataSourceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final TransientDataSourceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            return object;
        }
    }

    public static class PersistentDataSourceFactoryElementSerializer implements JsonSerializer<PersistentDataSourceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final PersistentDataSourceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            return object;
        }
    }

    public static class ScriptDataSourceFactoryElementSerializer implements JsonSerializer<ScriptDataSourceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ScriptDataSourceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            if ( src.getInit () != null )
            {
                object.addProperty ( "init", "" + src.getInit () );
            }
            if ( src.getUpdateCommand () != null )
            {
                object.addProperty ( "updateCommand", "" + src.getUpdateCommand () );
            }
            if ( src.getTimerCommand () != null )
            {
                object.addProperty ( "timerCommand", "" + src.getTimerCommand () );
            }
            if ( src.getWriteCommand () != null )
            {
                object.addProperty ( "writeCommand", "" + src.getWriteCommand () );
            }
            if ( src.getTimer () != null )
            {
                object.addProperty ( "timer", "" + src.getTimer () );
            }
            object.addProperty ( "engine", "" + src.getEngine () );
            for ( final Entry<String, String> entry : src.getDataSources ().entrySet () )
            {
                object.addProperty ( "datasource." + entry.getKey (), entry.getValue () );
            }
            for ( final Entry<String, String> entry : src.getWriteSources ().entrySet () )
            {
                object.addProperty ( "writeSource." + entry.getKey (), entry.getValue () );
            }
            return object;
        }
    }

    public static class Iec60870ConnectionFactoryElementSerializer implements JsonSerializer<Iec60870ConnectionFactoryElement>
    {
        @Override
        public JsonElement serialize ( final Iec60870ConnectionFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "host", src.getHost () );
            object.addProperty ( "port", "" + src.getPort () );
            if ( src.getTimeZone () != null )
            {
                object.addProperty ( "protocol.timeZone", "" + src.getTimeZone () );
            }
            return object;
        }
    }

    public static class Iec60870ExporterFactoryElementSerializer implements JsonSerializer<Iec60870ExporterFactoryElement>
    {
        @Override
        public JsonElement serialize ( final Iec60870ExporterFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "port", "" + src.getPort () );
            object.addProperty ( "hive.user", "interconnect" );
            object.addProperty ( "hive.password", "interconnect12!" );
            object.addProperty ( "timeZone", "Europe/Berlin" );
            object.addProperty ( "ignoreDaylightSavingTime", "false" );
            object.addProperty ( "withTimestamp.float", "" + src.isFloatWithTimestamps () );
            object.addProperty ( "withTimestamp.boolean", "" + src.isBoolWithTimestamps () );
            object.addProperty ( "asduAddressType", "SIZE_2" );
            object.addProperty ( "informationObjectAddressType", "SIZE_3" );
            object.addProperty ( "causeOfTransmissionType", "SIZE_2" );
            object.addProperty ( "backgroundScanPeriod", "60000" );
            object.addProperty ( "spontaneousItemBuffer", "100" );
            object.addProperty ( "t1", "" + src.getT1 () );
            object.addProperty ( "t2", "" + src.getT2 () );
            object.addProperty ( "t3", "" + src.getT3 () );
            object.addProperty ( "k", "" + src.getK () );
            object.addProperty ( "w", "" + src.getW () );
            for ( final Entry<String, String> entry : src.getExports ().entrySet () )
            {
                object.addProperty ( "entry." + entry.getKey (), entry.getValue () );
            }
            return object;
        }
    }

    public static class ModbusExporterFactoryElementSerializer implements JsonSerializer<ModbusExporterFactoryElement>
    {
        static final DecimalFormat df = new DecimalFormat ( "0" );
        static
        {
            df.setMaximumFractionDigits ( 340 );
        }

        @Override
        public JsonElement serialize ( final ModbusExporterFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "port", "" + src.getPort () );
            object.addProperty ( "hive.user", "interconnect" );
            object.addProperty ( "hive.password", "interconnect12!" );
            object.addProperty ( "dataOrder", src.getDataOrder () );
            object.addProperty ( "slaveId", "" + src.getSlaveId () );
            for ( final Entry<String, ModbusItemDefinition> entry : src.getItems ().entrySet () )
            {
                if ( entry.getValue ().getScale () == null )
                {
                    object.addProperty ( "item." + entry.getKey (), String.format ( "%d:%s", entry.getValue ().getAddress (), entry.getValue ().getType () ) );
                }
                else
                {
                    object.addProperty ( "item." + entry.getKey (), String.format ( Locale.US, "%d:%s:%s", entry.getValue ().getAddress (), entry.getValue ().getType (), df.format ( entry.getValue ().getScale () ) ) );
                }
            }
            return object;
        }
    }

    public static class RestExporterFactoryElementSerializer implements JsonSerializer<RestExporterFactoryElement>
    {
        @Override
        public JsonElement serialize ( final RestExporterFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            for ( final Entry<String, String> entry : src.getExports ().entrySet () )
            {
                object.addProperty ( "items." + entry.getKey (), entry.getValue () );
            }
            return object;
        }
    }

    public static class OpcXmlDaConnectionFactoryElementSerializer implements JsonSerializer<OpcXmlDaConnectionFactoryElement>
    {
        @Override
        public JsonElement serialize ( final OpcXmlDaConnectionFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "url", src.getUrl () );
            object.addProperty ( "portName", "" + src.getPortName () );
            object.addProperty ( "serviceName", "" + src.getServiceName () );

            if ( src.isPolling () )
            {
                object.addProperty ( "pollByRead", "true" );
                object.addProperty ( "maxAge", "" + src.getWaitTime () );
                object.addProperty ( "period", "" + src.getSamplingRate () );
            }
            else
            {
                object.addProperty ( "samplingRate", "" + src.getSamplingRate () );
                object.addProperty ( "waitTime", "" + src.getWaitTime () );
            }

            object.addProperty ( "timeout", "" + src.getTimeOut () );
            object.addProperty ( "wsdlUrl", "" + src.getWsdlUrl () );
            return object;
        }
    }

    public static class MasterItemFactoryElementSerializer implements JsonSerializer<MasterItemFactoryElement>
    {
        @Override
        public JsonElement serialize ( final MasterItemFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "datasource.id", src.getDataSourceId () );
            return object;
        }
    }

    public static class DataItemDataSourceFactoryElementSerializer implements JsonSerializer<DataItemExportFactoryElement>
    {
        @Override
        public JsonElement serialize ( final DataItemExportFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "datasource.id", src.getDataSourceId () );
            object.addProperty ( "item.id", "" + src.getItemId () );
            object.addProperty ( "io.directions", "" + src.getIoDirections () );
            return object;
        }
    }

    public static class ScaleHandlerFactoryElementSerializer implements JsonSerializer<ScaleHandlerFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ScaleHandlerFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "master.id", src.getMasterId () );
            object.addProperty ( "factor", "" + src.getFactor () );
            object.addProperty ( "offset", "" + src.getOffset () );
            object.addProperty ( "active", "true" );
            object.addProperty ( "handlerPriority", "200" );
            return object;
        }
    }

    public static class ManualHandlerFactoryElementSerializer implements JsonSerializer<ManualHandlerFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ManualHandlerFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "master.id", src.getMasterId () );
            object.addProperty ( "handlerPriority", "500" );
            object.addProperty ( "active", "false" );
            return object;
        }
    }

    public static class ItemSummaryHandlerFactoryElementSerializer implements JsonSerializer<ItemSummaryHandlerFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ItemSummaryHandlerFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "master.id", src.getMasterId () );
            object.addProperty ( "handlerPriority", src.getHandlerPriority () );
            object.addProperty ( "tag", Joiner.on ( ", " ).join ( src.getTags () ) );
            for ( final String tag : src.getTags () )
            {
                object.addProperty ( "tag." + tag + ".prefix", src.getPrefix () );
            }
            return object;
        }
    }

    public static class ModbusSlaveDeviceFactoryElementSerializer implements JsonSerializer<ModbusSlaveDeviceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ModbusSlaveDeviceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "modbus.master.id", src.getMasterId () );
            object.addProperty ( "slave.id", "" + src.getSlaveId () );
            object.addProperty ( "dataOrder", src.getDataOrder () );
            for ( final Entry<String, String> block : src.getBlocks ().entrySet () )
            {
                object.addProperty ( "block." + block.getKey (), block.getValue () );
            }
            return object;
        }
    }

    public static class ModbusMasterDeviceFactoryElementSerializer implements JsonSerializer<ModbusMasterDeviceFactoryElement>
    {
        @Override
        public JsonElement serialize ( final ModbusMasterDeviceFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "host", src.getHost () );
            object.addProperty ( "port", "" + src.getPort () );
            object.addProperty ( "protocolType", "TCP" );
            object.addProperty ( "readTimeout", "" + src.getReadTimeout () );
            return object;
        }
    }

    public static class MemoryTypesFactoryElementSerializer implements JsonSerializer<MemoryTypesFactoryElement>
    {
        @Override
        public JsonElement serialize ( final MemoryTypesFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            for ( final Entry<String, String> entry : src.getVariables ().entrySet () )
            {
                object.addProperty ( "variable." + entry.getKey (), entry.getValue () );
            }
            return object;
        }
    }

    public static class SecOsgiManagerFactoryElementSerializer implements JsonSerializer<SecOsgiManagerFactoryElement>
    {
        @Override
        public JsonElement serialize ( final SecOsgiManagerFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "serviceType", "script" );
            object.addProperty ( "priority", "" + src.getPriority () );
            object.addProperty ( "properties.script.engine", "JavaScript" );
            object.addProperty ( "properties.script", src.getScript () );
            return object;
        }
    }

    public static class HistoricalItemFactoryElementSerializer implements JsonSerializer<HistoricalItemFactoryElement>
    {
        @Override
        public JsonElement serialize ( final HistoricalItemFactoryElement src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject object = new JsonObject ();
            object.addProperty ( "datasource.id", src.getDataSourceId () );
            return object;
        }
    }

    public Gson getGson ()
    {
        return new GsonBuilder ().setPrettyPrinting () //
                .registerTypeAdapter ( Configuration.class, new ConfigurationSerializer () ) //
                .registerTypeAdapter ( DaConnectionFactoryElement.class, new DaConnectionFactoryElementSerializer () ) //
                .registerTypeAdapter ( DataItemImportFactoryElement.class, new DataSourceDataItemFactoryElementSerializer () ) //
                .registerTypeAdapter ( HistoricalItemFactoryElement.class, new HistoricalItemFactoryElementSerializer () ) //
                .registerTypeAdapter ( MasterItemFactoryElement.class, new MasterItemFactoryElementSerializer () ) //
                .registerTypeAdapter ( ConstantDataSourceFactoryElement.class, new ConstantDataSourceFactoryElementSerializer () )//
                .registerTypeAdapter ( TransientDataSourceFactoryElement.class, new TransientDataSourceFactoryElementSerializer () )//
                .registerTypeAdapter ( ScriptDataSourceFactoryElement.class, new ScriptDataSourceFactoryElementSerializer () ) //
                .registerTypeAdapter ( ScaleHandlerFactoryElement.class, new ScaleHandlerFactoryElementSerializer () ) //
                .registerTypeAdapter ( ManualHandlerFactoryElement.class, new ManualHandlerFactoryElementSerializer () ) //
                .registerTypeAdapter ( ItemSummaryHandlerFactoryElement.class, new ItemSummaryHandlerFactoryElementSerializer () ) //
                .registerTypeAdapter ( DataItemExportFactoryElement.class, new DataItemDataSourceFactoryElementSerializer () ) //
                .registerTypeAdapter ( RestExporterFactoryElement.class, new RestExporterFactoryElementSerializer () ) //
                .registerTypeAdapter ( Iec60870ExporterFactoryElement.class, new Iec60870ExporterFactoryElementSerializer () ) //
                .registerTypeAdapter ( Iec60870ConnectionFactoryElement.class, new Iec60870ConnectionFactoryElementSerializer () ) //
                .registerTypeAdapter ( ModbusExporterFactoryElement.class, new ModbusExporterFactoryElementSerializer () ) //
                .registerTypeAdapter ( ModbusMasterDeviceFactoryElement.class, new ModbusMasterDeviceFactoryElementSerializer () ) //
                .registerTypeAdapter ( ModbusSlaveDeviceFactoryElement.class, new ModbusSlaveDeviceFactoryElementSerializer () ) //
                .registerTypeAdapter ( MemoryTypesFactoryElement.class, new MemoryTypesFactoryElementSerializer () ) //
                .registerTypeAdapter ( OpcXmlDaConnectionFactoryElement.class, new OpcXmlDaConnectionFactoryElementSerializer () ) //
                .registerTypeAdapter ( SecOsgiManagerFactoryElement.class, new SecOsgiManagerFactoryElementSerializer () ) //
                .create ();
    }
}
