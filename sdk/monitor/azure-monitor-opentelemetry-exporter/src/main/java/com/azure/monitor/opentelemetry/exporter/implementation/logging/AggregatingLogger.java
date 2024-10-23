// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class AggregatingLogger {

    static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
        ThreadPoolUtils.createDaemonThreadFactory(AggregatingLogger.class, "aggregating logger"));

    private final Logger logger;
    private final String grouping;

    // Period for scheduled executor
    private final int intervalSeconds;

    private final boolean trackingOperations;

    private final AtomicBoolean firstFailure = new AtomicBoolean();

    // number of successes and failures in the 5-min window
    private long numSuccesses;

    // using MutableLong for two purposes
    // * so we don't need to get and set into map each time we want to increment
    // * avoid autoboxing for values above 128
    private Map<String, MutableLong> failureMessages = new HashMap<>();

    private final Object lock = new Object();

    AggregatingLogger(Class<?> source, String operation, boolean trackingOperations) {
        this(source, operation, trackingOperations, 300);
    }

    // visible for testing
    AggregatingLogger(Class<?> source, String operation, boolean trackingOperations, int intervalSeconds) {
        logger = LoggerFactory.getLogger(source);
        this.grouping = operation;
        this.trackingOperations = trackingOperations;
        this.intervalSeconds = intervalSeconds;
    }

    void recordSuccess() {
        synchronized (lock) {
            numSuccesses++;
        }
    }

    // warningMessage should have low cardinality
    void recordWarning(String warningMessage) {
        recordWarning(warningMessage, null);
    }

    // warningMessage should have low cardinality
    void recordWarning(String warningMessage, @Nullable Throwable exception) {
        if (!firstFailure.getAndSet(true)) {
            // log the first time we see an exception as soon as it occurs, along with full stack trace
            logger.warn("{}: {} (future warnings will be aggregated and logged once every {} minutes)", grouping,
                warningMessage, intervalSeconds / 60, exception);
            scheduledExecutor.scheduleWithFixedDelay(new ExceptionStatsLogger(), intervalSeconds, intervalSeconds,
                TimeUnit.SECONDS);
            return;
        }

        logger.debug("{} {}", grouping, warningMessage, exception);

        synchronized (lock) {
            if (failureMessages.size() < 10) {
                failureMessages.computeIfAbsent(warningMessage, key -> new MutableLong()).increment();
            } else {
                // we have a cardinality problem and don't want to spam the logger
                // (or consume too much memory)
                MutableLong count = failureMessages.get(warningMessage);
                if (count != null) {
                    count.increment();
                } else {
                    failureMessages.computeIfAbsent("other", key -> new MutableLong()).increment();
                }
            }
        }
    }

    private static class MutableLong implements Comparable<MutableLong> {
        private long value;

        private void increment() {
            value++;
        }

        @Override
        public int compareTo(MutableLong other) {
            return Long.compare(value, other.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MutableLong)) {
                return false;
            }
            MutableLong that = (MutableLong) obj;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private class ExceptionStatsLogger implements Runnable {

        @Override
        public void run() {
            long numSuccesses;
            Map<String, MutableLong> failureMessages;
            // grab quickly and reset under lock (do not perform logging under lock)
            synchronized (lock) {
                numSuccesses = AggregatingLogger.this.numSuccesses;
                failureMessages = AggregatingLogger.this.failureMessages;

                AggregatingLogger.this.numSuccesses = 0;
                AggregatingLogger.this.failureMessages = new HashMap<>();
            }
            if (!failureMessages.isEmpty()) {
                long numWarnings = getTotalFailures(failureMessages);
                long numMinutes = AggregatingLogger.this.intervalSeconds / 60;
                long total = numSuccesses + numWarnings;
                StringBuilder message = new StringBuilder();
                message.append("In the last ");
                message.append(numMinutes);
                message.append(" minutes, the following");
                if (trackingOperations) {
                    message.append(" operation has failed ");
                    message.append(numWarnings);
                    message.append(" times (out of ");
                    message.append(total);
                    message.append("): ");
                } else {
                    message.append(" warning has occurred ");
                    message.append(numWarnings);
                    message.append(" times: ");
                }
                message.append(grouping);
                message.append(":");
                failureMessages.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEach(entry -> {
                        message.append(System.lineSeparator());
                        message.append(" * ");
                        message.append(entry.getKey());
                        message.append(" (");
                        message.append(entry.getValue().value);
                        message.append(" times)");
                    });
                logger.warn(message.toString());
            }
        }
    }

    private static long getTotalFailures(Map<String, MutableLong> failureMessages) {
        long total = 0;
        for (MutableLong value : failureMessages.values()) {
            total += value.value;
        }
        return total;
    }
}
