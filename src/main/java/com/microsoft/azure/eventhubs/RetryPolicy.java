package com.microsoft.azure.eventhubs;

public abstract class RetryPolicy {
	public static final RetryPolicy Default = new RetryExponential();
	public static final RetryPolicy NoRetry = new RetryExponential();
	
	// TODO: Flush out what all needs to be implemented for CustomRetryPolicies
}
