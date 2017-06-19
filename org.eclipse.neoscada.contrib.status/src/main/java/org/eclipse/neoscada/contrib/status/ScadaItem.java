/*******************************************************************************
 * Copyright (c) 2017 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.neoscada.contrib.status;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scada.da.client.DataItemValue;

public class ScadaItem
{
    private AtomicReference<DataItemValue> daItemValueRef = new AtomicReference<DataItemValue> ( DataItemValue.DISCONNECTED );

    private ConcurrentSkipListSet<DataItemValue> toggleStates = new ConcurrentSkipListSet<> ( new Comparator<DataItemValue> () {
        @Override
        public int compare ( DataItemValue o1, DataItemValue o2 )
        {
            if ( o1.getTimestamp () != null && o2.getTimestamp () != null )
            {
                return o1.getTimestamp ().compareTo ( o2.getTimestamp () );
            }
            return 0;
        }
    } );

    private String tag;

    private boolean checkTime = false;

    private boolean checkToggle = false;

    private boolean toggleState = false;

    private double l = -Double.MAX_VALUE;

    private double ll = -Double.MAX_VALUE;

    private double h = Double.MAX_VALUE;

    private double hh = Double.MAX_VALUE;

    private long timeDelta = 15 * 60 * 1000; // default is 15min

    public DataItemValue getDaItemValue ()
    {
        return daItemValueRef.get ();
    }

    public void setDaItemValue ( DataItemValue daItemValue )
    {
        this.daItemValueRef.set ( daItemValue );
        if ( checkToggle )
        {
            this.toggleStates.add ( daItemValue );
        }
    }

    public String getTag ()
    {
        return tag;
    }

    public void setTag ( String tag )
    {
        this.tag = tag;
    }

    public boolean isCheckTime ()
    {
        return checkTime;
    }

    public void setCheckTime ( boolean checkTime )
    {
        this.checkTime = checkTime;
    }

    public boolean isCheckToggle ()
    {
        return checkToggle;
    }

    public void setCheckToggle ( boolean checkToggle )
    {
        this.checkToggle = checkToggle;
    }

    public double getL ()
    {
        return l;
    }

    public void setL ( double l )
    {
        this.l = l;
    }

    public double getLl ()
    {
        return ll;
    }

    public void setLl ( double ll )
    {
        this.ll = ll;
    }

    public double getH ()
    {
        return h;
    }

    public void setH ( double h )
    {
        this.h = h;
    }

    public double getHh ()
    {
        return hh;
    }

    public void setHh ( double hh )
    {
        this.hh = hh;
    }

    public long getTimeDelta ()
    {
        return timeDelta;
    }

    public void setTimeDelta ( long timeDelta )
    {
        this.timeDelta = timeDelta;
    }

    public boolean isValueError ()
    {
        return daItemValueRef.get ().isError ();
    }

    public boolean isLlError ()
    {
        if ( daItemValueRef.get ().isError () )
        {
            return true;
        }
        return daItemValueRef.get ().getValue ().asDouble ( -Double.MAX_VALUE ) <= ll;
    }

    public boolean isLError ()
    {
        if ( daItemValueRef.get ().isError () )
        {
            return true;
        }
        return daItemValueRef.get ().getValue ().asDouble ( -Double.MAX_VALUE ) <= l;
    }

    public boolean isHError ()
    {
        if ( daItemValueRef.get ().isError () )
        {
            return true;
        }
        return daItemValueRef.get ().getValue ().asDouble ( Double.MAX_VALUE ) >= h;
    }

    public boolean isHhError ()
    {
        if ( daItemValueRef.get ().isError () )
        {
            return true;
        }
        return daItemValueRef.get ().getValue ().asDouble ( Double.MAX_VALUE ) >= hh;
    }

    public boolean isProblem ()
    {
        return isLlError () || isLError () || isHError () || isHhError () || isToggleError () || isTimestampError ();
    }

    public boolean evaluateToggleState ()
    {
        Set<DataItemValue> toRemove = new LinkedHashSet<> ();
        int numOfTrue = 0;
        int numOfFalse = 0;
        for ( DataItemValue div : toggleStates )
        {
            if ( div.isError () || div.getTimestamp () == null )
            {
                toRemove.add ( div );
                continue;
            }
            if ( div.getTimestamp ().getTimeInMillis () + timeDelta < System.currentTimeMillis () )
            {
                toRemove.add ( div );
            }
            else
            {
                numOfTrue += div.getValue ().asBoolean ( false ) ? 1 : 0;
                numOfFalse += div.getValue ().asBoolean ( true ) ? 0 : 1;
            }
        }
        toggleStates.removeAll ( toRemove );
        this.toggleState = numOfTrue > 0 && numOfFalse > 0;
        return this.toggleState;
    }

    public boolean isToggleError ()
    {
        return ( checkToggle && !this.toggleState );
    }

    public boolean isTimestampError ()
    {
        if ( checkTime )
        {
            DataItemValue div = daItemValueRef.get ();
            if ( div.getTimestamp () == null )
            {
                return true;
            }
            if ( div.getTimestamp ().getTimeInMillis () + timeDelta < System.currentTimeMillis () )
            {
                return true;
            }
        }
        return false;
    }
}
