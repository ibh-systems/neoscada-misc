package org.eclipse.neoscada.contrib.status;

import java.io.Serializable;
import java.util.Date;

public class GraphiteData implements Serializable, Comparable<GraphiteData>
{
    private static final long serialVersionUID = 1L;

    private final String metric;

    private final String value;

    private final Date timestamp;

    public GraphiteData ( String metric, String value )
    {
        this.metric = metric;
        this.value = value;
        this.timestamp = new Date ();
    }

    public GraphiteData ( String metric, String value, Date timestamp )
    {
        this.metric = metric;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getMetric ()
    {
        return metric;
    }

    public String getValue ()
    {
        return value;
    }

    public Date getTimestamp ()
    {
        return timestamp;
    }

    @Override
    public String toString ()
    {
        return String.format ( "%s %s %d", this.metric, this.value, this.timestamp.getTime () / 1000 );
    }

    @Override
    public int compareTo ( GraphiteData o )
    {
        int i = this.timestamp.compareTo ( o.getTimestamp () );
        if ( i != 0 )
        {
            return i;
        }
        i = this.metric.compareTo ( o.getMetric () );
        if ( i != 0 )
        {
            return i;
        }
        return this.getValue ().compareTo ( o.getValue () );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( metric == null ) ? 0 : metric.hashCode () );
        result = prime * result + ( ( timestamp == null ) ? 0 : timestamp.hashCode () );
        result = prime * result + ( ( value == null ) ? 0 : value.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass () != obj.getClass () )
            return false;
        GraphiteData other = (GraphiteData)obj;
        if ( metric == null )
        {
            if ( other.metric != null )
                return false;
        }
        else if ( !metric.equals ( other.metric ) )
            return false;
        if ( timestamp == null )
        {
            if ( other.timestamp != null )
                return false;
        }
        else if ( !timestamp.equals ( other.timestamp ) )
            return false;
        if ( value == null )
        {
            if ( other.value != null )
                return false;
        }
        else if ( !value.equals ( other.value ) )
            return false;
        return true;
    }
}
