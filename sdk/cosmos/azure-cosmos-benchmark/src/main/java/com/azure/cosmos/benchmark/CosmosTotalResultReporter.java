// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically reads SDK-emitted Micrometer metrics from the shared {@link MeterRegistry}
 * and uploads aggregated benchmark results to a Cosmos DB container.
 *
 * <p>Tracks rolling success/failure rates (computed from counter deltas between report intervals)
 * and latency percentiles from the SDK's operation timer.</p>
 */
public class CosmosTotalResultReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTotalResultReporter.class);
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // SDK metric names
    private static final String OP_CALLS_METRIC = "cosmos.client.op.calls";
    private static final String OP_LATENCY_METRIC = "cosmos.client.op.latency";

    private final MeterRegistry registry;
    private final CosmosContainer results;
    private final String operation;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;
    private final int concurrency;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;

    // Internal histograms for rolling aggregation (simple arrays of samples)
    private final java.util.List<Double> successRateSamples = new java.util.ArrayList<>();
    private final java.util.List<Double> failureRateSamples = new java.util.ArrayList<>();
    private final java.util.List<Double> medianLatencySamples = new java.util.ArrayList<>();
    private final java.util.List<Double> p99LatencySamples = new java.util.ArrayList<>();
    private final java.util.List<Double> cpuUsageSamples = new java.util.ArrayList<>();

    private Instant lastRecorded;
    private double lastRecordedSuccessCount;
    private double lastRecordedFailureCount;

    private CosmosTotalResultReporter(
        MeterRegistry registry,
        CosmosContainer results,
        String operation,
        String testVariationName,
        String branchName,
        String commitId,
        int concurrency) {

        this.registry = registry;
        this.results = results;
        this.operation = operation;
        this.testVariationName = testVariationName != null ? testVariationName : "";
        this.branchName = branchName != null ? branchName : "";
        this.commitId = commitId != null ? commitId : "";
        this.concurrency = concurrency;
        this.cpuReader = new CpuMemoryReader();
        this.lastRecorded = Instant.now();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cosmos-result-reporter");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Start periodic reporting.
     */
    public void start(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    /**
     * Collect one sample of metrics for rolling aggregation.
     */
    public void report() {
        // Compute success/failure rates from counter deltas
        double successCount = sumCountersByName(OP_CALLS_METRIC, true);
        double failureCount = sumCountersByName(OP_CALLS_METRIC, false);

        Instant now = Instant.now();
        double intervalInSeconds = Duration.between(lastRecorded, now).toMillis() / 1000.0;

        if (intervalInSeconds > 0) {
            cpuUsageSamples.add(cpuReader.getSystemWideCpuUsage());

            if (successCount == 0 && failureCount == 0) {
                lastRecorded = now;
                return;
            }

            double successRate = (successCount - lastRecordedSuccessCount) / intervalInSeconds;
            double failureRate = (failureCount - lastRecordedFailureCount) / intervalInSeconds;
            successRateSamples.add(successRate);
            failureRateSamples.add(failureRate);

            lastRecordedSuccessCount = successCount;
            lastRecordedFailureCount = failureCount;
            lastRecorded = now;

            // Read latency percentiles from SDK timer
            Timer latencyTimer = findTimerByName(OP_LATENCY_METRIC);
            if (latencyTimer != null) {
                HistogramSnapshot snapshot = latencyTimer.takeSnapshot();
                double median = 0, p99 = 0;
                for (ValueAtPercentile vp : snapshot.percentileValues()) {
                    if (vp.percentile() == 0.5) median = vp.value(TimeUnit.MILLISECONDS);
                    else if (vp.percentile() == 0.99) p99 = vp.value(TimeUnit.MILLISECONDS);
                }

                if (median > 0) {
                    medianLatencySamples.add(median);
                    p99LatencySamples.add(p99);
                }
            }
        }
    }

    /**
     * Stop reporting and upload final aggregated results.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        report();
        uploadFinalResults();
    }

    private void uploadFinalResults() {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnn")
            .withZone(ZoneId.from(ZoneOffset.UTC));

        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("TIMESTAMP", formatter.format(Instant.now()).substring(0, 27));
        doc.put("Operation", this.operation);
        doc.put("TestVariationName", this.testVariationName);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("Concurrency", this.concurrency);
        doc.put("CpuUsage", percentile75(cpuUsageSamples));
        doc.put("SuccessRate", percentile75(successRateSamples));
        doc.put("FailureRate", percentile75(failureRateSamples));

        double p99 = new BigDecimal(Double.toString(percentile75(p99LatencySamples)))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
        double median = new BigDecimal(Double.toString(percentile75(medianLatencySamples)))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();

        doc.put("P99LatencyInMs", p99);
        doc.put("MedianLatencyInMs", median);

        results.createItem(doc, new PartitionKey(id), null);
        LOGGER.info("Final results uploaded to {} - {}", results.getId(), doc.toPrettyString());
    }

    private double sumCountersByName(String metricName, boolean successOnly) {
        double total = 0;
        for (Counter counter : registry.find(metricName).counters()) {
            String statusCode = counter.getId().getTag("OperationStatusCode");
            if (statusCode == null) {
                continue;
            }
            boolean isSuccess = statusCode.startsWith("2");
            if (successOnly == isSuccess) {
                total += counter.count();
            }
        }
        return total;
    }

    private Timer findTimerByName(String metricName) {
        return registry.find(metricName).timer();
    }

    private static double percentile75(java.util.List<Double> samples) {
        if (samples.isEmpty()) {
            return 0;
        }
        java.util.List<Double> sorted = new java.util.ArrayList<>(samples);
        java.util.Collections.sort(sorted);
        int index = (int) Math.ceil(0.75 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    /**
     * Returns a new {@link Builder} for {@link CosmosTotalResultReporter}.
     */
    public static Builder forRegistry(MeterRegistry registry, CosmosContainer resultsContainer,
                                      String operation, String testVariationName,
                                      String branchName, String commitId, int concurrency) {
        return new Builder(registry, resultsContainer, operation, testVariationName,
            branchName, commitId, concurrency);
    }

    /**
     * A builder for {@link CosmosTotalResultReporter} instances.
     */
    public static class Builder {
        private final MeterRegistry registry;
        private final CosmosContainer resultsContainer;
        private final String operation;
        private final String testVariationName;
        private final String branchName;
        private final String commitId;
        private final int concurrency;

        private Builder(MeterRegistry registry, CosmosContainer resultsContainer,
                        String operation, String testVariationName,
                        String branchName, String commitId, int concurrency) {
            this.registry = registry;
            this.resultsContainer = resultsContainer;
            this.operation = operation;
            this.testVariationName = testVariationName;
            this.branchName = branchName;
            this.commitId = commitId;
            this.concurrency = concurrency;
        }

        /**
         * Builds a {@link CosmosTotalResultReporter} with the given properties.
         */
        public CosmosTotalResultReporter build() {
            return new CosmosTotalResultReporter(
                registry, resultsContainer, operation, testVariationName,
                branchName, commitId, concurrency);
        }
    }
}
