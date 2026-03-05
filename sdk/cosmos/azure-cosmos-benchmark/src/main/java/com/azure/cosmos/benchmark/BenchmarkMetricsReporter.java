// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Periodically reads SDK-emitted Micrometer meters from a shared {@link MeterRegistry}
 * and reports them to console (SLF4J) or CSV files.
 *
 * <p>This replaces the CodaHale {@code ConsoleReporter} / {@code CsvReporter} combination.
 * Unlike the CodaHale reporters, this class reads the SDK's own {@code cosmos.client.op.*}
 * meters rather than maintaining separate application-level metrics.</p>
 */
public class BenchmarkMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkMetricsReporter.class);

    private static final String COSMOS_OP_PREFIX = "cosmos.client.op.";
    private static final String COSMOS_SYSTEM_PREFIX = "cosmos.client.system.";

    private static final String CSV_HEADER =
        "timestamp,meter_name,type,tags,count,mean_rate,m1_rate,p50,p95,p99,max,unit";

    private final MeterRegistry registry;
    private final Path csvOutputDir;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private BufferedWriter csvWriter;

    /**
     * Create a reporter that writes to CSV files in the given directory.
     * If {@code csvOutputDir} is null, reports to console (SLF4J) instead.
     */
    public BenchmarkMetricsReporter(MeterRegistry registry, Path csvOutputDir) {
        this.registry = registry;
        this.csvOutputDir = csvOutputDir;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "benchmark-metrics-reporter");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Start periodic reporting.
     *
     * @param interval reporting interval
     * @param unit     time unit
     */
    public void start(long interval, TimeUnit unit) {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        if (csvOutputDir != null) {
            try {
                Files.createDirectories(csvOutputDir);
                Path csvFile = csvOutputDir.resolve("benchmark-metrics.csv");
                csvWriter = Files.newBufferedWriter(csvFile,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                csvWriter.write(CSV_HEADER);
                csvWriter.newLine();
                csvWriter.flush();
                logger.info("BenchmarkMetricsReporter started (CSV) -> {}", csvFile);
            } catch (IOException e) {
                logger.error("Failed to create benchmark metrics CSV", e);
                return;
            }
        } else {
            logger.info("BenchmarkMetricsReporter started (console)");
        }

        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    /**
     * Force a single report snapshot.
     */
    public void report() {
        String timestamp = Instant.now().toString();

        List<Meter> relevantMeters = registry.getMeters().stream()
            .filter(m -> {
                String name = m.getId().getName();
                return name.startsWith(COSMOS_OP_PREFIX)
                    || name.startsWith(COSMOS_SYSTEM_PREFIX)
                    || name.startsWith("jvm.");
            })
            .collect(Collectors.toList());

        if (relevantMeters.isEmpty()) {
            return;
        }

        for (Meter meter : relevantMeters) {
            if (meter instanceof Counter) {
                reportCounter(timestamp, (Counter) meter);
            } else if (meter instanceof Timer) {
                reportTimer(timestamp, (Timer) meter);
            } else if (meter instanceof Gauge) {
                reportGauge(timestamp, (Gauge) meter);
            } else if (meter instanceof io.micrometer.core.instrument.DistributionSummary) {
                reportDistributionSummary(timestamp, (io.micrometer.core.instrument.DistributionSummary) meter);
            }
        }

        if (csvWriter != null) {
            try {
                csvWriter.flush();
            } catch (IOException e) {
                logger.warn("Failed to flush benchmark metrics CSV", e);
            }
        }
    }

    /**
     * Stop the reporter and close any open files.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        report();

        if (csvWriter != null) {
            try {
                csvWriter.close();
                logger.info("BenchmarkMetricsReporter stopped");
            } catch (IOException e) {
                logger.warn("Failed to close benchmark metrics CSV", e);
            }
        }
    }

    private void reportCounter(String timestamp, Counter counter) {
        String name = counter.getId().getName();
        String tags = formatTags(counter);
        double count = counter.count();

        if (csvWriter != null) {
            writeCsvLine(timestamp, name, "counter", tags,
                String.format("%.0f", count), "", "", "", "", "", "", "");
        } else {
            logger.info("  {} [{}] count={}", name, tags, (long) count);
        }
    }

    private void reportTimer(String timestamp, Timer timer) {
        String name = timer.getId().getName();
        String tags = formatTags(timer);
        long count = timer.count();
        double mean = timer.mean(TimeUnit.MILLISECONDS);
        HistogramSnapshot snapshot = timer.takeSnapshot();
        ValueAtPercentile[] percentiles = snapshot.percentileValues();

        double p50 = 0, p95 = 0, p99 = 0;
        for (ValueAtPercentile vp : percentiles) {
            double pctile = vp.percentile();
            double valueMs = vp.value(TimeUnit.MILLISECONDS);
            if (pctile == 0.5) p50 = valueMs;
            else if (pctile == 0.95) p95 = valueMs;
            else if (pctile == 0.99) p99 = valueMs;
        }
        double max = snapshot.max(TimeUnit.MILLISECONDS);

        if (csvWriter != null) {
            writeCsvLine(timestamp, name, "timer", tags,
                String.valueOf(count), "", "",
                String.format("%.2f", p50), String.format("%.2f", p95),
                String.format("%.2f", p99), String.format("%.2f", max), "ms");
        } else {
            logger.info("  {} [{}] count={} mean={:.2f}ms p50={:.2f}ms p95={:.2f}ms p99={:.2f}ms max={:.2f}ms",
                name, tags, count, mean, p50, p95, p99, max);
        }
    }

    private void reportGauge(String timestamp, Gauge gauge) {
        String name = gauge.getId().getName();
        String tags = formatTags(gauge);
        double value = gauge.value();

        if (csvWriter != null) {
            writeCsvLine(timestamp, name, "gauge", tags,
                "", "", "", "", "", "", String.format("%.2f", value), "");
        } else {
            logger.info("  {} [{}] value={}", name, tags, value);
        }
    }

    private void reportDistributionSummary(String timestamp,
                                           io.micrometer.core.instrument.DistributionSummary summary) {
        String name = summary.getId().getName();
        String tags = formatTags(summary);
        long count = summary.count();
        double mean = summary.mean();
        HistogramSnapshot snapshot = summary.takeSnapshot();
        ValueAtPercentile[] percentiles = snapshot.percentileValues();

        double p50 = 0, p95 = 0, p99 = 0;
        for (ValueAtPercentile vp : percentiles) {
            double pctile = vp.percentile();
            if (pctile == 0.5) p50 = vp.value();
            else if (pctile == 0.95) p95 = vp.value();
            else if (pctile == 0.99) p99 = vp.value();
        }
        double max = snapshot.max();

        if (csvWriter != null) {
            writeCsvLine(timestamp, name, "distribution", tags,
                String.valueOf(count), "", "",
                String.format("%.2f", p50), String.format("%.2f", p95),
                String.format("%.2f", p99), String.format("%.2f", max), "");
        } else {
            logger.info("  {} [{}] count={} mean={:.2f} p50={:.2f} p95={:.2f} p99={:.2f} max={:.2f}",
                name, tags, count, mean, p50, p95, p99, max);
        }
    }

    private String formatTags(Meter meter) {
        return meter.getId().getTags().stream()
            .map(tag -> tag.getKey() + "=" + tag.getValue())
            .collect(Collectors.joining(";"));
    }

    private void writeCsvLine(String... fields) {
        if (csvWriter == null) {
            return;
        }
        try {
            csvWriter.write(String.join(",", fields));
            csvWriter.newLine();
        } catch (IOException e) {
            logger.warn("Failed to write benchmark metrics CSV line", e);
        }
    }
}
