package com.ibh.systems.neoscada.generator.lib;

public abstract class AbstractFactory implements Factory
{

    @Override
    public int compareTo ( final Factory that )
    {
        return getId ().compareTo ( that.getId () );
    }
}
