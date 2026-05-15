// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CSV metrics reporter that reads directly from the Micrometer {@link MeterRegistry}.
 *
 * <p>Unlike the previous implementation that bridged through a Dropwizard
 * {@code MetricRegistry}, this reporter iterates native Micrometer meters
 * at reporting time and writes CSV files — the same lightweight approach
 * used by {@link CosmosMetricsReporter}. This eliminates the per-{@code record()}
 * overhead of the Dropwizard bridge on the hot path.</p>
 *
 * <p>Each unique meter (name + tags) gets its own CSV file. A header row is
 * written on first access; subsequent intervals append data rows.</p>
 */
public class CsvMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvMetricsReporter.class);

    private static final String TIMER_HEADER = "t,count,mean_ms,max_ms,p50_ms,p90_ms,p95_ms,p99_ms";
    private static final String COUNTER_HEADER = "t,count";
    private static final String GAUGE_HEADER = "t,value";
    private static final String DISTRIBUTION_HEADER = "t,count,mean,max,p50,p90,p95,p99";

    private final MeterRegistry meterRegistry;
    private final File metricsDir;
    private final ScheduledExecutorService scheduler;
    private final Set<String> initializedFiles = ConcurrentHashMap.newKeySet();

    public CsvMetricsReporter(MeterRegistry meterRegistry, String reportingDirectory) {
        this.meterRegistry = meterRegistry;
        this.metricsDir = Paths.get(reportingDirectory, "metrics").toFile();
        this.metricsDir.mkdirs();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "csv-metrics-reporter");
            t.setDaemon(true);
            return t;
        });
        logger.info("CsvMetricsReporter started -> {}", metricsDir);
    }

    public void start(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    public void report() {
        String timestamp = Instant.now().toString();

        // Collect all rows per file, then flush once per file to minimize I/O syscalls
        Map<String, List<String>> pendingRows = new HashMap<>();

        for (Meter meter : meterRegistry.getMeters()) {
            try {
                if (meter instanceof Timer) {
                    reportTimer(timestamp, (Timer) meter, pendingRows);
                } else if (meter instanceof Counter) {
                    reportCounter(timestamp, (Counter) meter, pendingRows);
                } else if (meter instanceof Gauge) {
                    reportGauge(timestamp, (Gauge) meter, pendingRows);
                } else if (meter instanceof DistributionSummary) {
                    reportDistributionSummary(timestamp, (DistributionSummary) meter, pendingRows);
                }
            } catch (Exception e) {
                logger.warn("Failed to write metric: {}", meter.getId().getName(), e);
            }
        }

        flushRows(pendingRows);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        report();
    }

    private void reportTimer(String timestamp, Timer timer, Map<String, List<String>> pendingRows) {
        if (timer.count() == 0) {
            return;
        }

        String fileName = csvFileName(timer);
        HistogramSnapshot snapshot = timer.takeSnapshot();

        double p50 = 0, p90 = 0, p95 = 0, p99 = 0;
        for (ValueAtPercentile vp : snapshot.percentileValues()) {
            double p = vp.percentile();
            if (p == 0.5) {
                p50 = vp.value(TimeUnit.MILLISECONDS);
            } else if (p == 0.9) {
                p90 = vp.value(TimeUnit.MILLISECONDS);
            } else if (p == 0.95) {
                p95 = vp.value(TimeUnit.MILLISECONDS);
            } else if (p == 0.99) {
                p99 = vp.value(TimeUnit.MILLISECONDS);
            }
        }

        addRow(pendingRows, fileName, TIMER_HEADER,
            String.format(Locale.US, "%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                timestamp,
                timer.count(),
                timer.mean(TimeUnit.MILLISECONDS),
                snapshot.max(TimeUnit.MILLISECONDS),
                p50, p90, p95, p99));
    }

    private void reportCounter(String timestamp, Counter counter, Map<String, List<String>> pendingRows) {
        if (counter.count() == 0) {
            return;
        }

        String fileName = csvFileName(counter);
        addRow(pendingRows, fileName, COUNTER_HEADER,
            String.format(Locale.US, "%s,%d", timestamp, (long) counter.count()));
    }

    private void reportGauge(String timestamp, Gauge gauge, Map<String, List<String>> pendingRows) {
        double value = gauge.value();
        if (Double.isNaN(value)) {
            return;
        }

        String fileName = csvFileName(gauge);
        addRow(pendingRows, fileName, GAUGE_HEADER,
            String.format(Locale.US, "%s,%.2f", timestamp, value));
    }

    private void reportDistributionSummary(String timestamp, DistributionSummary summary,
                                           Map<String, List<String>> pendingRows) {
        if (summary.count() == 0) {
            return;
        }

        String fileName = csvFileName(summary);
        HistogramSnapshot snapshot = summary.takeSnapshot();

        double p50 = 0, p90 = 0, p95 = 0, p99 = 0;
        for (ValueAtPercentile vp : snapshot.percentileValues()) {
            double p = vp.percentile();
            if (p == 0.5) {
                p50 = vp.value();
            } else if (p == 0.9) {
                p90 = vp.value();
            } else if (p == 0.95) {
                p95 = vp.value();
            } else if (p == 0.99) {
                p99 = vp.value();
            }
        }

        addRow(pendingRows, fileName, DISTRIBUTION_HEADER,
            String.format(Locale.US, "%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                timestamp,
                summary.count(),
                summary.mean(),
                snapshot.max(),
                p50, p90, p95, p99));
    }

    private String csvFileName(Meter meter) {
        StringBuilder sb = new StringBuilder(meter.getId().getName());
        for (Tag tag : meter.getId().getTags()) {
            sb.append('.').append(tag.getKey()).append('.').append(tag.getValue());
        }
        return sb.toString().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void addRow(Map<String, List<String>> pendingRows, String fileName, String header, String row) {
        List<String> rows = pendingRows.computeIfAbsent(fileName, k -> {
            List<String> list = new ArrayList<>();
            // Add header as first row if this file hasn't been initialized yet
            if (initializedFiles.add(fileName)) {
                list.add(header);
            }
            return list;
        });
        rows.add(row);
    }

    private void flushRows(Map<String, List<String>> pendingRows) {
        for (Map.Entry<String, List<String>> entry : pendingRows.entrySet()) {
            File file = new File(metricsDir, entry.getKey() + ".csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                for (String line : entry.getValue()) {
                    writer.println(line);
                }
            } catch (IOException e) {
                logger.warn("Failed to write to CSV file: {}", file, e);
            }
        }
    }
}
