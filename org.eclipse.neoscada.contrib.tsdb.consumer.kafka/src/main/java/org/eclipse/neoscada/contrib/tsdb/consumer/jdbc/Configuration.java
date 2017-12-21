package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.io.Serializable;

public class Configuration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String kafkaUrl = "localhost:9092";

    // default behavior, one topic, all tags
    private String javaScript = "function toTopic(id) { return 'neoscada'; }";

    private String javaScriptFile;

    private int flushInterval = 1; // in seconds

    public String getKafkaUrl ()
    {
        return kafkaUrl;
    }

    public void setKafkaUrl ( String kafkaUrl )
    {
        this.kafkaUrl = kafkaUrl;
    }

    public String getJavaScript ()
    {
        return javaScript;
    }

    public void setJavaScript ( String javaScript )
    {
        this.javaScript = javaScript;
    }

    public String getJavaScriptFile ()
    {
        return javaScriptFile;
    }

    public void setJavaScriptFile ( String javaScriptFile )
    {
        this.javaScriptFile = javaScriptFile;
    }

    public int getFlushInterval ()
    {
        return flushInterval;
    }

    public void setFlushInterval ( int flushInterval )
    {
        this.flushInterval = flushInterval;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + flushInterval;
        result = prime * result + ( ( javaScript == null ) ? 0 : javaScript.hashCode () );
        result = prime * result + ( ( javaScriptFile == null ) ? 0 : javaScriptFile.hashCode () );
        result = prime * result + ( ( kafkaUrl == null ) ? 0 : kafkaUrl.hashCode () );
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
        Configuration other = (Configuration)obj;
        if ( flushInterval != other.flushInterval )
            return false;
        if ( javaScript == null )
        {
            if ( other.javaScript != null )
                return false;
        }
        else if ( !javaScript.equals ( other.javaScript ) )
            return false;
        if ( javaScriptFile == null )
        {
            if ( other.javaScriptFile != null )
                return false;
        }
        else if ( !javaScriptFile.equals ( other.javaScriptFile ) )
            return false;
        if ( kafkaUrl == null )
        {
            if ( other.kafkaUrl != null )
                return false;
        }
        else if ( !kafkaUrl.equals ( other.kafkaUrl ) )
            return false;
        return true;
    }
}
