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
import java.util.*;

/**
 *  RetryPolicy implementation where the delay between retries will grow in a staggered exponential manner.
 *  RetryPolicy can be set on the client operations using {@link ServiceBusConnectionStringBuilder}.
 *  RetryIntervals will be computed using a retryFactor which is a function of deltaBackOff (MaximumBackoff - MinimumBackoff) and MaximumRetryCount
 */
public final class RetryExponential extends RetryPolicy
{
	private final Duration minimumBackoff;
	private final Duration maximumBackoff;
	private final int maximumRetryCount;
	private final double retryFactor;
	
	public RetryExponential(Duration minimumBackoff, Duration maximumBackoff, int maximumRetryCount)
	{
		this.minimumBackoff = minimumBackoff;
		this.maximumBackoff = maximumBackoff;
		this.maximumRetryCount = maximumRetryCount;
		this.retryFactor = this.computeRetryFactor();
	}

	@Override
	public Duration getNextRetryInterval(String clientId, Exception lastException, Duration remainingTime)
	{
		int currentRetryCount = this.getRetryCount(clientId);
	
		if (currentRetryCount >= this.maximumRetryCount)
		{
			return null;
		}
		
		// keep track of last error and add extra wait for ServerBusyException
		if (!RetryPolicy.isRetryableException(lastException))
		{
			return null;
		}
	
		double nextRetryInterval = Math.pow(this.retryFactor, (double)currentRetryCount);
		long nextRetryIntervalSeconds = (long) nextRetryInterval ;
		long nextRetryIntervalNano = (long)((nextRetryInterval - (double)nextRetryIntervalSeconds) * 1000000000);
		if (remainingTime.getSeconds() < Math.max(nextRetryInterval, ClientConstants.TIMER_TOLERANCE.getSeconds()))
		{
			return null;
		}
		
		Duration retryAfter = this.minimumBackoff.plus(Duration.ofSeconds(nextRetryIntervalSeconds, nextRetryIntervalNano));
		return retryAfter;
	}
	
	private double computeRetryFactor()
	{
		long deltaBackoff = this.maximumBackoff.minus(this.minimumBackoff).getSeconds();
		if (deltaBackoff <= 0 || this.maximumRetryCount <= 0) {
			return 0;
		}
		
		return (Math.log(deltaBackoff) / Math.log(this.maximumRetryCount));
	}
}
