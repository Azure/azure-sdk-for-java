// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstraction for a Scheduler functionality - which can later be replaced by a light-weight Thread
 */
public final class Timer {
    private static ScheduledExecutorService executor = null;

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(Timer.class);
    private static final HashSet<String> REFERENCES = new HashSet<>();
    private static final Object SYNC_REFERENCES = new Object();

    private Timer() {
    }


    // runFrequency implemented only for TimeUnit granularity - Seconds
    public static ScheduledFuture<?> schedule(Runnable runnable, Duration runFrequency, TimerType timerType) {
        switch (timerType) {
            case OneTimeRun:
                return executor.schedule(runnable, runFrequency.toMillis(), TimeUnit.MILLISECONDS);
            case RepeatRun:
                return executor.scheduleWithFixedDelay(runnable, runFrequency.toMillis(), runFrequency.toMillis(), TimeUnit.MILLISECONDS);
            default:
                throw new UnsupportedOperationException("Unsupported timer pattern.");
        }
    }

    static void register(final String clientId) {
        synchronized (SYNC_REFERENCES) {
            if (REFERENCES.size() == 0 && (executor == null || executor.isShutdown())) {
                final int corePoolSize = Math.max(Runtime.getRuntime().availableProcessors(), 4);
                TRACE_LOGGER.debug("Starting ScheduledThreadPoolExecutor with coreThreadPoolSize:{}", corePoolSize);

                executor = Executors.newScheduledThreadPool(corePoolSize);
            }

            REFERENCES.add(clientId);
        }
    }

    static void unregister(final String clientId) {
        synchronized (SYNC_REFERENCES) {
            if (REFERENCES.remove(clientId) && REFERENCES.size() == 0 && executor != null) {
                TRACE_LOGGER.debug("Shuting down ScheduledThreadPoolExecutor");
                executor.shutdownNow();
            }
        }
    }
}
