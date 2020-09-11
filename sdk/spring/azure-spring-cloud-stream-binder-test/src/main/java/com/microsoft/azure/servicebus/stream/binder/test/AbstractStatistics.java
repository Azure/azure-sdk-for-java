// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.test;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@ThreadSafe
public abstract class AbstractStatistics {
    private final int size;
    private final Statistics throughput;
    private final Statistics numberMessagePerSec;
    private final long reportingInterval;
    private final LongAdder totalMessages;
    private final LongAdder totalBytes;
    private final ScheduledExecutorService scheduler;

    public AbstractStatistics(int size, long reportingInterval, String labelPrefix) {
        this.size = size;
        this.throughput = new Statistics(labelPrefix + "Throughput KB/sec");
        this.numberMessagePerSec = new Statistics(labelPrefix + "nMsg/sec");
        this.reportingInterval = reportingInterval;
        this.totalMessages = new LongAdder();
        this.totalBytes = new LongAdder();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(new ReportingTask(), this.reportingInterval, this.reportingInterval,
                TimeUnit.MILLISECONDS);
    }

    public void record(long messageSize) {
        totalMessages.increment();
        totalBytes.add(messageSize);
    }

    public void printSummary() {
        this.throughput.printSummary();
        this.numberMessagePerSec.printSummary();
    }

    protected void complete() {
    }

    class ReportingTask implements Runnable {
        private final AtomicLong lastReportingTime;
        private final AtomicLong lastMessages;
        private final AtomicLong lastBytes;

        ReportingTask() {
            this.lastReportingTime = new AtomicLong(System.currentTimeMillis());
            this.lastMessages = new AtomicLong(0);
            this.lastBytes = new AtomicLong(0);
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long messages = totalMessages.longValue();
            long bytes = totalBytes.longValue();

            long elapsedMs = now - lastReportingTime.get();
            double windowKbRead = ((bytes - lastBytes.get()) * 1.0) / (1024);
            double windowKbPerSec = 1000.0 * windowKbRead / elapsedMs;
            long windowMessage = messages - lastMessages.get();
            double windowMessagesPerSec = (windowMessage * 1.0 / elapsedMs) * 1000.0;

            if (windowMessage > 0) {
                throughput.record(windowKbPerSec);
                numberMessagePerSec.record(windowMessagesPerSec);
            }

            lastReportingTime.set(now);
            lastBytes.set(bytes);
            lastMessages.set(messages);

            System.out.printf("Total %d records, %.1f records/sec (%.4f KB/sec).%n", messages, windowMessagesPerSec,
                    windowKbPerSec);

            if (messages >= size) {
                printSummary();
                complete();
            }
        }
    }
}
