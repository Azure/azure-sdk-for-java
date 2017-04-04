package com.microsoft.azure.servicebus;

public class MaxConcurrencyCounter
{
	private int concurrencyCount = 0;
	private int maxConcurrencyCount = 0;
	
	public synchronized void incrementCount()
	{
		this.concurrencyCount++;
		if(this.concurrencyCount > this.maxConcurrencyCount)
		{
			this.maxConcurrencyCount = this.concurrencyCount;
		}
	}
	
	public synchronized void decrementCount()
	{
		this.concurrencyCount--;
	}
	
	public synchronized int getMaxConcurrencyCount()
	{
		//System.out.println("Max concurrency count :" + this.maxConcurrencyCount);
		return this.maxConcurrencyCount;
	}
}
