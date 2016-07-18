package com.ibh.systems.neoscada.generator.lib;

public abstract class AbstractFactoryElement implements FactoryElement
{

    private final String id;

    public AbstractFactoryElement ( final String id )
    {
        this.id = id;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

}
