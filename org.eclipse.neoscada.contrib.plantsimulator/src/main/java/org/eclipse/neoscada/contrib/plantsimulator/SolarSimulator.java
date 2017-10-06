package org.eclipse.neoscada.contrib.plantsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolarSimulator extends CommonSimulator
{
    private final static Logger logger = LoggerFactory.getLogger ( SolarSimulator.class );

    public SolarSimulator ( Statistics statistics, WeatherProvider wp, PlantConfig plantConfig, int period )
    {
        super ( statistics, wp, plantConfig, period );
    }

    @Override
    protected CalculatedPower nextStep ()
    {
        logger.debug ( "next step for {}", plantConfig.getName () );
        final CalculatedPower p = new CalculatedPower ();
        p.setTimestamp ( System.currentTimeMillis () );
        p.setInput ( wp.getSolarRadiation () );
        p.setInputPlus5 ( wp.getSolarRadiationPlus5 () );
        double activePowerPlant = 0.0;
        double precedingBasePointPlant = 0.0;
        for ( int i = 0; i < plantConfig.getNumOfGenerators (); i++ )
        {
            double activePower = getInverterPower ( p.getInput () );
            double precedingBasePoint = getInverterPower ( p.getInputPlus5 () );
            p.getPower ().put ( i, activePower );
            p.getPowerPlus5 ().put ( i, precedingBasePoint );
            activePowerPlant += activePower;
            precedingBasePointPlant += precedingBasePoint;
        }
        p.setOutput ( activePowerPlant );
        p.setOutputPlus5 ( precedingBasePointPlant );
        return p;
    }

    private double getInverterPower ( double irradiation )
    {
        final double p = this.plantConfig.getPowerOfGenerators () / 1000.0 * irradiation;
        final double fluctuation = ( 0.5 - rnd.nextDouble () ) * 0.01;
        return Math.round ( ( p + ( fluctuation * p ) ) * 100 ) / 100.0;
    }
}
