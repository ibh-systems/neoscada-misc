package org.eclipse.neoscada.contrib.kafka;

import java.io.Serializable;

import org.eclipse.scada.da.client.DataItemValue;

/**
 * represents the value how it should be written to the database
 * 
 * @author jrose
 */
public class ValueChangeEvent implements Serializable, Comparable<ValueChangeEvent>
{

    private static final long serialVersionUID = 1L;

    private final String id;

    private final DataItemValue value;

    private final boolean heartbeat;

    public ValueChangeEvent ( String id, DataItemValue value, boolean heartbeat )
    {
        this.id = id;
        this.value = value;
        this.heartbeat = heartbeat;
    }

    public String getId ()
    {
        return id;
    }

    public DataItemValue getValue ()
    {
        return value;
    }

    public boolean isHeartbeat ()
    {
        return heartbeat;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode () );
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
        ValueChangeEvent other = (ValueChangeEvent)obj;
        if ( id == null )
        {
            if ( other.id != null )
                return false;
        }
        else if ( !id.equals ( other.id ) )
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

    @Override
    public String toString ()
    {
        return "ValueChangeEvent [id=" + id + ", value=" + value + ", heartbeat=" + this.heartbeat + "]";
    }

    @Override
    public int compareTo ( ValueChangeEvent o )
    {
        long t1 = Long.MIN_VALUE;
        long t2 = Long.MIN_VALUE;
        if ( this.value.getTimestamp () != null )
        {
            t1 = this.value.getTimestamp ().getTimeInMillis ();
        }
        if ( o.getValue ().getTimestamp () != null )
        {
            t2 = o.value.getTimestamp ().getTimeInMillis ();
        }
        if ( t1 != t2 )
        {
            return Long.compare ( t1, t2 );
        }
        if ( !this.id.equals ( o.id ) )
        {
            return this.id.compareTo ( o.id );
        }
        return Boolean.compare ( this.heartbeat, o.heartbeat );
    }
}
