package org.eclipse.neoscada.contrib.status;

public class ThreadInformation {
	private boolean deadlock = false;
	
	private int numOfThreads = 0;
	
	public ThreadInformation ()
    {
    }
	
	public ThreadInformation(ThreadInformation threadInformation) {
	    this.numOfThreads = threadInformation.numOfThreads;
	    this.deadlock = threadInformation.deadlock;
	}
	
	public boolean isDeadlock() {
		return deadlock;
	}
	
	public void setDeadlock(boolean isDeadlock) {
		this.deadlock = isDeadlock;
	}
	
	public void setNumOfThreads(int numOfThreads) {
		this.numOfThreads = numOfThreads;
	}
	
	public int getNumOfThreads() {
		return numOfThreads;
	}
}
