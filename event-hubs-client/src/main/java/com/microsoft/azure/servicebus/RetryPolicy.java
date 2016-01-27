package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.concurrent.*;

// TODO: SIMPLIFY retryPolicy - ConcurrentHashMap is not needed
public abstract class RetryPolicy
{
	private ConcurrentHashMap<String, Integer> retryCounts;
	
	public static final RetryPolicy NoRetry = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0);
	
	protected RetryPolicy()
	{
		this.retryCounts = new ConcurrentHashMap<String, Integer>();
	}
	
	public void incrementRetryCount(String clientId)
	{
		synchronized (clientId)
		{
			Integer retryCount = this.retryCounts.get(clientId);
			this.retryCounts.put(clientId, retryCount == null ? 1 : retryCount + 1);
		}
	}
	
	public void resetRetryCount(String clientId)
	{
		Integer currentRetryCount = this.retryCounts.get(clientId);
		if (currentRetryCount != null && currentRetryCount != 0)
		{
			synchronized (clientId)
			{
				this.retryCounts.put(clientId, 0);
			}
		}
	}
	
	public static boolean isRetryableException(Exception exception)
	{
		if (exception == null)
		{
			throw new IllegalArgumentException("exception cannot be null");
		}
		
		if (exception instanceof ServiceBusException)
		{
			return ((ServiceBusException) exception).getIsTransient();
		}
		
		return false;
	}
	
	public static RetryPolicy getDefault()
	{
		return new RetryExponential(
			ClientConstants.DefaultRetryMinBackoff, 
			ClientConstants.DefaultRetryMaxBackoff, 
			ClientConstants.DefaultMaxRetryCount);
	}
	
	protected int getRetryCount(String clientId)
	{
		synchronized(clientId)
		{
			Integer retryCount = this.retryCounts.get(clientId);
			return retryCount == null ? 0 : retryCount;
		}
	}
	
	/**
	 * return returns 'null' Duration when not Allowed
	 */
	public abstract Duration getNextRetryInterval(String clientId, Exception lastException, Duration remainingTime);
}
