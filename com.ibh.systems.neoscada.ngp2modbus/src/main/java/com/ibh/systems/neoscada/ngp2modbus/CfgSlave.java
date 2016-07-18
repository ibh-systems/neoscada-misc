package com.ibh.systems.neoscada.ngp2modbus;
import java.util.Set;
import java.util.TreeSet;


public class CfgSlave
{
    private int slaveId = 0;

    private Set<CfgRegisterMap> holdingRegisters = new TreeSet<> ();

    private Set<CfgRegisterMap> inputRegisters = new TreeSet<> ();

    private Set<CfgRegisterMap> coilRegisters = new TreeSet<> ();

    private Set<CfgRegisterMap> discreteRegisters = new TreeSet<> ();

    public int getSlaveId ()
    {
        return slaveId;
    }

    public void setSlaveId ( int slaveId )
    {
        this.slaveId = slaveId;
    }

    public Set<CfgRegisterMap> getHoldingRegisters ()
    {
        return holdingRegisters;
    }

    public void setHoldingRegisters ( Set<CfgRegisterMap> holdingRegisters )
    {
        this.holdingRegisters = holdingRegisters;
    }

    public Set<CfgRegisterMap> getInputRegisters ()
    {
        return inputRegisters;
    }

    public void setInputRegisters ( Set<CfgRegisterMap> inputRegisters )
    {
        this.inputRegisters = inputRegisters;
    }

    public Set<CfgRegisterMap> getCoilRegisters ()
    {
        return coilRegisters;
    }

    public void setCoilRegisters ( Set<CfgRegisterMap> coilRegisters )
    {
        this.coilRegisters = coilRegisters;
    }

    public Set<CfgRegisterMap> getDiscreteRegisters ()
    {
        return discreteRegisters;
    }

    public void setDiscreteRegisters ( Set<CfgRegisterMap> discreteRegisters )
    {
        this.discreteRegisters = discreteRegisters;
    }
}