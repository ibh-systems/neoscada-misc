package org.eclipse.neoscada.contrib.plantsimulator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.AtomicDouble;

public class WeatherProvider
{
    private static final DateTimeZone TZ = DateTimeZone.forID ( "Europe/Berlin" );

    private static DateFormat df = new SimpleDateFormat ( "yyyy-MM-dd HH:mm", Locale.GERMANY );

    private PreparedStatement solarSt;

    private PreparedStatement windSt;

    private ScheduledExecutorService scheduler;

    private AtomicDouble solar = new AtomicDouble ( 0.0 );

    private AtomicDouble solarPlus5 = new AtomicDouble ( 0.0 );

    private AtomicDouble wind = new AtomicDouble ( 0.0 );

    private AtomicDouble windPlus5 = new AtomicDouble ( 0.0 );

    public WeatherProvider () throws Exception
    {
        Connection con = DriverManager.getConnection ( "jdbc:h2:mem:weather;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "su", "" );

        con.setAutoCommit ( false );
        PreparedStatement st = con.prepareStatement ( "CREATE TABLE SOLAR (ts TIMESTAMP, value DECIMAL(5,1));" );
        st.execute ();
        con.commit ();
        st.close ();
        st = con.prepareStatement ( "CREATE TABLE WIND (ts TIMESTAMP, value DECIMAL(3,1));" );
        st.execute ();
        con.commit ();
        st.close ();
        st = con.prepareStatement ( "CREATE INDEX idx_ts_solar ON SOLAR(ts);" );
        st.execute ();
        con.commit ();
        st.close ();
        st = con.prepareStatement ( "CREATE INDEX idx_ts_wind ON wIND(ts);" );
        st.execute ();
        con.commit ();
        st.close ();
        loadSolar ( con );
        loadWind ( con );

        solarSt = con.prepareStatement ( "SELECT value FROM SOLAR WHERE ts > DATEADD('MINUTE', -11, ?) AND ts < DATEADD('MINUTE', 11, ?)" );
        windSt = con.prepareStatement ( "SELECT value FROM WIND WHERE ts > DATEADD('MINUTE', -11, ?) AND ts < DATEADD('MINUTE', 11, ?)" );

        scheduler = Executors.newSingleThreadScheduledExecutor ();
        scheduler.scheduleAtFixedRate ( new Runnable () {
            @Override
            public void run ()
            {
                try
                {
                    updateWeatherData ();
                }
                catch ( Exception e )
                {
                    e.printStackTrace ();
                }
            }
        }, 0, 3, TimeUnit.SECONDS );
    }

    protected void updateWeatherData () throws SQLException
    {
        final MutableDateTime dt = new MutableDateTime ( System.currentTimeMillis (), TZ );
        dt.set ( DateTimeFieldType.year (), 2016 );
        Timestamp sqlTs = new Timestamp ( dt.toInstant ().getMillis () );

        solarSt.setTimestamp ( 1, sqlTs );
        solarSt.setTimestamp ( 2, sqlTs );
        ResultSet rs = solarSt.executeQuery ();
        List<Double> values = new ArrayList<> ( 3 );
        while ( rs.next () )
        {
            values.add ( rs.getDouble ( 1 ) );
        }
        rs.close ();
        if ( values.size () == 0 )
        {
            // do nothing, keep the last value
        }
        else if ( values.size () == 1 )
        {
            solar.set ( values.get ( 0 ) );
        }
        else if ( values.size () == 2 )
        {
            solar.set ( ( values.get ( 0 ) + values.get ( 1 ) ) / 2.0 );
        }
        else if ( values.size () > 2 )
        {
            solar.set ( values.get ( 1 ) );
        }

        windSt.setTimestamp ( 1, sqlTs );
        windSt.setTimestamp ( 2, sqlTs );
        rs = windSt.executeQuery ();
        values = new ArrayList<> ( 3 );
        while ( rs.next () )
        {
            values.add ( rs.getDouble ( 1 ) );
        }
        rs.close ();
        if ( values.size () == 0 )
        {
            // do nothing, keep the last value
        }
        else if ( values.size () == 1 )
        {
            wind.set ( values.get ( 0 ) );
        }
        else if ( values.size () == 2 )
        {
            wind.set ( ( values.get ( 0 ) + values.get ( 1 ) ) / 2.0 );
        }
        else if ( values.size () > 2 )
        {
            wind.set ( values.get ( 1 ) );
        }

        dt.add ( 5 * 60 * 1000 );

        sqlTs = new Timestamp ( dt.toInstant ().getMillis () );
        solarSt.setTimestamp ( 1, sqlTs );
        solarSt.setTimestamp ( 2, sqlTs );
        rs = solarSt.executeQuery ();
        values = new ArrayList<> ( 3 );
        while ( rs.next () )
        {
            values.add ( rs.getDouble ( 1 ) );
        }
        rs.close ();
        if ( values.size () == 0 )
        {
            // do nothing, keep the last value
        }
        else if ( values.size () == 1 )
        {
            solarPlus5.set ( values.get ( 0 ) );
        }
        else if ( values.size () == 2 )
        {
            solarPlus5.set ( ( values.get ( 0 ) + values.get ( 1 ) ) / 2.0 );
        }
        else if ( values.size () > 2 )
        {
            solarPlus5.set ( values.get ( 1 ) );
        }

        windSt.setTimestamp ( 1, sqlTs );
        windSt.setTimestamp ( 2, sqlTs );
        rs = windSt.executeQuery ();
        values = new ArrayList<> ( 3 );
        while ( rs.next () )
        {
            values.add ( rs.getDouble ( 1 ) );
        }
        rs.close ();
        if ( values.size () == 0 )
        {
            // do nothing, keep the last value
        }
        else if ( values.size () == 1 )
        {
            windPlus5.set ( values.get ( 0 ) );
        }
        else if ( values.size () == 2 )
        {
            windPlus5.set ( ( values.get ( 0 ) + values.get ( 1 ) ) / 2.0 );
        }
        else if ( values.size () > 2 )
        {
            windPlus5.set ( values.get ( 1 ) );
        }
    }

    public double getSolarRadiation ()
    {
        return solar.get ();
    }

    public double getSolarRadiationPlus5 ()
    {
        return solarPlus5.get ();
    }

    public double getWindSpeed ()
    {
        return wind.get ();
    }

    public double getWindSpeedPlus5 ()
    {
        return windPlus5.get ();
    }

    private void loadSolar ( Connection con ) throws Exception
    {

        final BufferedReader r = new BufferedReader ( new InputStreamReader ( getClass ().getClassLoader ().getResourceAsStream ( "solar.csv" ), Charsets.UTF_8 ) );
        String line;
        PreparedStatement st = con.prepareStatement ( "INSERT INTO SOLAR VALUES (?, ?);" );
        while ( ( line = r.readLine () ) != null )
        {
            final String[] parts = line.split ( ";" );
            final String d = parts[0];
            final String v = parts.length > 1 ? parts[1] : "0";
            st.setTimestamp ( 1, new Timestamp ( df.parse ( d ).getTime () ) );
            st.setDouble ( 2, Double.parseDouble ( v ) );
            st.executeUpdate ();
        }
        st.close ();
        con.commit ();
        r.close ();
    }

    private void loadWind ( Connection con ) throws Exception
    {

        final BufferedReader r = new BufferedReader ( new InputStreamReader ( getClass ().getClassLoader ().getResourceAsStream ( "wind.csv" ), Charsets.UTF_8 ) );
        String line;
        PreparedStatement st = con.prepareStatement ( "INSERT INTO WIND VALUES (?, ?);" );
        while ( ( line = r.readLine () ) != null )
        {
            final String[] parts = line.split ( ";" );
            final String d = parts[0];
            final String v = parts.length > 1 ? parts[1] : "0";
            st.setTimestamp ( 1, new Timestamp ( df.parse ( d ).getTime () ) );
            st.setDouble ( 2, Double.parseDouble ( v ) );
            st.executeUpdate ();
        }
        st.close ();
        con.commit ();
        r.close ();
    }
}
