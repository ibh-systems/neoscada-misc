package org.eclipse.neoscada.contrib.plantsimulator;

public class PlantConfig
{
    private String name;

    private PlantType plantType;

    private int generators;

    private int power;

    private int nominalPower;

    private int seed;

    private int port;

    private ConnectionType connectionType;

    public PlantConfig ( String name, PlantType plantType, int generators, int power, int seed, ConnectionType connectionType, int port )
    {
        this.name = name;
        this.plantType = plantType;
        this.generators = generators;
        this.power = power;
        this.nominalPower = generators * power;
        this.seed = seed;
        this.connectionType = connectionType;
        this.port = port;
    }

    public String getName ()
    {
        return name;
    }

    public PlantType getPlantType ()
    {
        return plantType;
    }

    public int getNumOfGenerators ()
    {
        return generators;
    }

    public int getPowerOfGenerators ()
    {
        return power;
    }

    public int getNominalPower ()
    {
        return nominalPower;
    }

    public int getSeed ()
    {
        return seed;
    }
    
    public ConnectionType getConnectionType ()
    {
        return connectionType;
    }
    
    public int getPort ()
    {
        return port;
    }

    @Override
    public String toString ()
    {
        return name + " (" + plantType + ") Gen: " + generators + "*" + power + "kW, nominal power: " + getNominalPower () + "kW @" + connectionType + ":" + port;
    }
}
