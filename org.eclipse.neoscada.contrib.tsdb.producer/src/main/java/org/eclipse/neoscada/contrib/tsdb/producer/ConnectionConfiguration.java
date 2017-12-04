package org.eclipse.neoscada.contrib.tsdb.producer;

import java.io.Serializable;

public class ConnectionConfiguration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String user = "eclipsescada";

    private String password = "eclipsescada";

    private String ngpUrl = "da:ngp://localhost:2101";

    private int checkInterval = 900; // in seconds

    private boolean cacheNames = true;

    // default behavior, all tags, name stays as is
    private String javaScript = "function filterTag(tagName) { return true; }\nfunction toName(tagName) { return tagName; }";

    private String javaScriptFile;

    public String getUser ()
    {
        return user;
    }

    public void setUser ( String user )
    {
        this.user = user;
    }

    public String getPassword ()
    {
        return password;
    }

    public void setPassword ( String password )
    {
        this.password = password;
    }

    public String getNgpUrl ()
    {
        return ngpUrl;
    }

    public void setNgpUrl ( String ngpUrl )
    {
        this.ngpUrl = ngpUrl;
    }

    public int getCheckInterval ()
    {
        return checkInterval;
    }

    public void setCheckInterval ( int checkInterval )
    {
        this.checkInterval = checkInterval;
    }

    public boolean isCacheNames ()
    {
        return cacheNames;
    }

    public void setCacheNames ( boolean cacheNames )
    {
        this.cacheNames = cacheNames;
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
}
