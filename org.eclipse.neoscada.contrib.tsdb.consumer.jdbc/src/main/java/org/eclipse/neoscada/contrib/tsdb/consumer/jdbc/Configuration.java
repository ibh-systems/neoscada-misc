package org.eclipse.neoscada.contrib.tsdb.consumer.jdbc;

import java.io.Serializable;

public class Configuration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String databaseConnectionUrl;

    private String databaseDriver;

    private String databaseName;

    private String databaseUser;

    private String databasePassword;

    private String databaseHost;
    
    private int batchSize = 100;

    private int flushInterval = 1; // in seconds

    private boolean storeName = true;

    private boolean storeError = true;

    private boolean storeAlarm = true;

    private boolean storeWarning = true;

    private boolean storeManual = true;

    private boolean storeBlocked = true;

    private boolean storeHeartbeat = true;

    private boolean storeEntryTimestamp = true;
    
    private boolean storeBooleanAsInteger = false;

    private String javaScript = "function toTableName(id) { return 'tsdb'; }";

    private String javaScriptFile;

    public String getDatabaseConnectionUrl ()
    {
        return databaseConnectionUrl;
    }

    public void setDatabaseConnectionUrl ( String databaseConnectionUrl )
    {
        this.databaseConnectionUrl = databaseConnectionUrl;
    }

    public String getDatabaseDriver ()
    {
        return databaseDriver;
    }

    public void setDatabaseDriver ( String databaseDriver )
    {
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseName ()
    {
        return databaseName;
    }

    public void setDatabaseName ( String databaseName )
    {
        this.databaseName = databaseName;
    }

    public String getDatabaseUser ()
    {
        return databaseUser;
    }

    public void setDatabaseUser ( String databaseUser )
    {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword ()
    {
        return databasePassword;
    }

    public void setDatabasePassword ( String databasePassword )
    {
        this.databasePassword = databasePassword;
    }

    public String getDatabaseHost ()
    {
        return databaseHost;
    }

    public void setDatabaseHost ( String databaseHost )
    {
        this.databaseHost = databaseHost;
    }

    public int getBatchSize ()
    {
        return batchSize;
    }

    public void setBatchSize ( int batchSize )
    {
        this.batchSize = batchSize;
    }

    public int getFlushInterval ()
    {
        return flushInterval;
    }

    public void setFlushInterval ( int flushInterval )
    {
        this.flushInterval = flushInterval;
    }

    public boolean isStoreName ()
    {
        return storeName;
    }

    public void setStoreName ( boolean storeName )
    {
        this.storeName = storeName;
    }

    public boolean isStoreError ()
    {
        return storeError;
    }

    public void setStoreError ( boolean storeError )
    {
        this.storeError = storeError;
    }

    public boolean isStoreAlarm ()
    {
        return storeAlarm;
    }

    public void setStoreAlarm ( boolean storeAlarm )
    {
        this.storeAlarm = storeAlarm;
    }

    public boolean isStoreWarning ()
    {
        return storeWarning;
    }

    public void setStoreWarning ( boolean storeWarning )
    {
        this.storeWarning = storeWarning;
    }

    public boolean isStoreManual ()
    {
        return storeManual;
    }

    public void setStoreManual ( boolean storeManual )
    {
        this.storeManual = storeManual;
    }

    public boolean isStoreBlocked ()
    {
        return storeBlocked;
    }

    public void setStoreBlocked ( boolean storeBlocked )
    {
        this.storeBlocked = storeBlocked;
    }

    public boolean isStoreHeartbeat ()
    {
        return storeHeartbeat;
    }

    public void setStoreHeartbeat ( boolean storeHeartbeat )
    {
        this.storeHeartbeat = storeHeartbeat;
    }

    public boolean isStoreEntryTimestamp ()
    {
        return storeEntryTimestamp;
    }

    public void setStoreEntryTimestamp ( boolean storeEntryTimestamp )
    {
        this.storeEntryTimestamp = storeEntryTimestamp;
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

    public boolean isStoreBooleanAsInteger ()
    {
        return storeBooleanAsInteger;
    }

    public void setStoreBooleanAsInteger ( boolean storeBooleanAsInteger )
    {
        this.storeBooleanAsInteger = storeBooleanAsInteger;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + batchSize;
        result = prime * result + ( ( databaseConnectionUrl == null ) ? 0 : databaseConnectionUrl.hashCode () );
        result = prime * result + ( ( databaseDriver == null ) ? 0 : databaseDriver.hashCode () );
        result = prime * result + ( ( databaseHost == null ) ? 0 : databaseHost.hashCode () );
        result = prime * result + ( ( databaseName == null ) ? 0 : databaseName.hashCode () );
        result = prime * result + ( ( databasePassword == null ) ? 0 : databasePassword.hashCode () );
        result = prime * result + ( ( databaseUser == null ) ? 0 : databaseUser.hashCode () );
        result = prime * result + flushInterval;
        result = prime * result + ( ( javaScript == null ) ? 0 : javaScript.hashCode () );
        result = prime * result + ( ( javaScriptFile == null ) ? 0 : javaScriptFile.hashCode () );
        result = prime * result + ( storeAlarm ? 1231 : 1237 );
        result = prime * result + ( storeBlocked ? 1231 : 1237 );
        result = prime * result + ( storeBooleanAsInteger ? 1231 : 1237 );
        result = prime * result + ( storeEntryTimestamp ? 1231 : 1237 );
        result = prime * result + ( storeError ? 1231 : 1237 );
        result = prime * result + ( storeHeartbeat ? 1231 : 1237 );
        result = prime * result + ( storeManual ? 1231 : 1237 );
        result = prime * result + ( storeName ? 1231 : 1237 );
        result = prime * result + ( storeWarning ? 1231 : 1237 );
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
        if ( batchSize != other.batchSize )
            return false;
        if ( databaseConnectionUrl == null )
        {
            if ( other.databaseConnectionUrl != null )
                return false;
        }
        else if ( !databaseConnectionUrl.equals ( other.databaseConnectionUrl ) )
            return false;
        if ( databaseDriver == null )
        {
            if ( other.databaseDriver != null )
                return false;
        }
        else if ( !databaseDriver.equals ( other.databaseDriver ) )
            return false;
        if ( databaseHost == null )
        {
            if ( other.databaseHost != null )
                return false;
        }
        else if ( !databaseHost.equals ( other.databaseHost ) )
            return false;
        if ( databaseName == null )
        {
            if ( other.databaseName != null )
                return false;
        }
        else if ( !databaseName.equals ( other.databaseName ) )
            return false;
        if ( databasePassword == null )
        {
            if ( other.databasePassword != null )
                return false;
        }
        else if ( !databasePassword.equals ( other.databasePassword ) )
            return false;
        if ( databaseUser == null )
        {
            if ( other.databaseUser != null )
                return false;
        }
        else if ( !databaseUser.equals ( other.databaseUser ) )
            return false;
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
        if ( storeAlarm != other.storeAlarm )
            return false;
        if ( storeBlocked != other.storeBlocked )
            return false;
        if ( storeBooleanAsInteger != other.storeBooleanAsInteger )
            return false;
        if ( storeEntryTimestamp != other.storeEntryTimestamp )
            return false;
        if ( storeError != other.storeError )
            return false;
        if ( storeHeartbeat != other.storeHeartbeat )
            return false;
        if ( storeManual != other.storeManual )
            return false;
        if ( storeName != other.storeName )
            return false;
        if ( storeWarning != other.storeWarning )
            return false;
        return true;
    }
}
