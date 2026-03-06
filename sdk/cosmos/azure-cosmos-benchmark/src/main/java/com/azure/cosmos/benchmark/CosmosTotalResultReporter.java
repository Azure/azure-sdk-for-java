// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically samples SDK metrics from the Dropwizard bridge registry and on shutdown
 * uploads aggregated benchmark results to a Cosmos DB container.
 *
 * <p>Uses native Dropwizard {@link com.codahale.metrics.Meter} rates and
 * {@link com.codahale.metrics.Timer} percentiles — no manual rate/percentile
 * computation needed.</p>
 */
public class CosmosTotalResultReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTotalResultReporter.class);
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // SDK metric name prefix (bridged via HierarchicalNameMapper)
    private static final String OP_CALLS_PREFIX = "cosmos.client.op.calls";
    private static final String OP_LATENCY_PREFIX = "cosmos.client.op.latency";

    private final MetricRegistry dropwizardRegistry;
    private final CosmosContainer results;
    private final String operation;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;
    private final int concurrency;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;

    private CosmosTotalResultReporter(
        DropwizardBridgeMeterRegistry bridgeRegistry,
        CosmosContainer results,
        String operation,
        String testVariationName,
        String branchName,
        String commitId,
        int concurrency) {

        this.dropwizardRegistry = bridgeRegistry.getDropwizardRegistry();
        this.results = results;
        this.operation = operation;
        this.testVariationName = testVariationName != null ? testVariationName : "";
        this.branchName = branchName != null ? branchName : "";
        this.commitId = commitId != null ? commitId : "";
        this.concurrency = concurrency;
        this.cpuReader = new CpuMemoryReader();
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
     * Log a periodic summary (for debugging). The real upload happens in {@link #stop()}.
     */
    public void report() {
        double successRate = sumMeterRates(true);
        double failureRate = sumMeterRates(false);
        if (successRate > 0 || failureRate > 0) {
            LOGGER.debug("Periodic: successRate={}/s failureRate={}/s cpu={}",
                String.format("%.1f", successRate),
                String.format("%.1f", failureRate),
                String.format("%.1f%%", cpuReader.getSystemWideCpuUsage() * 100));
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

        uploadFinalResults();
    }

    private void uploadFinalResults() {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnn")
            .withZone(ZoneId.from(ZoneOffset.UTC));

        // Read rates from Dropwizard Meters (native 1-minute EWMA rates)
        double successRate = sumMeterRates(true);
        double failureRate = sumMeterRates(false);

        // Read latency percentiles from Dropwizard Timers
        double medianMs = 0;
        double p99Ms = 0;
        for (Map.Entry<String, com.codahale.metrics.Timer> entry : dropwizardRegistry.getTimers().entrySet()) {
            if (entry.getKey().contains(OP_LATENCY_PREFIX)) {
                Snapshot snapshot = entry.getValue().getSnapshot();
                medianMs += snapshot.getMedian();
                p99Ms += snapshot.get99thPercentile();
            }
        }

        // Dropwizard Timer reports in nanoseconds by default
        medianMs = medianMs / 1_000_000.0;
        p99Ms = p99Ms / 1_000_000.0;

        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("TIMESTAMP", formatter.format(Instant.now()).substring(0, 27));
        doc.put("Operation", this.operation);
        doc.put("TestVariationName", this.testVariationName);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("Concurrency", this.concurrency);
        doc.put("CpuUsage", cpuReader.getSystemWideCpuUsage());
        doc.put("SuccessRate", new BigDecimal(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        doc.put("FailureRate", new BigDecimal(failureRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        doc.put("P99LatencyInMs", new BigDecimal(p99Ms).setScale(2, RoundingMode.HALF_UP).doubleValue());
        doc.put("MedianLatencyInMs", new BigDecimal(medianMs).setScale(2, RoundingMode.HALF_UP).doubleValue());

        results.createItem(doc, new PartitionKey(id), null);
        LOGGER.info("Final results uploaded to {} - {}", results.getId(), doc.toPrettyString());
    }

    /**
     * Sum the 1-minute rates of all Dropwizard Meters matching the operation calls prefix,
     * filtered by success or failure (based on status code in the hierarchical name).
     */
    private double sumMeterRates(boolean successOnly) {
        double total = 0;
        for (Map.Entry<String, com.codahale.metrics.Meter> entry : dropwizardRegistry.getMeters().entrySet()) {
            String name = entry.getKey();
            if (!name.contains(OP_CALLS_PREFIX)) {
                continue;
            }
            // The hierarchical name includes tags like ".OperationStatusCode.200."
            // Extract status code to determine success/failure
            boolean isSuccess = isSuccessStatusInName(name);
            if (successOnly == isSuccess) {
                total += entry.getValue().getOneMinuteRate();
            }
        }
        return total;
    }

    private boolean isSuccessStatusInName(String hierarchicalName) {
        // HierarchicalNameMapper.DEFAULT encodes tags as ".tagKey.tagValue."
        // Look for ".OperationStatusCode.NNN." pattern
        String marker = ".OperationStatusCode.";
        int idx = hierarchicalName.indexOf(marker);
        if (idx < 0) {
            return false;
        }
        String rest = hierarchicalName.substring(idx + marker.length());
        // Extract the status code value (next segment before '.')
        int dotIdx = rest.indexOf('.');
        String statusCodeStr = dotIdx > 0 ? rest.substring(0, dotIdx) : rest;
        try {
            int code = Integer.parseInt(statusCodeStr);
            return code >= 200 && code < 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns a new {@link Builder} for {@link CosmosTotalResultReporter}.
     */
    public static Builder forRegistry(DropwizardBridgeMeterRegistry registry, CosmosContainer resultsContainer,
                                      String operation, String testVariationName,
                                      String branchName, String commitId, int concurrency) {
        return new Builder(registry, resultsContainer, operation, testVariationName,
            branchName, commitId, concurrency);
    }

    /**
     * A builder for {@link CosmosTotalResultReporter} instances.
     */
    public static class Builder {
        private final DropwizardBridgeMeterRegistry registry;
        private final CosmosContainer resultsContainer;
        private final String operation;
        private final String testVariationName;
        private final String branchName;
        private final String commitId;
        private final int concurrency;

        private Builder(DropwizardBridgeMeterRegistry registry, CosmosContainer resultsContainer,
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
