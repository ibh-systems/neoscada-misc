package org.eclipse.neoscada.contrib.plantsimulator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

public class PlantSimulator
{

    public void run ( WeatherProvider wp, InputStream is, int numOfPlants )
    {
        Type listType = new TypeToken<ArrayList<PlantConfig>> () {
            private static final long serialVersionUID = 1L;
        }.getType ();
        BufferedReader br = new BufferedReader ( new InputStreamReader ( is, Charsets.UTF_8 ) );
        List<PlantConfig> plantConfigs = new GsonBuilder ().create ().fromJson ( br, listType );

        int i = 0;
        for ( PlantConfig plantConfig : plantConfigs )
        {
            i++;
            switch ( plantConfig.getPlantType () )
            {
                case WIND:
                    new WindSimulator ( wp, plantConfig ).run ();
                    break;
                case SOLAR:
                    new SolarSimulator ( wp, plantConfig ).run ();
                    break;
                case BIOMASS:
                    new BiomassSimulator ( wp, plantConfig ).run ();
                    break;
            }
            if ( i >= numOfPlants )
            {
                break;
            }
        }
    }

}
