package com.microsoft.azure.servicebus;

import java.time.Duration;

public final class MessageHandlerOptions
{
	private static final boolean DEFAULT_AUTO_COMPLETE = true;
	private static final int DEFAULT_MAX_CONCURRENT_CALLS = 1;
	private static final int DEFAULT_MAX_RENEW_TIME_MINUTES = 5;		
		
	private boolean autoComplete;
	private Duration maxAutoRenewDuration;
	private int maxConcurrentCalls;
	
	public MessageHandlerOptions()
	{
		this(DEFAULT_MAX_CONCURRENT_CALLS, DEFAULT_AUTO_COMPLETE, Duration.ofMinutes(DEFAULT_MAX_RENEW_TIME_MINUTES));
	}
	
	/**
	 * 
	 * @param maxConcurrentCalls maximum number of concurrent calls to the onMessage handler
	 * @param autoComplete true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise.
	 * @param maxAutoRenewDuration - Maximum duration within which the client keeps renewing the message lock if the processing of the message is not completed by the handler.
	 */
	public MessageHandlerOptions(int maxConcurrentCalls, boolean autoComplete, Duration maxAutoRenewDuration)
	{		
		this.autoComplete = autoComplete;
		this.maxAutoRenewDuration = maxAutoRenewDuration;
		this.maxConcurrentCalls = maxConcurrentCalls;
	}	

	public boolean isAutoComplete() {
		return this.autoComplete;
	}	

	public int getMaxConcurrentCalls() {
		return this.maxConcurrentCalls;
	}

	public Duration getMaxAutoRenewDuration() {
		return this.maxAutoRenewDuration;
	}
}
