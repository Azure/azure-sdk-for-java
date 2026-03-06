// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Always-on lightweight console reporter that logs summary metrics every reporting interval.
 *
 * <p>Reports: total success rate, total failure rate, median latency, p99 latency, and CPU usage.
 * This matches the summary-level output of the original CodaHale ConsoleReporter on the main branch.</p>
 */
public class ConsoleSummaryReporter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleSummaryReporter.class);

    private static final String OP_CALLS_PREFIX = "cosmos.client.op.calls";
    private static final String OP_LATENCY_PREFIX = "cosmos.client.op.latency";

    private final MetricRegistry dropwizardRegistry;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;

    public ConsoleSummaryReporter(DropwizardBridgeMeterRegistry bridgeRegistry) {
        this.dropwizardRegistry = bridgeRegistry.getDropwizardRegistry();
        this.cpuReader = new CpuMemoryReader();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "console-summary-reporter");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
        logger.info("ConsoleSummaryReporter started (interval={}s)", unit.toSeconds(interval));
    }

    public void report() {
        double successRate = 0;
        double failureRate = 0;
        long totalCount = 0;

        for (Map.Entry<String, Meter> entry : dropwizardRegistry.getMeters().entrySet()) {
            if (!entry.getKey().contains(OP_CALLS_PREFIX)) {
                continue;
            }
            Meter meter = entry.getValue();
            if (isSuccessStatusInName(entry.getKey())) {
                successRate += meter.getOneMinuteRate();
            } else {
                failureRate += meter.getOneMinuteRate();
            }
            totalCount += meter.getCount();
        }

        double medianMs = 0;
        double p99Ms = 0;
        int timerCount = 0;
        for (Map.Entry<String, Timer> entry : dropwizardRegistry.getTimers().entrySet()) {
            if (!entry.getKey().contains(OP_LATENCY_PREFIX)) {
                continue;
            }
            Snapshot snapshot = entry.getValue().getSnapshot();
            // Dropwizard Timer reports in nanoseconds
            medianMs += snapshot.getMedian() / 1_000_000.0;
            p99Ms += snapshot.get99thPercentile() / 1_000_000.0;
            timerCount++;
        }

        if (totalCount == 0) {
            return;
        }

        float cpuUsage = cpuReader.getSystemWideCpuUsage();

        logger.info("[METRICS] successRate={:.1f}/s  failureRate={:.1f}/s  "
                + "medianMs={:.2f}  p99Ms={:.2f}  cpu={:.1f}%  totalOps={}",
            successRate, failureRate, medianMs, p99Ms, cpuUsage * 100, totalCount);
    }

    public void stop() {
        report();
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isSuccessStatusInName(String hierarchicalName) {
        String marker = ".OperationStatusCode.";
        int idx = hierarchicalName.indexOf(marker);
        if (idx < 0) {
            return false;
        }
        String rest = hierarchicalName.substring(idx + marker.length());
        int dotIdx = rest.indexOf('.');
        String statusCodeStr = dotIdx > 0 ? rest.substring(0, dotIdx) : rest;
        try {
            int code = Integer.parseInt(statusCodeStr);
            return code >= 200 && code < 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
