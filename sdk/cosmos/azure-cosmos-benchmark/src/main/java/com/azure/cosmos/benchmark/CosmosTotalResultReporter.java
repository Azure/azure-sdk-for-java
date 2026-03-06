// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
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
 * Periodically uploads all metrics from the Dropwizard bridge registry to a Cosmos DB container.
 *
 * <p>Each report interval, one document is uploaded per meter (Timer, Meter, Counter, Gauge),
 * preserving the full hierarchical name (which encodes all Micrometer tag dimensions).
 * This makes the data directly queryable in Kusto with full dimension support.</p>
 *
 * <p>If no upload endpoint is configured, this reporter is a no-op.</p>
 *
 * <p>Uploaded document schema:</p>
 * <pre>
 * {
 *   "id": "uuid",
 *   "Timestamp": "2026-03-06 12:00:00.0000000",
 *   "MetricName": "cosmos.client.op.latency.Container.db/coll.Operation.ReadItem...",
 *   "MetricType": "timer",
 *   "Count": 1234,
 *   "MeanRate": 45.6,
 *   "OneMinuteRate": 50.2,
 *   "MedianMs": 2.34,
 *   "P75Ms": 3.45,
 *   "P95Ms": 5.67,
 *   "P99Ms": 12.34,
 *   "MaxMs": 45.67,
 *   "Value": null,
 *   "RunMetadata": { "Operation", "TestVariationName", "BranchName", "CommitId", "Concurrency", "CpuUsage" }
 * }
 * </pre>
 */
public class CosmosTotalResultReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTotalResultReporter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnn")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private final MetricRegistry dropwizardRegistry;
    private final CosmosClient cosmosClient;
    private final CosmosContainer resultsContainer;
    private final String operation;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;
    private final int concurrency;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;
    private final boolean enabled;

    private CosmosTotalResultReporter(
        DropwizardBridgeMeterRegistry bridgeRegistry,
        BenchmarkConfig config,
        String operation,
        int concurrency) {

        this.operation = operation;
        this.testVariationName = config.getTestVariationName() != null ? config.getTestVariationName() : "";
        this.branchName = config.getBranchName() != null ? config.getBranchName() : "";
        this.commitId = config.getCommitId() != null ? config.getCommitId() : "";
        this.concurrency = concurrency;
        this.cpuReader = new CpuMemoryReader();

        // Self-contained: create Cosmos client if upload is configured, else no-op
        if (config.getResultUploadEndpoint() != null
            && config.getResultUploadDatabase() != null
            && config.getResultUploadContainer() != null) {

            this.dropwizardRegistry = bridgeRegistry.getDropwizardRegistry();
            this.cosmosClient = new CosmosClientBuilder()
                .endpoint(config.getResultUploadEndpoint())
                .key(config.getResultUploadKey())
                .buildClient();
            this.resultsContainer = cosmosClient
                .getDatabase(config.getResultUploadDatabase())
                .getContainer(config.getResultUploadContainer());
            this.enabled = true;
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cosmos-result-reporter");
                t.setDaemon(true);
                return t;
            });

            LOGGER.info("CosmosTotalResultReporter enabled -> {}/{}",
                config.getResultUploadDatabase(), config.getResultUploadContainer());
        } else {
            this.dropwizardRegistry = null;
            this.cosmosClient = null;
            this.resultsContainer = null;
            this.enabled = false;
            this.scheduler = null;

            LOGGER.info("CosmosTotalResultReporter disabled (no upload endpoint configured)");
        }
    }

    /**
     * Create a CosmosTotalResultReporter. If no upload endpoint is configured in
     * {@link BenchmarkConfig}, the reporter is a no-op.
     */
    public static CosmosTotalResultReporter create(
        DropwizardBridgeMeterRegistry bridgeRegistry,
        BenchmarkConfig config,
        String operation,
        int concurrency) {
        return new CosmosTotalResultReporter(bridgeRegistry, config, operation, concurrency);
    }

    /**
     * Start periodic reporting.
     */
    public void start(long interval, TimeUnit unit) {
        if (!enabled) {
            return;
        }
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    /**
     * Upload one snapshot of all metrics to Cosmos DB.
     */
    public void report() {
        if (!enabled) {
            return;
        }

        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        if (timestamp.length() > 27) {
            timestamp = timestamp.substring(0, 27);
        }
        double cpuUsage = cpuReader.getSystemWideCpuUsage();

        // Upload Timers (latency metrics — SDK ops, Netty, etc.)
        for (Map.Entry<String, Timer> entry : dropwizardRegistry.getTimers().entrySet()) {
            Timer timer = entry.getValue();
            if (timer.getCount() == 0) {
                continue;
            }
            Snapshot snapshot = timer.getSnapshot();
            ObjectNode doc = createBaseDoc(timestamp, entry.getKey(), "timer", cpuUsage);
            doc.put("Count", timer.getCount());
            doc.put("MeanRate", round(timer.getMeanRate()));
            doc.put("OneMinuteRate", round(timer.getOneMinuteRate()));
            // Dropwizard Timer reports in nanoseconds
            doc.put("MedianMs", round(snapshot.getMedian() / 1_000_000.0));
            doc.put("P75Ms", round(snapshot.get75thPercentile() / 1_000_000.0));
            doc.put("P95Ms", round(snapshot.get95thPercentile() / 1_000_000.0));
            doc.put("P99Ms", round(snapshot.get99thPercentile() / 1_000_000.0));
            doc.put("MaxMs", round(snapshot.getMax() / 1_000_000.0));
            uploadDoc(doc);
        }

        // Upload Meters (rate metrics — success/failure counts)
        for (Map.Entry<String, Meter> entry : dropwizardRegistry.getMeters().entrySet()) {
            Meter meter = entry.getValue();
            if (meter.getCount() == 0) {
                continue;
            }
            ObjectNode doc = createBaseDoc(timestamp, entry.getKey(), "meter", cpuUsage);
            doc.put("Count", meter.getCount());
            doc.put("MeanRate", round(meter.getMeanRate()));
            doc.put("OneMinuteRate", round(meter.getOneMinuteRate()));
            uploadDoc(doc);
        }

        // Upload Gauges (connection pools, JVM stats, Netty pool metrics)
        for (Map.Entry<String, Gauge> entry : dropwizardRegistry.getGauges().entrySet()) {
            Object value = entry.getValue().getValue();
            if (value instanceof Number) {
                ObjectNode doc = createBaseDoc(timestamp, entry.getKey(), "gauge", cpuUsage);
                doc.put("Value", ((Number) value).doubleValue());
                uploadDoc(doc);
            }
        }

        // Upload Counters
        for (Map.Entry<String, Counter> entry : dropwizardRegistry.getCounters().entrySet()) {
            if (entry.getValue().getCount() == 0) {
                continue;
            }
            ObjectNode doc = createBaseDoc(timestamp, entry.getKey(), "counter", cpuUsage);
            doc.put("Count", entry.getValue().getCount());
            uploadDoc(doc);
        }
    }

    /**
     * Stop reporting and clean up resources.
     */
    public void stop() {
        if (!enabled) {
            return;
        }

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Final upload
        report();

        if (cosmosClient != null) {
            cosmosClient.close();
        }

        LOGGER.info("CosmosTotalResultReporter stopped");
    }

    private ObjectNode createBaseDoc(String timestamp, String metricName, String metricType, double cpuUsage) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("Timestamp", timestamp);
        doc.put("MetricName", metricName);
        doc.put("MetricType", metricType);
        // Run metadata for Kusto pivoting
        doc.put("Operation", this.operation);
        doc.put("TestVariationName", this.testVariationName);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("Concurrency", this.concurrency);
        doc.put("CpuUsage", round(cpuUsage));
        return doc;
    }

    private void uploadDoc(ObjectNode doc) {
        try {
            String id = doc.get("id").asText();
            resultsContainer.createItem(doc, new PartitionKey(id), null);
        } catch (Exception e) {
            LOGGER.warn("Failed to upload metric: {}", doc.get("MetricName"), e);
        }
    }

    private static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
