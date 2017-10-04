package org.eclipse.neoscada.contrib.plantsimulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scada.protocol.modbus.slave.AbstractDataSlave;
import org.eclipse.scada.protocol.modbus.slave.AnalogType;
import org.eclipse.scada.protocol.modbus.slave.DigitalType;
import org.eclipse.scada.protocol.modbus.slave.ModbusRequestException;
import org.eclipse.scada.protocol.modbus.slave.SlaveHost;
import org.openscada.protocol.iec60870.ProtocolOptions;
import org.openscada.protocol.iec60870.server.Server;
import org.openscada.protocol.iec60870.server.ServerModule;
import org.openscada.protocol.iec60870.server.data.DataModule;
import org.openscada.protocol.iec60870.server.data.DataModuleOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public abstract class CommonSimulator
{
    private final static Logger logger = LoggerFactory.getLogger ( CommonSimulator.class );

    protected final WeatherProvider wp;

    protected final Random rnd;

    protected final ScheduledExecutorService scheduledExecutorService;

    protected final PlantConfig plantConfig;

    private boolean isDown = false;

    private boolean isStuck = false;

    private boolean unavailable = false;

    private boolean isError = false;

    private boolean isManuallyError = false;

    private boolean isManuallyStuck = false;

    private int eisman = 100;

    private final InetSocketAddress address;

    private SlaveHost slaveHost = null;

    private Server server;

    private AtomicReference<CalculatedPower> calculatedPower = new AtomicReference<CalculatedPower> ( new CalculatedPower () );

    private boolean toggle = false;

    private boolean watchdog = false;

    private boolean active = false;

    private int setpoint = 100;

    private SimulatorModel dataModel;

    private Statistics statistics;

    protected CommonSimulator ( Statistics statistics, WeatherProvider wp, PlantConfig plantConfig )
    {
        this.statistics = statistics;
        this.rnd = new Random ( plantConfig.getSeed () );
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor ( new ThreadFactoryBuilder ().setNameFormat ( plantConfig.getName () + "-%d" ).build () );
        this.wp = wp;
        this.plantConfig = plantConfig;
        address = new InetSocketAddress ( "0.0.0.0", plantConfig.getPort () );
    }

    public void run ()
    {
        this.scheduledExecutorService.scheduleAtFixedRate ( new Runnable () {

            @Override
            public void run ()
            {
                nextStepInternal ();
            }
        }, 4, 10, TimeUnit.SECONDS );
    }

    private void nextStepInternal ()
    {
        // chance to go down 1%
        // chance to recover from down 5%
        if ( isDown )
        {
            if ( rnd.nextDouble () < 0.05 )
            {
                isDown = false;
            }
        }
        else
        {
            if ( rnd.nextDouble () > 0.99 )
            {
                isDown = true;
            }
        }
        // chance to go in error 2.5%
        // chance to recover from error 10%
        if ( isError )
        {
            if ( rnd.nextDouble () < 0.1 )
            {
                isError = false;
            }
        }
        else
        {
            if ( rnd.nextDouble () > 0.975 )
            {
                isError = true;
            }
        }
        // chance to go in error 2.5%
        // chance to recover from error 5%
        if ( unavailable )
        {
            if ( rnd.nextDouble () < 0.05 )
            {
                unavailable = false;
            }
        }
        else
        {
            if ( rnd.nextDouble () > 0.975 )
            {
                unavailable = true;
            }
        }
        // chance to get stuck 2.5%
        // chance to recover from error 2.5%
        if ( isStuck )
        {
            if ( rnd.nextDouble () < 0.025 )
            {
                isStuck = false;
            }
        }
        else
        {
            if ( rnd.nextDouble () > 0.975 )
            {
                isStuck = true;
            }
        }
        // chance to go in eisman 0.05%
        switch ( eisman )
        {
            case 100:
                if ( rnd.nextDouble () > 0.995 )
                {
                    int e = rnd.nextInt ( 10 );
                    if ( e == 9 )
                    {
                        eisman = 70;
                    }
                    else if ( e == 8 )
                    {
                        eisman = 30;
                    }
                    else
                    {
                        eisman = 0;
                    }
                }
                break;
            default:
                break;
        }
        if ( ! ( isStuck || isManuallyStuck ) )
        {
            toggle = !toggle;
            calculatedPower.set ( nextStep () );
        }
        if ( plantConfig.getConnectionType () == ConnectionType.MODBUS )
        {
            try
            {
                handleModbus ();
            }
            catch ( Exception e )
            {
                logger.error ( "handleModbus () failed:", e );
            }
        }
        else if ( plantConfig.getConnectionType () == ConnectionType.IEC104 )
        {
            try
            {
                handleIec ();
            }
            catch ( Exception e )
            {
                logger.error ( "handleIec () failed:", e );
                disposeIec104Server ();
            }
        }
    }

    private void handleIec () throws Exception
    {
        CalculatedPower cp = calculatedPower.get ();
        logger.trace ( "handleIec for {}: down = {}, error = {}, unavailable = {}, toggle = {}, cp = {}, numOfServersOverall = {}", new Object[] { plantConfig.getName (), isDown, isError, unavailable, toggle, cp, statistics.getNumberOfIecServers ().get () } );
        if ( isDown )
        {
            disposeIec104Server ();
        }
        else if ( server == null )
        {
            ProtocolOptions protocolOptions = new ProtocolOptions.Builder ().build ();
            DataModuleOptions options = new DataModuleOptions.Builder ().build ();
            dataModel = new SimulatorModel ( plantConfig.getName (), this );
            DataModule d = new DataModule ( options, dataModel );
            List<ServerModule> sm = new ArrayList<> ();
            sm.add ( d );
            server = new Server ( address.getPort (), protocolOptions, sm );
            statistics.getNumberOfIecServers ().incrementAndGet ();
        }
        else
        {
            dataModel.update ();
        }
    }

    private void disposeIec104Server ()
    {
        if ( server != null )
        {
            try
            {
                statistics.getNumberOfIecServers ().decrementAndGet ();
                server.close ();
            }
            catch ( Exception e )
            {
                logger.error ( "disposeIec104Server : server.close () failed:", e );
            }
            server = null;
        }
        if ( dataModel != null )
        {
            try
            {
                dataModel.dispose ();
            }
            catch ( Exception e )
            {
                logger.error ( "disposeIec104Server : dataModel.dispose () failed:", e );
            }
            dataModel = null;
        }
    }

    private void handleModbus () throws IOException
    {
        final CalculatedPower cp = calculatedPower.get ();
        logger.trace ( "handleModbus for {}: down = {}, error = {}, unavailable = {}, toggle = {}, cp = {}", new Object[] { plantConfig.getName (), isDown, isError, unavailable, toggle, cp } );
        if ( isDown )
        {
            if ( slaveHost != null )
            {
                slaveHost.dispose ();
                slaveHost = null;
            }
        }
        else if ( slaveHost == null )
        {
            slaveHost = new SlaveHost ( null, null, address );
            slaveHost.registerSlave ( new AbstractDataSlave () {
                @Override
                protected boolean[] handleReadDigital ( DigitalType type, int startAddress, int quantity ) throws ModbusRequestException
                {
                    boolean[] result = new boolean[quantity];
                    boolean[] values = new boolean[2];
                    values[0] = isManuallyError;
                    values[1] = isManuallyStuck;
                    if ( startAddress < 2 )
                    {
                        for ( int i = startAddress; i < Math.min ( startAddress + quantity, 2 ); i++ )
                        {
                            result[i - startAddress] = values[i];
                        }
                    }
                    return result;
                }

                @Override
                protected int[] handleReadAnalog ( AnalogType type, int startAddress, int quantity ) throws ModbusRequestException
                {
                    int[] result = new int[quantity];
                    int[] values = new int[9];
                    values[0] = active ? 1 : 0;
                    values[1] = watchdog ? 1 : 0;
                    values[2] = setpoint;
                    values[3] = unavailable ? 0 : 1;
                    values[4] = toggle ? 1 : 0;
                    values[5] = (int)Math.round ( cp.getOutput () );
                    values[6] = (int)Math.round ( cp.getOutput () );
                    values[7] = (int)Math.round ( cp.getOutputPlus5 () );
                    values[8] = (int)Math.round ( cp.getOutput () );
                    if ( startAddress < 9 )
                    {
                        for ( int i = startAddress; i < Math.min ( startAddress + quantity, 9 ); i++ )
                        {
                            result[i - startAddress] = values[i];
                        }
                    }
                    return result;
                }

                @Override
                protected void handleDigitalWrite ( int address, boolean[] values ) throws ModbusRequestException
                {
                    for ( int i = 0; i < values.length; i++ )
                    {
                        switch ( address + i )
                        {
                            case 0:
                                isManuallyError = values[i];
                                break;
                            case 1:
                                isManuallyStuck = values[i];
                                break;
                        }
                    }
                }

                @Override
                protected void handleAnalogWrite ( int address, int[] values ) throws ModbusRequestException
                {
                    for ( int i = 0; i < values.length; i++ )
                    {
                        switch ( address + i )
                        {
                            case 0:
                                active = values[i] != 0;
                                break;
                            case 1:
                                watchdog = values[i] != 0;
                                break;
                            case 2:
                                setpoint = values[i];
                                if ( setpoint < 0 )
                                {
                                    setpoint = 0;
                                }
                                if ( setpoint > 100 )
                                {
                                    setpoint = 100;
                                }
                                break;
                        }
                    }
                }
            }, 1 );
        }
    }

    /**
     * calculates power
     */
    protected abstract CalculatedPower nextStep ();

    public boolean isToggle ()
    {
        return toggle;
    }

    public boolean isError ()
    {
        return isError || isManuallyError;
    }

    public int getEisman ()
    {
        return eisman;
    }

    public CalculatedPower getCalculatedPower ()
    {
        return calculatedPower.get ();
    }

    public boolean isWatchdog ()
    {
        return watchdog;
    }

    public boolean isActive ()
    {
        return active;
    }

    public int getSetpoint ()
    {
        return setpoint;
    }

    public boolean isUnavailable ()
    {
        return unavailable;
    }

    public boolean isStuck ()
    {
        return isStuck || isManuallyStuck;
    }

    public boolean isManuallyError ()
    {
        return isManuallyError;
    }

    public void setManuallyError ( boolean isManuallyError )
    {
        this.isManuallyError = isManuallyError;
    }

    public boolean isManuallyStuck ()
    {
        return isManuallyStuck;
    }

    public void setManuallyStuck ( boolean isManuallyStuck )
    {
        this.isManuallyStuck = isManuallyStuck;
    }
}
