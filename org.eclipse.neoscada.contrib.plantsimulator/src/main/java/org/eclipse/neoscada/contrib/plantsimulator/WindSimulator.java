package org.eclipse.neoscada.contrib.plantsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindSimulator extends CommonSimulator
{
    private final static Logger logger = LoggerFactory.getLogger ( WindSimulator.class );

    public WindSimulator ( Statistics statistics, WeatherProvider wp, PlantConfig plantConfig )
    {
        super ( statistics, wp, plantConfig );
    }

    @Override
    protected CalculatedPower nextStep ()
    {
        logger.debug ( "next step for {}", plantConfig.getName () );
        final CalculatedPower p = new CalculatedPower ();
        p.setTimestamp ( System.currentTimeMillis () );
        p.setInput ( wp.getWindSpeed () );
        p.setInputPlus5 ( wp.getWindSpeedPlus5 () );
        double activePowerPlant = 0.0;
        double precedingBasePointPlant = 0.0;
        for ( int i = 0; i < plantConfig.getNumOfGenerators (); i++ )
        {
            double activePower = getTurbinePower ( p.getInput () );
            double precedingBasePoint = getTurbinePower ( p.getInputPlus5 () );
            p.getPower ().put ( i, activePower );
            p.getPowerPlus5 ().put ( i, precedingBasePoint );
            activePowerPlant += activePower;
            precedingBasePointPlant += precedingBasePoint;
        }
        p.setOutput ( activePowerPlant );
        p.setOutputPlus5 ( precedingBasePointPlant );
        return p;
    }

    private double getTurbinePower ( double ws )
    {
        if ( ws < 2.5 )
        {
            // FIXME: ws = 0.0;
        }
        else if ( ws > 13.0 )
        {
            ws = 13.1;
        }
        final double p = 0.001 * 0.5 * 1.23 * ( this.plantConfig.getPowerOfGenerators () * 2 ) * ( Math.pow ( ws, 3.0 ) ) * 0.4;
        final double fluctuation = ( 0.5 - rnd.nextDouble () ) * 0.01;
        return Math.round ( ( p + ( fluctuation * p ) ) * 100 ) / 100.0;
    }
}
