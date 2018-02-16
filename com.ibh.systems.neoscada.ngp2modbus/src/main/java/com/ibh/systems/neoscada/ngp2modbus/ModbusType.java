package com.ibh.systems.neoscada.ngp2modbus;

public enum ModbusType
{
    UINT16(16),
    SINT16(16),
    UINT32(32),
    SINT32(32),
    FLOAT16(16),
    FLOAT32(32);

    private final int size;

    private ModbusType (int size)
    {
        this.size = size;
    }
    
    public int getSize ()
    {
        return size;
    }
}
