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
		// TODO: does string inturn'ing effect sync logic ?
		synchronized (clientId)
		{
			int currentRetryCount = this.getRetryCount(clientId);
		
			if (currentRetryCount >= this.maximumRetryCount)
			{
				return null;
			}
			
			// TODO: Given the current implementation evaluate the need for the extra wait for ServerBusyException
			if (!RetryPolicy.isRetryableException(lastException))
			{
				return null;
			}
		
			double nextRetryInterval = Math.pow(this.retryFactor, (double)currentRetryCount);
			long nextRetryIntervalSeconds = (long) nextRetryInterval ;
			long nextRetryIntervalNano = (long)((nextRetryInterval - (double)nextRetryIntervalSeconds) * 1000000000);
			if (remainingTime.getSeconds() < Math.max(nextRetryInterval, ClientConstants.TimerTolerance.getSeconds()))
			{
				return null;
			}
			
			Duration retryAfter = this.minimumBackoff.plus(Duration.ofSeconds(nextRetryIntervalSeconds, nextRetryIntervalNano));
			return retryAfter;
		}
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
