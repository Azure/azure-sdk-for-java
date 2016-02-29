/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * An abstraction for a Scheduler functionality - which can later be replaced by a light-weight Thread
 */
public final class Timer
{
	private static final ScheduledThreadPoolExecutor executor =
			new ScheduledThreadPoolExecutor(Math.min(Runtime.getRuntime().availableProcessors(), 2));
	
	private Timer() 
	{
	}

	/**
	 * @param runFrequency implemented only for TimeUnit granularity - Seconds
	 */
	public static void schedule(Runnable runnable, Duration runFrequency, TimerType timerType)
	{
		switch (timerType)
		{
			case OneTimeRun:
				long seconds = runFrequency.getSeconds();
				if (seconds > 0)
					executor.schedule(runnable, seconds, TimeUnit.SECONDS);
				else
					executor.schedule(runnable, runFrequency.toMillis(), TimeUnit.MILLISECONDS);
				break;
			
			case RepeatRun:
				executor.scheduleWithFixedDelay(runnable, runFrequency.getSeconds(), runFrequency.getSeconds(), TimeUnit.SECONDS);
				break;
				
			default:
				throw new UnsupportedOperationException("Unsupported timer pattern.");
		}
	}
}
