package com.ibh.systems.neoscada.generator.lib;

public class SecOsgiManagerFactoryElement extends AbstractFactoryElement
{

    private final int priority;

    private final String script;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_SEC_OSGI_MANAGER_FACTORY;
    }

    public SecOsgiManagerFactoryElement ( final String id, final int priority, final String script )
    {
        super ( id );
        this.priority = priority;
        this.script = script;
    }

    public int getPriority ()
    {
        return this.priority;
    }

    public String getScript ()
    {
        return this.script;
    }

}
