/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.concurrent.*;

// TODO: SIMPLIFY retryPolicy - ConcurrentHashMap is not needed
public abstract class RetryPolicy
{
	private static final RetryPolicy NO_RETRY = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0);
	private ConcurrentHashMap<String, Integer> retryCounts;
	
	protected RetryPolicy()
	{
		this.retryCounts = new ConcurrentHashMap<String, Integer>();
	}
	
	public void incrementRetryCount(String clientId)
	{
		Integer retryCount = this.retryCounts.get(clientId);
		this.retryCounts.put(clientId, retryCount == null ? 1 : retryCount + 1);
	}
	
	public void resetRetryCount(String clientId)
	{
		Integer currentRetryCount = this.retryCounts.get(clientId);
		if (currentRetryCount != null && currentRetryCount != 0)
		{
			this.retryCounts.put(clientId, 0);
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
			ClientConstants.DEFAULT_RERTRY_MIN_BACKOFF, 
			ClientConstants.DEFAULT_RERTRY_MAX_BACKOFF, 
			ClientConstants.DEFAULT_MAX_RETRY_COUNT);
	}
	
	public static RetryPolicy getNoRetry()
	{
		return RetryPolicy.NO_RETRY;
	}
	
	protected int getRetryCount(String clientId)
	{
		Integer retryCount = this.retryCounts.get(clientId);
		return retryCount == null ? 0 : retryCount;
	}

	/**
	 * return returns 'null' Duration when not Allowed
	 */
	public abstract Duration getNextRetryInterval(String clientId, Exception lastException, Duration remainingTime);
}
