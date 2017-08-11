package org.eclipse.neoscada.contrib.plantsimulator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;

public class PlantConfigGenerator
{
    public void run ( int seed, int numOfPlants, int startPort ) throws Exception
    {
        if ( numOfPlants > 10000 )
        {
            throw new IllegalArgumentException ( "numOfPlants may not exceed 10000" );
        }
        if ( numOfPlants < 1 )
        {
            throw new IllegalArgumentException ( "numOfPlants must be at least 1" );
        }
        Random rnd = new Random ( seed );

        final List<String> locations = new ArrayList<> ( numOfPlants + 1 );
        final List<String> cities = getCities ();
        final List<String> streets = getStreets ();
        final List<PlantConfig> plantConfigs = new ArrayList<> ( numOfPlants + 1 );
        for ( String city : cities )
        {
            for ( String street : streets )
            {
                locations.add ( city + ", " + street );
            }
        }
        // shuffle 7 times
        for ( int i = 0; i < 7; i++ )
        {
            Collections.shuffle ( locations, rnd );
        }
        for ( int i = 0; i < numOfPlants; i++ )
        {
            final String location = locations.get ( i );
            HashFunction hf = Hashing.md5 ();
            byte[] h = hf.newHasher ().putString ( location, Charsets.UTF_8 ).hash ().asBytes ();

            // determine plant type
            // the relative numbers are roughly 3 : 3 : 1
            PlantType plantType = PlantType.BIOMASS;
            if ( h[0] + 128 < 84 )
            {
                plantType = PlantType.WIND;
            }
            else if ( h[0] + 128 < 170 )
            {
                plantType = PlantType.SOLAR;
            }

            // depending on plant type, determine actual name
            String company;
            int index = ( (int)h[1] + 128 ) / 32;
            if ( plantType == PlantType.WIND )
            {
                company = Arrays.asList ( "Bürgerwindpark ", "Windgenossenschaft ", "Windpark ", "Windprojekt ", "Windenergie ", "Ostwind ", "Südwind ", "Nordwind " ).get ( index );
            }
            else if ( plantType == PlantType.SOLAR )
            {
                company = Arrays.asList ( "Bürgersolarpark ", "Solarpark ", "Feld ", "Photovoltaikanlage ", "Solarkomplex ", "SunTec ", "Sunfield ", "Bürgerstrom " ).get ( index );
            }
            else
            {
                company = Arrays.asList ( "Agrargenossenschaft ", "LPG ", "Bio ", "Bioenergie ", "Argarenergie ", "Agro ", "Genossenschaft ", "Biogas " ).get ( index );
            }

            // determine number of generators/inverters
            int generators = ( (int)h[2] + 128 );
            if ( plantType == PlantType.WIND )
            {
                generators = Double.valueOf ( Math.floor ( ( Math.sqrt ( (double)generators ) + Math.pow ( ( (double)generators ), 8.0 ) / ( Math.pow ( 266, 7 ) ) ) / 4 ) ).intValue ();
            }
            else if ( plantType == PlantType.SOLAR )
            {
                generators = Double.valueOf ( Math.floor ( ( Math.sqrt ( (double)generators ) + Math.pow ( ( (double)generators ), 8.0 ) / ( Math.pow ( 266, 7 ) ) ) / 8 ) ).intValue ();
            }
            else
            {
                generators = generators / 64;
            }
            generators = generators == 0 ? 1 : generators;

            // determine size of generators/turbines
            int p = ( (int)h[3] + 128 );
            int power = 0;
            if ( plantType == PlantType.WIND )
            {
                if ( p < 120 )
                {
                    power = 1000;
                }
                else if ( p < 180 )
                {
                    power = 1500;
                }
                else if ( p < 190 )
                {
                    power = 2000;
                }
                else if ( p < 210 )
                {
                    power = 2500;
                }
                else if ( p < 220 )
                {
                    power = 3000;
                }
                else if ( p < 230 )
                {
                    power = 4500;
                }
                else if ( p < 240 )
                {
                    power = 4500;
                }
                else
                {
                    power = 6000;
                }
            }
            else if ( plantType == PlantType.SOLAR )
            {
                if ( p < 80 )
                {
                    power = 850;
                }
                else if ( p < 160 )
                {
                    power = 1000;
                }
                else if ( p < 200 )
                {
                    power = 1500;
                }
                else if ( p < 240 )
                {
                    power = 2500;
                }
                else
                {
                    power = 3000;
                }
            }
            else
            {
                if ( p < 32 )
                {
                    power = 150;
                }
                else if ( p < 64 )
                {
                    power = 250;
                }
                else if ( p < 128 )
                {
                    power = 450;
                }
                else if ( p < 160 )
                {
                    power = 850;
                }
                else if ( p < 190 )
                {
                    power = 1000;
                }
                else if ( p < 230 )
                {
                    power = 1500;
                }
                else if ( p < 250 )
                {
                    power = 3000;
                }
                else
                {
                    power = 5000;
                }
            }

            int simSeed = (int)h[4] * (int)h[5];

            ConnectionType connectionType = ConnectionType.IEC104;
            if ( ( (int)h[6] + 128 ) > 120 )
            {
                connectionType = ConnectionType.MODBUS;
            }
            plantConfigs.add ( new PlantConfig ( company + location, plantType, generators, power, simSeed, connectionType, startPort + i ) );
        }
        new GsonBuilder ().setPrettyPrinting ().create ().toJson ( plantConfigs, System.out );
    }

    private List<String> getCities () throws Exception
    {
        final List<String> cities = new ArrayList<> ( 251 );
        final BufferedReader r = new BufferedReader ( new InputStreamReader ( getClass ().getClassLoader ().getResourceAsStream ( "cities.csv" ), Charsets.UTF_8 ) );
        String line;
        while ( ( line = r.readLine () ) != null )
        {
            cities.add ( line );
        }
        r.close ();
        return cities;
    }

    private List<String> getStreets () throws Exception
    {
        final List<String> streets = new ArrayList<> ( 41 );
        final BufferedReader r = new BufferedReader ( new InputStreamReader ( getClass ().getClassLoader ().getResourceAsStream ( "strassen.csv" ), Charsets.UTF_8 ) );
        String line;
        while ( ( line = r.readLine () ) != null )
        {
            streets.add ( line );
        }
        r.close ();
        return streets;
    }
}
