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

import java.lang.management.MemoryUsage;

public class JvmMemoryUsage
{
    private MemoryUsage heapMemoryUsage = new MemoryUsage ( 0, 0, 0, 0 );

    private MemoryUsage nonHeapMemoryUsage = new MemoryUsage ( 0, 0, 0, 0 );
    
    public MemoryUsage getHeapMemoryUsage ()
    {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage ( MemoryUsage heapMemoryUsage )
    {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public MemoryUsage getNonHeapMemoryUsage ()
    {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage ( MemoryUsage nonHeapMemoryUsage )
    {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }
}
