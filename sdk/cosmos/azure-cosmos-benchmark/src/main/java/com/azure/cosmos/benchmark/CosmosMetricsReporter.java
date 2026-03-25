// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
// Note: CpuMemoryReader is an SDK internal API. Acceptable for benchmark tooling
// which already depends on SDK internals (e.g., ImplementationBridgeHelpers).
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uploads all Micrometer metrics with full tag dimensions to a Cosmos DB container.
 *
 * <p>Reads from the Micrometer {@link MeterRegistry} (not the Dropwizard bridge) to preserve
 * explicit tag key-value pairs as individual document fields. This makes the data directly
 * queryable in Kusto with full dimension support (Container, Operation, OperationStatusCode,
 * ClientCorrelationId, RegionName, PartitionKeyRangeId, etc.).</p>
 *
 * <p>If no upload endpoint is configured, this reporter is a no-op.</p>
 *
 * <p>Uploaded document schema per metric:</p>
 * <pre>
 * {
 *   "id", "partition_key",
 *   "Timestamp", "MetricName", "MetricType",
 *   // All SDK tag dimensions as explicit fields:
 *   "Container", "Operation", "OperationStatusCode", "ClientCorrelationId",
 *   "RegionName", "RequestStatusCode", "RequestOperationType",
 *   "ConsistencyLevel", "PartitionKeyRangeId", "ServiceEndpoint",
 *   "ServiceAddress", "PartitionId", "ReplicaId", "OperationSubStatusCode",
 *   // Metric values (populated based on MetricType):
 *   "Count", "MeanRate", "OneMinuteRate",
 *   "MinMs", "MaxMs", "MeanMs", "P50Ms", "P90Ms", "P95Ms", "P99Ms",
 *   "Value",
 *   // Run metadata:
 *   "WorkloadId", "TestVariationName", "BranchName", "CommitId",
 *   "Concurrency", "CpuPercent"
 * }
 * </pre>
 */
public class CosmosMetricsReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosMetricsReporter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private final MeterRegistry micrometerRegistry;
    private final CosmosClient cosmosClient;
    private final CosmosContainer resultsContainer;
    private final String workloadId;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;
    private final int concurrency;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;
    private final boolean enabled;

    private CosmosMetricsReporter(
        MeterRegistry micrometerRegistry,
        CosmosReporterConfig reporterConfig,
        String workloadId,
        int concurrency) {

        this.workloadId = workloadId;
        this.testVariationName = reporterConfig.getTestVariationName();
        this.branchName = reporterConfig.getBranchName();
        this.commitId = reporterConfig.getCommitId();
        this.concurrency = concurrency;
        this.cpuReader = new CpuMemoryReader();

        this.micrometerRegistry = micrometerRegistry;
        this.cosmosClient = new CosmosClientBuilder()
            .endpoint(reporterConfig.getServiceEndpoint())
            .key(reporterConfig.getMasterKey())
            .buildClient();
        this.resultsContainer = cosmosClient
            .getDatabase(reporterConfig.getDatabase())
            .getContainer(reporterConfig.getContainer());
        this.enabled = true;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cosmos-result-reporter");
            t.setDaemon(true);
            return t;
        });
        LOGGER.info("CosmosMetricsReporter enabled -> {}/{}",
            reporterConfig.getDatabase(), reporterConfig.getContainer());
    }

    public static CosmosMetricsReporter create(
        MeterRegistry micrometerRegistry,
        CosmosReporterConfig reporterConfig,
        String workloadId,
        int concurrency) {
        return new CosmosMetricsReporter(micrometerRegistry, reporterConfig, workloadId, concurrency);
    }

    public void start(long interval, TimeUnit unit) {
        if (!enabled) return;
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    public void report() {
        if (!enabled) return;

        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        double cpuPercent = round(cpuReader.getSystemWideCpuUsage() * 100);

        for (Meter meter : micrometerRegistry.getMeters()) {
            String metricName = meter.getId().getName();

            try {
                if (meter instanceof Timer) {
                    reportTimer(timestamp, (Timer) meter, cpuPercent);
                } else if (meter instanceof Counter) {
                    reportCounter(timestamp, (Counter) meter, cpuPercent);
                } else if (meter instanceof Gauge) {
                    reportGauge(timestamp, (Gauge) meter, cpuPercent);
                } else if (meter instanceof DistributionSummary) {
                    reportDistributionSummary(timestamp, (DistributionSummary) meter, cpuPercent);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to upload metric: {}", metricName, e);
            }
        }
    }

    public void stop() {
        if (!enabled) return;

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        report();

        if (cosmosClient != null) {
            cosmosClient.close();
        }
        LOGGER.info("CosmosMetricsReporter stopped");
    }

    private void reportTimer(String timestamp, Timer timer, double cpuPercent) {
        if (timer.count() == 0) return;

        ObjectNode doc = createBaseDoc(timestamp, timer, "timer", cpuPercent);
        doc.put("Count", timer.count());

        HistogramSnapshot snapshot = timer.takeSnapshot();
        doc.put("MeanMs", round(timer.mean(TimeUnit.MILLISECONDS)));
        doc.put("MaxMs", round(snapshot.max(TimeUnit.MILLISECONDS)));

        for (ValueAtPercentile vp : snapshot.percentileValues()) {
            double ms = vp.value(TimeUnit.MILLISECONDS);
            double p = vp.percentile();
            if (p == 0.5) doc.put("P50Ms", round(ms));
            else if (p == 0.9) doc.put("P90Ms", round(ms));
            else if (p == 0.95) doc.put("P95Ms", round(ms));
            else if (p == 0.99) doc.put("P99Ms", round(ms));
        }

        uploadDoc(doc);
    }

    private void reportCounter(String timestamp, Counter counter, double cpuPercent) {
        if (counter.count() == 0) return;

        ObjectNode doc = createBaseDoc(timestamp, counter, "counter", cpuPercent);
        doc.put("Count", (long) counter.count());
        uploadDoc(doc);
    }

    private void reportGauge(String timestamp, Gauge gauge, double cpuPercent) {
        double value = gauge.value();
        if (Double.isNaN(value) || value == 0) return;

        ObjectNode doc = createBaseDoc(timestamp, gauge, "gauge", cpuPercent);
        doc.put("Value", round(value));
        uploadDoc(doc);
    }

    private void reportDistributionSummary(String timestamp, DistributionSummary summary, double cpuPercent) {
        if (summary.count() == 0) return;

        ObjectNode doc = createBaseDoc(timestamp, summary, "distribution", cpuPercent);
        doc.put("Count", summary.count());
        doc.put("MeanMs", round(summary.mean()));
        doc.put("MaxMs", round(summary.max()));
        doc.put("Value", round(summary.totalAmount()));

        HistogramSnapshot snapshot = summary.takeSnapshot();
        for (ValueAtPercentile vp : snapshot.percentileValues()) {
            double p = vp.percentile();
            if (p == 0.5) doc.put("P50Ms", round(vp.value()));
            else if (p == 0.9) doc.put("P90Ms", round(vp.value()));
            else if (p == 0.95) doc.put("P95Ms", round(vp.value()));
            else if (p == 0.99) doc.put("P99Ms", round(vp.value()));
        }

        uploadDoc(doc);
    }

    private ObjectNode createBaseDoc(String timestamp, Meter meter, String metricType, double cpuPercent) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("partition_key", id);
        doc.put("Timestamp", timestamp);
        doc.put("MetricName", meter.getId().getName());
        doc.put("MetricType", metricType);

        // Emit all tag dimensions as explicit fields.
        // Tags are written directly as field names since cosmos.client.* tags
        // (Container, Operation, etc.) don't collide with document metadata.
        for (Tag tag : meter.getId().getTags()) {
            String key = tag.getKey();
            // Guard against tags that could collide with document metadata fields
            if ("id".equals(key) || "partition_key".equals(key)) {
                key = "tag_" + key;
            }
            doc.put(key, tag.getValue());
        }

        // Run metadata
        doc.put("WorkloadId", this.workloadId);
        doc.put("TestVariationName", this.testVariationName);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("Concurrency", this.concurrency);
        doc.put("CpuPercent", cpuPercent);

        return doc;
    }

    private void uploadDoc(ObjectNode doc) {
        try {
            String id = doc.get("id").asText();
            resultsContainer.createItem(doc, new PartitionKey(id), null);
        } catch (Exception e) {
            LOGGER.warn("Failed to upload metric doc: {}", doc.get("MetricName"), e);
        }
    }

    private static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
