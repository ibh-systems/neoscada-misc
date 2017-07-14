package org.eclipse.neoscada.contrib.status;

import java.io.Serializable;

public class QueueSize implements Serializable, Comparable<QueueSize>
{
    private static final long serialVersionUID = -3384187397546095685L;

    private String name;

    private int size;

    public QueueSize ( String name, int size )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException ( "name must not be null" );
        }
        if ( size < 0 )
        {
            throw new IllegalArgumentException ( "size must be not less then 0" );
        }
        this.name = name;
        this.size = size;
    }

    public String getName ()
    {
        return name;
    }

    public int getSize ()
    {
        return size;
    }

    @Override
    public int compareTo ( QueueSize o )
    {
        int c = this.name.compareTo ( o.name );
        if ( c == 0 )
        {
            return Integer.compare ( this.size, o.size );
        }
        return c;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode () );
        result = prime * result + size;
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
        QueueSize other = (QueueSize)obj;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals ( other.name ) )
            return false;
        if ( size != other.size )
            return false;
        return true;
    }

    @Override
    public String toString ()
    {
        return this.name + " = " + this.size;
    }
}
