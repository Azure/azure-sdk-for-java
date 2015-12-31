package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.concurrent.*;

public abstract class RetryPolicy
{
	private ConcurrentHashMap<String, Integer> retryCounts;
	
	public static final RetryPolicy NoRetry = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0);
	
	protected RetryPolicy()
	{
		this.retryCounts = new ConcurrentHashMap<String, Integer>();
	}
	
	void incrementRetryCount(String clientId)
	{
		synchronized (clientId)
		{
			Integer retryCount = this.retryCounts.get(clientId);
			this.retryCounts.put(clientId, retryCount == null ? 1 : retryCount + 1);
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
	 * @param retryAfter "out" parameter. Pass retryAfter with value = 0 - this method will increment the value. Don't new-up this parameter in the implementations of {@link RetryPolicy#isNextRetryAllowed(Duration)}.
	 */
	public abstract boolean isNextRetryAllowed(String clientId, Exception lastException, Duration remainingTime, Duration retryAfter);
}
