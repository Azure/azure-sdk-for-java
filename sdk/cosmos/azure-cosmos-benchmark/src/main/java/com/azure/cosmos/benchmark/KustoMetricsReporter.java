// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
// Note: CpuMemoryReader is an SDK internal API. Acceptable for benchmark tooling
// which already depends on SDK internals (e.g., ImplementationBridgeHelpers).
import com.azure.identity.DefaultAzureCredentialBuilder;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Streams all Micrometer metrics with full tag dimensions directly into an Azure Data Explorer
 * (Kusto) table via the streaming-ingest REST endpoint.
 *
 * <p>Reads from the Micrometer {@link MeterRegistry} (not the Dropwizard bridge) to preserve
 * explicit tag key-value pairs. Known dimensions (Operation, OperationStatusCode, Container,
 * RegionName, RunId, SdkVersion, ConnectionMode, EndpointFlavor, Phase, ...) map to first-class
 * columns of the {@code BenchmarkMetrics} table; any additional tags are collected into the
 * {@code Tags} dynamic column so nothing is lost.</p>
 *
 * <p>Auth uses {@link com.azure.identity.DefaultAzureCredential} for the cluster scope
 * ({@code <clusterUrl>/.default}); locally this resolves the az-CLI user, on a VM the managed
 * identity. Ingestion uses the JDK {@link HttpURLConnection} so no extra Maven dependency is
 * pulled into the benchmark module.</p>
 *
 * <p>Each {@link #report()} cycle walks the registry, builds one JSON document per non-empty
 * meter, and POSTs them all as a single {@code multijson} body — one HTTP round trip per tick
 * rather than one per metric.</p>
 */
public class KustoMetricsReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KustoMetricsReporter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    /** Refresh the bearer token when it is within this window of expiry. */
    private static final long TOKEN_REFRESH_SKEW_SECONDS = 300;

    private final MeterRegistry micrometerRegistry;
    private final String workloadId;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;
    private final int concurrency;
    private final CpuMemoryReader cpuReader;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isStopped;

    private final String ingestUri;
    private final String tokenScope;
    private final TokenCredential credential;
    private final TokenRequestContext tokenRequestContext;
    private volatile AccessToken cachedToken;

    private KustoMetricsReporter(
        MeterRegistry micrometerRegistry,
        KustoReporterConfig reporterConfig,
        String workloadId,
        int concurrency) {

        this.micrometerRegistry = micrometerRegistry;
        this.workloadId = workloadId;
        this.testVariationName = reporterConfig.getTestVariationName();
        this.branchName = reporterConfig.getBranchName();
        this.commitId = reporterConfig.getCommitId();
        this.concurrency = concurrency;
        this.cpuReader = new CpuMemoryReader();
        this.isStopped = new AtomicBoolean(false);

        String clusterUrl = reporterConfig.getClusterUrl();
        this.ingestUri = clusterUrl
            + "/v1/rest/ingest/" + reporterConfig.getDatabase()
            + "/" + reporterConfig.getTable()
            + "?streamFormat=multijson&mappingName=" + reporterConfig.getMappingName();
        this.tokenScope = clusterUrl + "/.default";
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.tokenRequestContext = new TokenRequestContext().addScopes(this.tokenScope);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "kusto-result-reporter");
            t.setDaemon(true);
            return t;
        });
        LOGGER.info("KustoMetricsReporter enabled -> {}.{} (mapping={})",
            reporterConfig.getDatabase(), reporterConfig.getTable(), reporterConfig.getMappingName());
    }

    public static KustoMetricsReporter create(
        MeterRegistry micrometerRegistry,
        KustoReporterConfig reporterConfig,
        String workloadId,
        int concurrency) {
        return new KustoMetricsReporter(micrometerRegistry, reporterConfig, workloadId, concurrency);
    }

    public void start(long interval, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
    }

    public void report() {
        if (isStopped.get()) return;
        reportCore();
    }

    private void reportCore() {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        double cpuPercent = round(cpuReader.getSystemWideCpuUsage() * 100);

        List<ObjectNode> docs = new ArrayList<>();
        for (Meter meter : micrometerRegistry.getMeters()) {
            try {
                if (meter instanceof Timer) {
                    ObjectNode doc = buildTimerDoc(timestamp, (Timer) meter, cpuPercent);
                    if (doc != null) docs.add(doc);
                } else if (meter instanceof Counter) {
                    ObjectNode doc = buildCounterDoc(timestamp, (Counter) meter, cpuPercent);
                    if (doc != null) docs.add(doc);
                } else if (meter instanceof Gauge) {
                    ObjectNode doc = buildGaugeDoc(timestamp, (Gauge) meter, cpuPercent);
                    if (doc != null) docs.add(doc);
                } else if (meter instanceof DistributionSummary) {
                    ObjectNode doc = buildDistributionSummaryDoc(timestamp, (DistributionSummary) meter, cpuPercent);
                    if (doc != null) docs.add(doc);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to build metric doc: {}", meter.getId().getName(), e);
            }
        }

        if (!docs.isEmpty()) {
            ingest(docs);
        }
    }

    public void stop() {
        if (!isStopped.compareAndSet(false, true)) return;

        reportCore();

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("KustoMetricsReporter stopped");
    }

    private ObjectNode buildTimerDoc(String timestamp, Timer timer, double cpuPercent) {
        if (timer.count() == 0) return null;

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
        return doc;
    }

    private ObjectNode buildCounterDoc(String timestamp, Counter counter, double cpuPercent) {
        if (counter.count() == 0) return null;

        ObjectNode doc = createBaseDoc(timestamp, counter, "counter", cpuPercent);
        doc.put("Count", (long) counter.count());
        doc.put("Value", round(counter.count()));
        return doc;
    }

    private ObjectNode buildGaugeDoc(String timestamp, Gauge gauge, double cpuPercent) {
        double value = gauge.value();
        if (Double.isNaN(value) || value == 0) return null;

        ObjectNode doc = createBaseDoc(timestamp, gauge, "gauge", cpuPercent);
        doc.put("Value", round(value));
        return doc;
    }

    private ObjectNode buildDistributionSummaryDoc(String timestamp, DistributionSummary summary, double cpuPercent) {
        if (summary.count() == 0) return null;

        ObjectNode doc = createBaseDoc(timestamp, summary, "distribution", cpuPercent);
        doc.put("Count", summary.count());
        doc.put("Mean", round(summary.mean()));
        doc.put("Max", round(summary.max()));

        HistogramSnapshot snapshot = summary.takeSnapshot();
        for (ValueAtPercentile vp : snapshot.percentileValues()) {
            double p = vp.percentile();
            if (p == 0.5) doc.put("P50", round(vp.value()));
            else if (p == 0.9) doc.put("P90", round(vp.value()));
            else if (p == 0.95) doc.put("P95", round(vp.value()));
            else if (p == 0.99) doc.put("P99", round(vp.value()));
        }
        return doc;
    }

    private ObjectNode createBaseDoc(String timestamp, Meter meter, String metricType, double cpuPercent) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("Timestamp", timestamp);
        doc.put("MetricName", meter.getId().getName());
        doc.put("MetricType", metricType);

        // Emit tag dimensions as explicit fields (mapped to first-class columns when the tag key
        // matches a table column). Also collect ALL tags into the Tags dynamic column so nothing
        // is lost, including phi (for the *_percentile meters).
        ObjectNode tagsNode = OBJECT_MAPPER.createObjectNode();
        for (Tag tag : meter.getId().getTags()) {
            String key = tag.getKey();
            String val = tag.getValue();
            tagsNode.put(key, val);
            // phi is a real dimension used by the *_percentile metrics; surface it as a column.
            if ("phi".equals(key)) {
                try {
                    doc.put("Phi", Double.parseDouble(val));
                } catch (NumberFormatException ignored) {
                    // leave Phi null if not parseable
                }
            } else {
                doc.put(key, val);
            }
        }
        doc.set("Tags", tagsNode);

        // Run metadata. Prefer an explicit testVariationName when configured, else the derived
        // workloadId (the joined operation set). BenchmarkMetrics has no TestVariationName column,
        // so it is surfaced through WorkloadId.
        doc.put("WorkloadId",
            (this.testVariationName != null && !this.testVariationName.isEmpty())
                ? this.testVariationName : this.workloadId);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("CpuPercent", cpuPercent);

        return doc;
    }

    private void ingest(List<ObjectNode> docs) {
        StringBuilder sb = new StringBuilder();
        for (ObjectNode doc : docs) {
            sb.append(doc.toString()).append('\n');
        }
        byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);

        String bearer = getToken();
        if (bearer == null) {
            LOGGER.warn("Kusto ingest skipped: no bearer token available");
            return;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(ingestUri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("Authorization", "Bearer " + bearer);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setFixedLengthStreamingMode(payload.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                LOGGER.debug("Kusto ingest OK ({} docs, http {})", docs.size(), status);
            } else {
                String err = readStream(conn.getErrorStream());
                LOGGER.warn("Kusto ingest FAILED (http {}, {} docs): {}", status, docs.size(), err);
            }
        } catch (Exception e) {
            LOGGER.warn("Kusto ingest error ({} docs)", docs.size(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getToken() {
        AccessToken token = this.cachedToken;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (token != null
            && token.getExpiresAt() != null
            && token.getExpiresAt().isAfter(now.plusSeconds(TOKEN_REFRESH_SKEW_SECONDS))) {
            return token.getToken();
        }
        try {
            AccessToken fresh = credential.getToken(tokenRequestContext).block();
            if (fresh != null) {
                this.cachedToken = fresh;
                return fresh.getToken();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to acquire Kusto bearer token for scope {}", tokenScope, e);
        }
        return null;
    }

    private static String readStream(InputStream in) {
        if (in == null) return "";
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
