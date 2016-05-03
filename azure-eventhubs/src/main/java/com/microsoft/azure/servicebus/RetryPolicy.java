/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

// TODO: SIMPLIFY retryPolicy - ConcurrentHashMap is not needed
public abstract class RetryPolicy
{
	private static final RetryPolicy NO_RETRY = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0);
	private ConcurrentHashMap<String, Integer> retryCounts;
	private boolean isServerBusy;
	private Instant lastServerBusyReportedTime;
	private Object serverBusySync;
	
	protected RetryPolicy()
	{
		this.retryCounts = new ConcurrentHashMap<String, Integer>();
		this.isServerBusy = false;
		this.lastServerBusyReportedTime = Instant.EPOCH;
		this.serverBusySync = new Object();
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
	 * Gets the Interval after which nextRetry should be done.
	 * 
	 * @param clientId clientId
	 * @param lastException lastException
	 * @param remainingTime remainingTime to retry
	 * @return returns 'null' Duration when not Allowed
	 */
	public Duration getNextRetryInterval(String clientId, Exception lastException, Duration remainingTime)
	{
		synchronized (this.serverBusySync)
		{
			if (lastException != null &&
					(lastException instanceof ServerBusyException || (lastException.getCause() != null && lastException.getCause() instanceof ServerBusyException)))
			{
				this.isServerBusy = true;
				this.lastServerBusyReportedTime = Instant.now();
			}
		}
		
		return this.onGetNextRetryInterval(clientId, lastException, remainingTime);
	}
	
	public boolean isServerBusy()
	{
		synchronized (this.serverBusySync)
		{
			return (this.isServerBusy &&
					Instant.now().isBefore(this.lastServerBusyReportedTime.plus(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS, ChronoUnit.SECONDS)));
		}	
	}
	
	protected abstract Duration onGetNextRetryInterval(String clientId, Exception lastException, Duration remainingTime);
}
