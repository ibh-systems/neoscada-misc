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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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

    // contains "PORT:SLAVE:(H|I|C|D):ADDRESS" as key, type of the register is value 
    private final Map<String, ModbusType> address2Type = new TreeMap<> ();

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
                            ModbusType modbusType = address2Type.get ( key );
                            if ( item != null )
                            {
                                logger.trace ( "handleReadAnalog got item {} for key {}", item.getItemId (), key );
                                DataItemValue div = item.getSnapshotValue ();
                                logger.trace ( "handleReadAnalog item {} for key {} has value {}", item.getItemId (), key, div );
                                if ( div.isConnected () && !div.isError () )
                                {
                                    ByteBuf buf = Unpooled.directBuffer ( 2 );
                                    switch ( modbusType )
                                    {
                                        case UINT16:
                                        case SINT16:
                                            buf.writeShort ( div.getValue ().asInteger ( 0 ) );
                                            break;
                                        case UINT32:
                                        case SINT32:
                                            buf.writeInt ( div.getValue ().asInteger ( 0 ) );
                                            break;
                                        case FLOAT16:
                                            buf.writeShort ( fromFloat ( div.getValue ().asDouble ( 0.0 ).floatValue () ) );
                                            break;
                                        case FLOAT32:
                                            buf.writeFloat ( div.getValue ().asDouble ( 0.0 ).floatValue () );
                                            break;
                                        default:
                                            break;
                                    }
                                    result[i] = buf.readUnsignedShort();
                                    if ( ( modbusType.getSize () == 32 ) && ( i + 1 < result.length ) )
                                    {
                                        result[i + 1] = buf.readUnsignedShort();
                                    }
                                    buf.release ();
                                }
                                else
                                {
                                    // we explicitely set the value to 0
                                    result[i] = 0;
                                    if ( ( modbusType.getSize () == 32 ) && ( i + 1 < result.length ) )
                                    {
                                        result[i + 1] = 0;
                                    }
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
                                    final ModbusType modbusType = address2Type.get ( key );

                                    logger.trace ( "handleAnalogWrite key {} resulted in itemId {}", key, itemUri );
                                    DataItem dataItem = icm.getItem ( itemUri );
                                    if ( dataItem != null )
                                    {
                                        logger.trace ( "handleAnalogWrite key {} resulted in item {}", key, dataItem.getItemId () );

                                        // first gather the bytes in buffer
                                        ByteBuf buf;
                                        if ( modbusType.getSize () == 32 )
                                        {
                                            buf = Unpooled.directBuffer ( 2 );
                                            buf.writeShort ( values[i] );
                                            if ( i + 1 < values.length )
                                            {
                                                buf.writeShort ( values[i + 1] );
                                            }
                                        }
                                        else
                                        {
                                            buf = Unpooled.directBuffer ( 1 );
                                            buf.writeShort ( values[i] );
                                        }

                                        // now interprete the bytes
                                        Number value;
                                        switch ( modbusType )
                                        {
                                            case UINT16:
                                                value = (int)buf.readShort ();
                                                break;
                                            case SINT16:
                                                value = buf.readUnsignedShort ();
                                                break;
                                            case UINT32:
                                                value = buf.readUnsignedInt ();
                                                break;
                                            case SINT32:
                                                value = buf.readInt ();
                                                break;
                                            case FLOAT16:
                                                value = toFloat ( (int)buf.readShort () );
                                                break;
                                            case FLOAT32:
                                                value = buf.readFloat ();
                                                break;
                                            default:
                                                value = null;
                                                break;
                                        }

                                        // and finally write it
                                        WriteResult result = icm.getConnection ( itemUri ).startWrite ( dataItem.getItemId (), Variant.valueOf ( value ), null, null ).get ();
                                        if ( result.isError () )
                                        {
                                            logger.warn ( "handleAnalogWrite write on item {} resulted in error", dataItem, result.getError () );
                                            throw new ModbusRequestException ( 4, result.getError () );
                                        }
                                        logger.debug ( "handleAnalogWrite write on item {} completed", dataItem );
                                        buf.release ();
                                    }
                                    else
                                    {
                                        logger.warn ( "handleAnalogWrite key {} found no item, raising exception", key );
                                        throw new ModbusRequestException ( 2 );
                                    }
                                    // we skip the next byte when its a 32bit value (because its already set)
                                    if ( modbusType.getSize () == 32 )
                                    {
                                        i += 1;
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
                    address2Type.put ( key, register.getType () );
                }
                for ( CfgRegisterMap register : slave.getDiscreteRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":D:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                    address2Type.put ( key, register.getType () );
                }
                for ( CfgRegisterMap register : slave.getHoldingRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":H:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                    address2Type.put ( key, register.getType () );
                }
                for ( CfgRegisterMap register : slave.getInputRegisters () )
                {
                    String key = port.getPort () + ":" + slave.getSlaveId () + ":I:" + register.getAddress ();
                    icm.addItem ( register.getItem () );
                    address2Item.put ( key, register.getItem () );
                    address2Type.put ( key, register.getType () );
                }
            }
        }
        icm.start ();
    }

    private static int fromFloat ( float fval )
    {
        int fbits = Float.floatToIntBits ( fval );
        int sign = fbits >>> 16 & 0x8000; // sign only
        int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

        if ( val >= 0x47800000 ) // might be or become NaN/Inf
        { // avoid Inf due to rounding
            if ( ( fbits & 0x7fffffff ) >= 0x47800000 )
            { // is or must become NaN/Inf
                if ( val < 0x7f800000 ) // was value but too large
                    return sign | 0x7c00; // make it +/-Inf
                return sign | 0x7c00 | // remains +/-Inf or NaN
                        ( fbits & 0x007fffff ) >>> 13; // keep NaN (and Inf) bits
            }
            return sign | 0x7bff; // unrounded not quite Inf
        }
        if ( val >= 0x38800000 ) // remains normalized value
            return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
        if ( val < 0x33000000 ) // too small for subnormal
            return sign; // becomes +/-0
        val = ( fbits & 0x7fffffff ) >>> 23; // tmp exp for subnormal calc
        return sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
                + ( 0x800000 >>> val - 102 ) // round depending on cut off
        >>> 126 - val ); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
    }

    public static float toFloat ( int hbits )
    {
        int mant = hbits & 0x03ff; // 10 bits mantissa
        int exp = hbits & 0x7c00; // 5 bits exponent
        if ( exp == 0x7c00 ) // NaN/Inf
            exp = 0x3fc00; // -> NaN/Inf
        else if ( exp != 0 ) // normalized value
        {
            exp += 0x1c000; // exp - 15 + 127
            if ( mant == 0 && exp > 0x1c400 ) // smooth transition
                return Float.intBitsToFloat ( ( hbits & 0x8000 ) << 16 | exp << 13 | 0x3ff );
        }
        else if ( mant != 0 ) // && exp==0 -> subnormal
        {
            exp = 0x1c400; // make it normal
            do
            {
                mant <<= 1; // mantissa * 2
                exp -= 0x400; // decrease exp by 1
            } while ( ( mant & 0x400 ) == 0 ); // while not normal
            mant &= 0x3ff; // discard subnormal bit
        } // else +/-0 -> +/-0
        return Float.intBitsToFloat ( // combine all parts
                ( hbits & 0x8000 ) << 16 // sign  << ( 31 - 15 )
                        | ( exp | mant ) << 13 ); // value << ( 23 - 10 )
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
