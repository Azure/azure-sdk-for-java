// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
// Note: CpuMemoryReader is an SDK internal API. Acceptable for benchmark tooling
// which already depends on SDK internals (e.g., ImplementationBridgeHelpers).
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Always-on lightweight console reporter that logs summary metrics every reporting interval.
 *
 * <p>Reports: total success rate, total failure rate, median latency, p99 latency, and CPU usage.
 * Uses Micrometer's search API on the bridge registry for clean tag-based filtering.</p>
 */
public class ConsoleSummaryReporter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleSummaryReporter.class);

    private static final String OP_CALLS_METRIC = "cosmos.client.op.calls";
    private static final String OP_LATENCY_METRIC = "cosmos.client.op.latency";

    private final MeterRegistry meterRegistry;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;

    public ConsoleSummaryReporter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
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
        // Use Micrometer search API for clean tag-based filtering
        Collection<Counter> callCounters = meterRegistry.find(OP_CALLS_METRIC).counters();

        double successCount = 0;
        double failureCount = 0;
        for (Counter counter : callCounters) {
            String statusCode = counter.getId().getTag("OperationStatusCode");
            if (statusCode == null) {
                continue;
            }
            try {
                int code = Integer.parseInt(statusCode);
                if (code >= 200 && code < 300) {
                    successCount += counter.count();
                } else {
                    failureCount += counter.count();
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }

        if (successCount == 0 && failureCount == 0) {
            return;
        }

        // Find the timer with the most observations for representative latency percentiles
        double medianMs = 0;
        double p99Ms = 0;
        long maxTimerCount = 0;
        Collection<Timer> latencyTimers = meterRegistry.find(OP_LATENCY_METRIC).timers();
        for (Timer timer : latencyTimers) {
            if (timer.count() > maxTimerCount) {
                maxTimerCount = timer.count();
                HistogramSnapshot snapshot = timer.takeSnapshot();
                for (ValueAtPercentile vp : snapshot.percentileValues()) {
                    if (vp.percentile() == 0.5) medianMs = vp.value(TimeUnit.MILLISECONDS);
                    else if (vp.percentile() == 0.99) p99Ms = vp.value(TimeUnit.MILLISECONDS);
                }
            }
        }

        float cpuUsage = cpuReader.getSystemWideCpuUsage();

        logger.info("[METRICS] successOps={}  failureOps={}  "
                + "medianMs={}  p99Ms={}  cpu={}%",
            String.format("%.0f", successCount),
            String.format("%.0f", failureCount),
            String.format("%.2f", medianMs),
            String.format("%.2f", p99Ms),
            String.format("%.1f", cpuUsage * 100));
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
}
