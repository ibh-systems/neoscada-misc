package com.ibh.systems.neoscada.generator.lib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ScriptDataSourceFactoryElement extends AbstractFactoryElement
{

    private final String init;

    private final String updateCommand;

    private final String timerCommand;

    private final String writeCommand;

    private Integer timer = null;

    private final String engine = "JavaScript";

    private final Map<String, String> dataSources = new TreeMap<> ();

    private final Map<String, String> writeSources = new TreeMap<> ();

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_SCRIPT_ITEM_FACTORY;
    }

    public ScriptDataSourceFactoryElement ( final String id, final String init, final String updateCommand, final String timerCommand, final String writeCommand, final Integer timer, final Map<String, String> dataSources, final Map<String, String> writeSources )
    {
        super ( id );
        this.init = init;
        this.updateCommand = updateCommand;
        this.timerCommand = timerCommand;
        this.writeCommand = writeCommand;
        this.timer = timer;
        if ( dataSources != null )
        {
            this.dataSources.putAll ( dataSources );
        }
        if ( writeSources != null )
        {
            this.writeSources.putAll ( writeSources );
        }
    }

    public String getInit ()
    {
        return this.init;
    }

    public String getUpdateCommand ()
    {
        return this.updateCommand;
    }

    public String getTimerCommand ()
    {
        return this.timerCommand;
    }

    public String getWriteCommand ()
    {
        return this.writeCommand;
    }

    public Integer getTimer ()
    {
        return this.timer;
    }

    public String getEngine ()
    {
        return this.engine;
    }

    public Map<String, String> getDataSources ()
    {
        return Collections.unmodifiableMap ( this.dataSources );
    }

    public Map<String, String> getWriteSources ()
    {
        return Collections.unmodifiableMap ( this.writeSources );
    }
}
