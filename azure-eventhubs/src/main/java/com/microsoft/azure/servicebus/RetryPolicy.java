/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
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
