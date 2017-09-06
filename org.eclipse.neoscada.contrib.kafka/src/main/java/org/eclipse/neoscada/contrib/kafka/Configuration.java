package org.eclipse.neoscada.contrib.kafka;

import java.io.Serializable;

public class Configuration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String user = "eclipsescada";

	private String password = "eclipsescada";

	private String ngpUrl = "da:ngp://localhost:2101";
	
	private String zookeeperUrl = "localhost:9092";

	private int checkInterval = 900; // in seconds

	private int heartBeat = 900; // in seconds
	
	private boolean cacheNames = true;

	// default behavior, one table, all tags
	private String javaScript = "function toTopic(id) { return 'neoscada'; }\nfunction filterTag(tagName) { return true; }\nfunction toName(tagName) { return tagName; }";

	private String javaScriptFile;

	private boolean storeName = true;

	private boolean storeError = true;

	private boolean storeAlarm = true;

	private boolean storeWarning = true;

	private boolean storeManual = true;

    private boolean storeBlocked = true;

    private boolean storeHeartbeat = true;

    private boolean storeEntryTimestamp = true;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNgpUrl() {
		return ngpUrl;
	}

	public void setNgpUrl(String ngpUrl) {
		this.ngpUrl = ngpUrl;
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public int getHeartBeat ()
    {
        return heartBeat;
    }
	
	public void setHeartBeat ( int heartBeat )
    {
        this.heartBeat = heartBeat;
    }

	public boolean isCacheNames() {
		return cacheNames;
	}

	public void setCacheNames(boolean cacheNames) {
		this.cacheNames = cacheNames;
	}

	public String getJavaScript() {
		return javaScript;
	}

	public void setJavaScript(String javaScript) {
		this.javaScript = javaScript;
	}
	
	public String getJavaScriptFile() {
		return javaScriptFile;
	}
	
	public void setJavaScriptFile(String javaScriptFile) {
		this.javaScriptFile = javaScriptFile;
	}

	public boolean isStoreName() {
		return storeName;
	}

	public void setStoreName(boolean storeName) {
		this.storeName = storeName;
	}

	public boolean isStoreError() {
		return storeError;
	}

	public void setStoreError(boolean storeError) {
		this.storeError = storeError;
	}

	public boolean isStoreAlarm() {
		return storeAlarm;
	}

	public void setStoreAlarm(boolean storeAlarm) {
		this.storeAlarm = storeAlarm;
	}

	public boolean isStoreWarning() {
		return storeWarning;
	}

	public void setStoreWarning(boolean storeWarning) {
		this.storeWarning = storeWarning;
	}

	public boolean isStoreManual() {
		return storeManual;
	}

	public void setStoreManual(boolean storeManual) {
		this.storeManual = storeManual;
	}

	public boolean isStoreBlocked() {
		return storeBlocked;
	}

	public void setStoreBlocked(boolean storeBlocked) {
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
	
	public String getZookeeperUrl ()
    {
        return zookeeperUrl;
    }
	
	public void setZookeeperUrl ( String zookeeperUrl )
    {
        this.zookeeperUrl = zookeeperUrl;
    }
}
