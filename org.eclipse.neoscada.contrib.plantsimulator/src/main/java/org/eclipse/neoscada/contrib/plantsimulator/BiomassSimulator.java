package org.eclipse.neoscada.contrib.plantsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiomassSimulator extends CommonSimulator
{
    private final static Logger logger = LoggerFactory.getLogger ( BiomassSimulator.class );

    public BiomassSimulator ( Statistics statistics, WeatherProvider wp, PlantConfig plantConfig )
    {
        super ( statistics, wp, plantConfig );
    }

    @Override
    protected CalculatedPower nextStep ()
    {
        logger.debug ( "next step for {}", plantConfig.getName () );
        final CalculatedPower p = new CalculatedPower ();
        p.setTimestamp ( System.currentTimeMillis () );
        p.setInput ( 0.0 );
        p.setInputPlus5 ( 0.0 );
        double activePowerPlant = 0.0;
        double precedingBasePointPlant = 0.0;
        for ( int i = 0; i < plantConfig.getNumOfGenerators (); i++ )
        {
            final double fluctuation = ( 0.5 - rnd.nextDouble () ) * 0.01;
            double activePower = Math.round ( ( 0.85 * plantConfig.getPowerOfGenerators () + ( fluctuation * plantConfig.getPowerOfGenerators () ) ) * 100 ) / 100.0;
            p.getPower ().put ( i, activePower );
            p.getPowerPlus5 ().put ( i, activePower );
            activePowerPlant += activePower;
            precedingBasePointPlant += activePower;
        }
        p.setOutput ( activePowerPlant );
        p.setOutputPlus5 ( precedingBasePointPlant );
        return p;
    }
}
