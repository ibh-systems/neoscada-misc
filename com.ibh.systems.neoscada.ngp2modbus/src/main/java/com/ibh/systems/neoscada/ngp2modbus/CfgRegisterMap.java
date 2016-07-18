package com.ibh.systems.neoscada.ngp2modbus;

public class CfgRegisterMap implements Comparable<CfgRegisterMap>
{
    private int address;

    private String item;

    public int getAddress ()
    {
        return address;
    }

    public void setAddress ( int address )
    {
        this.address = address;
    }

    public String getItem ()
    {
        return item;
    }

    public void setItem ( String item )
    {
        this.item = item;
    }

    @Override
    public int compareTo ( CfgRegisterMap o )
    {
        return Integer.compare ( address, o.address );
    }
}