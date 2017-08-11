package org.eclipse.neoscada.contrib.plantsimulator;

import java.util.HashMap;
import java.util.Map;

public class CalculatedPower
{
    private long timestamp = 0;
    
    private double input = 0.0;
    
    private double inputPlus5 = 0.0;
    
    private Map<Integer, Double> power = new HashMap<> ();

    private Map<Integer, Double> powerPlus5 = new HashMap<> ();
    
    private double output = 0.0;
    
    private double outputPlus5 = 0.0;
    
    public long getTimestamp ()
    {
        return timestamp;
    }
    
    public void setTimestamp ( long timestamp )
    {
        this.timestamp = timestamp;
    }

    public double getInput ()
    {
        return input;
    }

    public double getInputPlus5 ()
    {
        return inputPlus5;
    }

    public Map<Integer, Double> getPower ()
    {
        return power;
    }

    public Map<Integer, Double> getPowerPlus5 ()
    {
        return powerPlus5;
    }
    
    public void setInput ( double input )
    {
        this.input = input;
    }
    
    public void setInputPlus5 ( double inputPlus5 )
    {
        this.inputPlus5 = inputPlus5;
    }

    public double getOutput ()
    {
        return output;
    }

    public void setOutput ( double output )
    {
        this.output = output;
    }

    public double getOutputPlus5 ()
    {
        return outputPlus5;
    }

    public void setOutputPlus5 ( double outputPlus5 )
    {
        this.outputPlus5 = outputPlus5;
    }

    @Override
    public String toString ()
    {
        return "CalculatedPower [power=" + power + ", powerPlus5=" + powerPlus5 + ", output=" + output + ", outputPlus5=" + outputPlus5 + "]";
    }
}
