package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.*;

/**
 *  RetryPolicy implementation where the delay between retries will grow in a staggered exponential manner.
 *  RetryPolicy can be set on the client operations using {@link ServiceBusConnectionStringBuilder}.
 */
public final class RetryExponential extends RetryPolicy
{
	private Duration minimumBackoff;
	private Duration maximumBackoff;
	private int maximumRetryCount;
	private double retryFactor;
	
	public RetryExponential(Duration minimumBackoff, Duration maximumBackoff, int maximumRetryCount)
	{
		this.minimumBackoff = minimumBackoff;
		this.maximumBackoff = maximumBackoff;
		this.maximumRetryCount = maximumRetryCount;
		this.retryFactor = this.computeRetryFactor();
	}

	@Override
	public boolean isNextRetryAllowed(String clientId, Exception lastException, Duration remainingTime, Duration retryAfter)
	{
		// TODO: does string inturn'ing effect sync logic ?
		synchronized (clientId)
		{
			int currentRetryCount = this.getRetryCount(clientId);
		
			if (currentRetryCount >= this.maximumRetryCount)
			{
				return false;
			}
			
			// TODO: Given the current implementation evaluate the need for the extra wait for ServerBusyException
			if (!RetryPolicy.isRetryableException(lastException))
			{
				return false;
			}
		
			long nextRetryInterval = (long) Math.pow(2, (double)this.maximumRetryCount + this.retryFactor);
			if (remainingTime.getSeconds() < nextRetryInterval) {
				return false;
			}
			
			retryAfter.plusSeconds(nextRetryInterval);
			return true;
		}
	}
	
	private double computeRetryFactor()
	{
		long deltaBackoff = this.maximumBackoff.getSeconds() - this.minimumBackoff.getSeconds();
		if (deltaBackoff <= 0 || this.maximumRetryCount <= 0) {
			return 0;
		}
		
		return (Math.log(deltaBackoff / (Math.pow(2, this.maximumRetryCount) - 1))) / (Math.log(2));
	}
}
