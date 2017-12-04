package org.eclipse.neoscada.contrib.tsdb.producer;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class DaProducerImplTest
{
    @Test
    public void testBla () throws Exception
    {
        DaProducerImpl p = new DaProducerImpl ();
        p.activate ();
    }
}
