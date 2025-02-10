// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class WarningLogger {

    private final Logger logger;
    private final String message;
    private final AtomicLong numWarnings = new AtomicLong();

    // Period for scheduled executor
    private final int intervalSeconds;

    private final AtomicBoolean firstFailure = new AtomicBoolean();

    public WarningLogger(Class<?> source, String message) {
        this(source, message, 300);
    }

    // visible for testing
    WarningLogger(Class<?> source, String message, int intervalSeconds) {
        logger = LoggerFactory.getLogger(source);
        this.message = message;
        this.intervalSeconds = intervalSeconds;
    }

    public void recordWarning() {
        recordWarning(null);
    }

    public void recordWarning(@Nullable Throwable exception) {
        if (!firstFailure.getAndSet(true)) {
            // log the first time we see an exception as soon as it occurs, along with full stack trace
            logger.warn("{} (future warnings will be aggregated and logged once every {} minutes)", message,
                intervalSeconds / 60, exception);
            AggregatingLogger.scheduledExecutor.scheduleWithFixedDelay(new ExceptionStatsLogger(), intervalSeconds,
                intervalSeconds, TimeUnit.SECONDS);
            return;
        }

        logger.debug(message, exception);

        numWarnings.incrementAndGet();
    }

    private class ExceptionStatsLogger implements Runnable {

        @Override
        public void run() {
            long numWarnings = WarningLogger.this.numWarnings.getAndSet(0);
            if (numWarnings == 0) {
                return;
            }
            long numMinutes = WarningLogger.this.intervalSeconds / 60;
            logger.warn("In the last {} minutes, the following warning has occurred {} times: {}", numMinutes,
                numWarnings, message);
        }
    }
}
