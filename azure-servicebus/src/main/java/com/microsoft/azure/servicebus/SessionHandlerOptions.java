package com.microsoft.azure.servicebus;

import java.time.Duration;

public final class SessionHandlerOptions {
	private static final boolean DEFAULT_AUTO_COMPLETE = true;
	private static final int DEFAULT_MAX_CONCURRENT_SESSIONS = 1;
	private static final int DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION = 1;
	private static final int DEFAULT_MAX_RENEW_TIME_MINUTES = 5;
		
	private boolean autoComplete;
	private Duration maxAutoRenewDuration;
	private int maxConcurrentSessions;
	private int maxConcurrentCallsPerSession;
	
	public SessionHandlerOptions()
	{
		this(DEFAULT_MAX_CONCURRENT_SESSIONS, DEFAULT_AUTO_COMPLETE, Duration.ofMinutes(DEFAULT_MAX_RENEW_TIME_MINUTES));
	}
	
	/**
	 * 
	 * @param maxConcurrentCalls
	 * @param autoComplete
	 * @param maxAutoRenewDuration - Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
	 * is not completed by the handler.
	 */
	public SessionHandlerOptions(int maxConcurrentSessions, boolean autoComplete, Duration maxAutoRenewDuration)
	{
		this(maxConcurrentSessions, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION, autoComplete, maxAutoRenewDuration);
	}
	
	/**
	 * 
	 * @param maxConcurrentSessions
	 * @param maxConcurrentCallsPerSession
	 * @param autoComplete
	 * @param maxAutoRenewDuration
	 */
	public SessionHandlerOptions(int maxConcurrentSessions, int maxConcurrentCallsPerSession, boolean autoComplete, Duration maxAutoRenewDuration)
	{
		this.maxConcurrentSessions = maxConcurrentSessions;
		this.maxConcurrentCallsPerSession = maxConcurrentCallsPerSession;
		this.autoComplete = autoComplete;
		this.maxAutoRenewDuration = maxAutoRenewDuration;
	}	

	public boolean isAutoComplete() {
		return this.autoComplete;
	}	

	public int getMaxConcurrentSessions() {
		return this.maxConcurrentSessions;
	}
	
	public int getMaxConcurrentCallsPerSession()
	{
		return this.maxConcurrentCallsPerSession;
	}

	public Duration getMaxAutoRenewDuration() {
		return this.maxAutoRenewDuration;
	}
}
