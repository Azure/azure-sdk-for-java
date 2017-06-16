package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.Locale;

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
	 * @param maxConcurrentSessions maximum number of concurrent sessions accepted by the session pump
	 * @param autoComplete true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise.
	 * @param maxAutoRenewDuration - Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
	 * is not completed by the handler.
	 */
	public SessionHandlerOptions(int maxConcurrentSessions, boolean autoComplete, Duration maxAutoRenewDuration)
	{
		this(maxConcurrentSessions, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION, autoComplete, maxAutoRenewDuration);
	}
	
	/**
	 * 
	 * @param maxConcurrentSessions maximum number of concurrent sessions accepted by the session pump
	 * @param maxConcurrentCallsPerSession maximum number of concurrent calls to the onMessage handler
	 * @param autoComplete true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise
	 * @param maxAutoRenewDuration Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
     * is not completed by the handler.
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
	
	@Override
    public String toString()
    {
        return String.format(Locale.US, "SessionHandlerOptions - AutoComplete:%s, MaxConcurrentSessions:%s, MaxConcurretnCallsPerSession:%s, MaxAutoRenewDuration:%s", this.autoComplete, this.maxConcurrentSessions, this.maxConcurrentCallsPerSession, this.maxAutoRenewDuration);
    }
}
