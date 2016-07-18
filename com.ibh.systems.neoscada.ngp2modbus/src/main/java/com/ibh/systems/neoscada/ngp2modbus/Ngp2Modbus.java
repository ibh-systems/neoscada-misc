package com.ibh.systems.neoscada.ngp2modbus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.eclipse.scada.core.Variant;
import org.eclipse.scada.da.client.DataItem;
import org.eclipse.scada.da.client.DataItemValue;
import org.eclipse.scada.da.core.WriteResult;
import org.eclipse.scada.protocol.modbus.slave.AbstractDataSlave;
import org.eclipse.scada.protocol.modbus.slave.AnalogType;
import org.eclipse.scada.protocol.modbus.slave.DigitalType;
import org.eclipse.scada.protocol.modbus.slave.ModbusRequestException;
import org.eclipse.scada.protocol.modbus.slave.ProtocolOptions;
import org.eclipse.scada.protocol.modbus.slave.SlaveHost;
import org.eclipse.scada.protocol.modbus.slave.SlaveHost.SlaveHostCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ngp2Modbus
{
    private final static Logger logger = LoggerFactory.getLogger ( Ngp2Modbus.class );

    private final SlaveHostCustomizer customizer = new SlaveHostCustomizer () {

        @Override
        public void customizeFilterChain ( final DefaultIoFilterChainBuilder filterChain )
        {
            filterChain.addFirst ( "logger", new LoggingFilter () );
        }
    };

    private Map<Integer, SlaveHost> slaveHosts = new TreeMap<> ();

    // contains "PORT:SLAVE:(H|I|C|D):ADDRESS" as key, dataitem is the value 
    private final Map<String, String> address2Item = new TreeMap<> ();

    private String configFile;

    private ItemAndConnectionManager icm;

    public Ngp2Modbus ( String configFile ) throws Exception
    {
        this.configFile = configFile;
        Class.forName ( "org.eclipse.scada.da.client.ngp.ConnectionImpl" );
    }

    void start () throws Exception
    {
        logger.debug ( "try to read configuration from {}", this.configFile );
        NgpToModbusConfiguration config = NgpToModbusConfiguration.read ( new BufferedInputStream ( new FileInputStream ( new File ( this.configFile ) ) ) );
        logger.trace ( "finished reading configuration" );
        icm = new ItemAndConnectionManager ();
        // first, setup items
        logger.trace ( "iterating over ports" );
        for ( final CfgPort port : config.getPorts () )
        {
            ProtocolOptions options = new ProtocolOptions ();
            InetSocketAddress address = new InetSocketAddress ( port.getBindAddress (), port.getPort () );
            logger.trace ( "create new slave on address {}", address );
            SlaveHost slaveHost = new SlaveHost ( options, customizer, address );
            slaveHosts.put ( port.getPort (), slaveHost );
            logger.trace ( "slave on address {} created", address );
            for ( final CfgSlave slave : port.getSlaves () )
            {
                logger.trace ( "register new slave with id {}", slave.getSlaveId () );
                slaveHost.registerSlave ( new AbstractDataSlave () {
                    @Override
                    protected boolean[] handleReadDigital ( DigitalType type, int startAddress, int quantity ) throws ModbusRequestException
                    {
                        logger.trace ( "handleReadDigital called with type {}, start address {} and {} registers", type, startAddress, quantity );
                        boolean[] result = new boolean[quantity];
                        for ( int i = 0; i < quantity; i++ )
                        {
                            String key = port.getPort () + ":" + slave.getSlaveId () + ":" + ( type == DigitalType.COIL ? "C" : "D" ) + ":" + ( startAddress + i );
                            logger.trace ( "handleReadDigital calculated key {}", key );
                            DataItem item = icm.getItem ( address2Item.get ( key ) );
                            if ( item != null )
                            {
                                logger.trace ( "handleReadDigital got item {} for key {}", item.getItemId (), key );
                                DataItemValue div = item.getSnapshotValue ();
                                logger.trace ( "handleReadDigital item {} for key {} has value {}", item.getItemId (), key, div );
                                if ( div.isConnected () && !div.isError () )
                                {
                                    result[i] = div.getValue ().asBoolean ( false );
                                }
                            }
                            else
                            {
                                logger.trace ( "handleReadDigital no DataItem found for key {}", key );
                            }
                        }
                        return result;
                    }

                    @Override
                    protected int[] handleReadAnalog ( AnalogType type, int startAddress, int quantity ) throws ModbusRequestException
                    {
                        logger.trace ( "handleReadAnalog called with type {}, start address {} and {} registers", type, startAddress, quantity );
                        int[] result = new int[quantity];
                        for ( int i = 0; i < quantity; i++ )
                        {
                            String key = port.getPort () + ":" + slave.getSlaveId () + ":" + ( type == AnalogType.HOLDING ? "H" : "I" ) + ":" + ( startAddress + i );
                            logger.trace ( "handleReadAnalog calculated key {}", key );
                            DataItem item = icm.getItem ( address2Item.get ( key ) );
                            if ( item != null )
                            {
                                logger.trace ( "handleReadAnalog got item {} for key {}", item.getItemId (), key );
                                DataItemValue div = item.getSnapshotValue ();
                                logger.trace ( "handleReadAnalog item {} for key {} has value {}", item.getItemId (), key, div );
                                if ( div.isConnected () && !div.isError () )
                                {
                                    result[i] = div.getValue ().asInteger ( 0 );
                                }
                            }
                            else
                            {
                                logger.trace ( "handleReadAnalog no DataItem found for key {}", key );
                            }
                        }
                        return result;
                    }

                    @Override
                    protected void handleDigitalWrite ( int address, boolean[] values ) throws ModbusRequestException
                    {
                        logger.trace ( "handleDigitalWrite called with address {} and values {}", address, values );
                        try
                        {
                            for ( int i = 0; i < values.length; i++ )
                            {
                                String key = port.getPort () + ":" + slave.getSlaveId () + ":C:" + ( address + i );
                                logger.trace ( "handleDigitalWrite calculated key {}", key );
                                String itemUri = address2Item.get ( key );
                                if ( itemUri != null )
                                {
                                    logger.trace ( "handleDigitalWrite key {} resulted in itemId {}", key, itemUri );
                                    DataItem dataItem = icm.getItem ( itemUri );
                                    if ( dataItem != null )
                                    {
                                        logger.trace ( "handleDigitalWrite key {} resulted in item {}", key, dataItem.getItemId () );
                                        WriteResult result = icm.getConnection ( itemUri ).startWrite ( dataItem.getItemId (), Variant.valueOf ( values[i] ), null, null ).get ();
                                        if ( result.isError () )
                                        {
                                            logger.warn ( "handleDigitalWrite write on item {} resulted in error", dataItem, result.getError () );
                                            throw new ModbusRequestException ( 4, result.getError () );
                                        }
                                        logger.debug ( "handleDigitalWrite write on item {} completed", dataItem );
                                    }
                                    else
                                    {
                                        logger.warn ( "handleDigitalWrite key {} found no item, raising exception", key );
                                        throw new ModbusRequestException ( 2 );
                                    }
                                }
                                else
                                {
                                    logger.warn ( "handleDigitalWrite key {} found no itemId, raising exception", key );
                                    throw new ModbusRequestException ( 2 );
                                }
                            }
                        }
                        catch ( ModbusRequestException m )
                        {
                            throw m;
                        }
                        catch ( Exception e )
                        {
                            logger.error ( "handleDigitalWrite caused exception", e );
                            throw new ModbusRequestException ( 4, e );
                        }
                    }

                    @Override
                    protected void handleAnalogWrite ( int address, int[] values ) throws ModbusRequestException
                    {
                        logger.trace ( "handleAnalogWrite called with address {} and values {}", address, values );
                        try
                        {
                            for ( int i = 0; i < values.length; i++ )
                            {
                                String key = port.getPort () + ":" + slave.getSlaveId () + ":H:" + ( address + i );
                                logger.trace ( "handleAnalogWrite calculated key {}", key );
                                String itemUri = address2Item.get ( key );
                                if ( itemUri != null )
                                {
                                    logger.trace ( "handleAnalogWrite key {} resulted in itemId {}", key, itemUri );
                                    DataItem dataItem = icm.getItem ( itemUri );
                                    if ( dataItem != null )
                                    {
                                        logger.trace ( "handleAnalogWrite key {} resulted in item {}", key, dataItem.getItemId () );
                                        WriteResult result = icm.getConnection ( itemUri ).startWrite ( dataItem.getItemId (), Variant.valueOf ( values[i] ), null, null ).get ();
                                        if ( result.isError () )
                                        {
                                            logger.warn ( "handleAnalogWrite write on item {} resulted in error", dataItem, result.getError () );
                                            throw new ModbusRequestException ( 4, result.getError () );
                                        }
                                        logger.debug ( "handleAnalogWrite write on item {} completed", dataItem );
                                    }
                                    else
                                    {
                                        logger.warn ( "handleAnalogWrite key {} found no item, raising exception", key );
                                        throw new ModbusRequestException ( 2 );
                                    }
                                }
                                else
                                {
                                    logger.warn ( "handleAnalogWrite key {} found no itemId, raising exception", key, itemUri );
                                    throw new ModbusRequestException ( 2 );
                                }
                            }
                        }
                        catch ( ModbusRequestException m )
                        {
                            throw m;
                        }
                        catch ( Exception e )
                        {
                            logger.error ( "handleAnalogWrite caused exception", e );
                            throw new ModbusRequestException ( 4, e );
                        }
                    }
                }, slave.getSlaveId () );
                for ( CfgRegisterMap register : slave.getCoilRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":C:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                }
                for ( CfgRegisterMap register : slave.getDiscreteRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":D:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                }
                for ( CfgRegisterMap register : slave.getHoldingRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":H:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                }
                for ( CfgRegisterMap register : slave.getInputRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":I:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                }
            }
        }
        icm.start ();
    }

    void stop ()
    {
        logger.info ( "stopping Ngp2Modbus" );
        for ( SlaveHost slaveHost : slaveHosts.values () )
        {
            slaveHost.dispose ();
        }
        slaveHosts.clear ();
        if ( icm != null )
        {
            icm.dispose ();
        }
    }
}
