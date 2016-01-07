package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An abstraction for a Scheduler functionality - which can later be replaced by a light-weight Thread
 */
public final class Timer
{
	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(0);
	
	private Timer() 
	{
	}

	public static void schedule(Runnable runnable, Duration runAfter, TimerType timerType)
	{
		switch (timerType)
		{
			case OneTimeRun:
				executor.schedule(runnable, runAfter.getSeconds(), TimeUnit.SECONDS);
				break;
				
			case RepeatRun:
				executor.scheduleAtFixedRate(runnable, runAfter.getSeconds(), runAfter.getSeconds(), TimeUnit.SECONDS);
				break;
				
			default:
				throw new UnsupportedOperationException("TODO: Other timer patterns");
		}
	}
}
