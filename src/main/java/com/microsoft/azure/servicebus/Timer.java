package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Timer {

	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(0);
	
	private Timer() { }

	public static void schedule(Runnable runnable, Duration timeout, TimerType timerType) {
		switch (timerType) {
		case OneTimeRun:
			executor.schedule(runnable, timeout.getSeconds(), TimeUnit.SECONDS);
			break;
		default:
			throw new UnsupportedOperationException("TODO: Other timer patterns");
		}
	}
}
