package com.microsoft.azure.servicebus;

import java.time.*;

public class TimeoutTracker
{
	private final Duration originalTimeout;
	private boolean isTimerStarted;
	private Instant startTime;
	
	/**
	 * @param timeout original operationTimeout
	 * @param startTrackingTimeout whether/not to start the timeout tracking - right now. if not started now, timer tracking will start upon the first call to {@link TimeoutTracker#elapsed()}/{@link TimeoutTracker#remaining()} 
	 */
	public TimeoutTracker(Duration timeout, boolean startTrackingTimeout)
	{
		if (timeout.compareTo(Duration.ZERO) < 0)
		{
			throw new IllegalArgumentException("timeout should be non-negative");
		}
		
		this.originalTimeout = timeout;
		
		if (startTrackingTimeout)
		{
			this.startTime = Instant.now();
		}
		
		this.isTimerStarted = startTrackingTimeout;
	}
	
	public static TimeoutTracker create(Duration timeout)
	{
		return new TimeoutTracker(timeout, true);
	}

	public Duration remaining()
	{
		return this.originalTimeout.minus(this.elapsed());
	}
	
	public Duration elapsed()
	{
		if (!this.isTimerStarted)
		{
			this.startTime = Instant.now();
			this.isTimerStarted = true;
		}
		
		return Duration.between(this.startTime, Instant.now());
	}
}
