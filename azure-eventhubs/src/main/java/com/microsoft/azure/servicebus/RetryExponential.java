/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;

/**
 *  RetryPolicy implementation where the delay between retries will grow in an exponential manner.
 *  RetryPolicy can be set on the client operations using {@link ConnectionStringBuilder}.
 *  RetryIntervals will be computed using a retryFactor which is a function of deltaBackOff (MaximumBackoff - MinimumBackoff) and MaximumRetryCount
 */
public final class RetryExponential extends RetryPolicy
{
	private final Duration minimumBackoff;
	private final Duration maximumBackoff;
	private final int maximumRetryCount;
	private final double retryFactor;
	
	public RetryExponential(final Duration minimumBackoff, final Duration maximumBackoff, final int maximumRetryCount, final String name)
	{
		super(name);
		
		this.minimumBackoff = minimumBackoff;
		this.maximumBackoff = maximumBackoff;
		this.maximumRetryCount = maximumRetryCount;
		this.retryFactor = this.computeRetryFactor();
	}

	@Override
	protected Duration onGetNextRetryInterval(final String clientId, final Exception lastException, final Duration remainingTime, final int baseWaitTimeSecs)
	{
		int currentRetryCount = this.getRetryCount(clientId);
	
		if (currentRetryCount >= this.maximumRetryCount)
		{
			return null;
		}
		
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
		retryAfter = retryAfter.plus(Duration.ofSeconds(baseWaitTimeSecs));

		return retryAfter;
	}
	
	private double computeRetryFactor()
	{
		long deltaBackoff = this.maximumBackoff.minus(this.minimumBackoff).getSeconds();
		if (deltaBackoff <= 0 || this.maximumRetryCount <= 0)
		{
			return 0;
		}
		
		return (Math.log(deltaBackoff) / Math.log(this.maximumRetryCount));
	}
}
