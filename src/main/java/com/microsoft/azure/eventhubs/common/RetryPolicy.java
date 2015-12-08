package com.microsoft.azure.eventhubs.common;

public class RetryPolicy {
	// TODO: Flush out what all needs to be implemented for CustomRetryPolicies
	public static final RetryPolicy Default = new RetryExponential();
	public static final RetryPolicy NoRetry = new RetryExponential();
	
}
