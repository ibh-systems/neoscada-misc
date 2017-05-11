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
package org.eclipse.neoscada.contrib.iec104torest;

import java.util.Map;
import java.util.TreeMap;

public class Configuration
{
    private String httpAddress = "0.0.0.0:8080";

    private String iec104Address = "localhost:2404";

    private Map<String, String> addressMapping = new TreeMap<> ();

    private boolean mapByNames = false;

    private int storePeriod = 0; // in minutes!

    private String storeDirectory;

    private String storeSuffix = ".csv";

    public Configuration ()
    {
    }

    public String getHttpAddress ()
    {
        return httpAddress;
    }

    public void setHttpAddress ( String httpAddress )
    {
        this.httpAddress = httpAddress;
    }

    public String getIec104Address ()
    {
        return iec104Address;
    }

    public void setIec104Address ( String iec104Address )
    {
        this.iec104Address = iec104Address;
    }

    public Map<String, String> getAddressMapping ()
    {
        return addressMapping;
    }

    public void setAddressMapping ( Map<String, String> addressMapping )
    {
        this.addressMapping = addressMapping;
    }

    public boolean isMapByNames ()
    {
        return mapByNames;
    }

    public void setMapByNames ( boolean mapByNames )
    {
        this.mapByNames = mapByNames;
    }

    public int getStorePeriod ()
    {
        return storePeriod;
    }

    public void setStorePeriod ( int storePeriod )
    {
        this.storePeriod = storePeriod;
    }

    public String getStoreDirectory ()
    {
        return storeDirectory;
    }

    public void setStoreDirectory ( String storeDirectory )
    {
        this.storeDirectory = storeDirectory;
    }

    public String getStoreSuffix ()
    {
        return storeSuffix;
    }

    public void setStoreSuffix ( String storeSuffix )
    {
        this.storeSuffix = storeSuffix;
    }
}
