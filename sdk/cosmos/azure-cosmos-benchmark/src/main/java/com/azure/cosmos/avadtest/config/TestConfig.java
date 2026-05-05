package com.azure.cosmos.avadtest.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Configuration loaded from a JSON file with environment variable overrides.
 * <p>
 * Load order (highest precedence first):
 * <ol>
 *   <li>Environment variables (e.g. COSMOS_KEY — always wins for secrets)</li>
 *   <li>JSON config file (--config path)</li>
 *   <li>Built-in defaults</li>
 * </ol>
 */
public final class TestConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String endpoint;
    private final String regionalEndpoint;
    private final String key;
    private final String database;
    private final String feedContainer;
    private final String leaseContainer;
    private final String preferredRegion;
    private final int opsPerSec;
    private final int docSizeBytes;
    private final int logicalPartitionCount;
    private final int durationSeconds;
    private final int workerCount;

    private TestConfig(Builder builder) {
        this.endpoint = builder.endpoint;
        this.regionalEndpoint = builder.regionalEndpoint;
        this.key = builder.key;
        this.database = builder.database;
        this.feedContainer = builder.feedContainer;
        this.leaseContainer = builder.leaseContainer;
        this.preferredRegion = builder.preferredRegion;
        this.opsPerSec = builder.opsPerSec;
        this.docSizeBytes = builder.docSizeBytes;
        this.logicalPartitionCount = builder.logicalPartitionCount;
        this.durationSeconds = builder.durationSeconds;
        this.workerCount = builder.workerCount;
    }

    /**
     * Load config from a JSON file. Environment variables override JSON values.
     */
    public static TestConfig fromJson(String filePath) throws IOException {
        JsonNode root = MAPPER.readTree(new File(filePath));
        JsonNode cosmos = root.path("cosmos");
        JsonNode ingestor = root.path("ingestor");

        return new Builder()
            .endpoint(resolve("COSMOS_ENDPOINT", textOrNull(cosmos, "endpoint"), null))
            .regionalEndpoint(resolve("COSMOS_REGIONAL_ENDPOINT", textOrNull(cosmos, "regionalEndpoint"), ""))
            .key(resolve("COSMOS_KEY", textOrNull(cosmos, "key"), null))
            .database(resolve("COSMOS_DATABASE", textOrNull(cosmos, "database"), "graph_db"))
            .feedContainer(resolve("COSMOS_FEED_CONTAINER", textOrNull(cosmos, "feedContainer"), "avad-test"))
            .leaseContainer(resolve("COSMOS_LEASE_CONTAINER", textOrNull(cosmos, "leaseContainer"), "avad-test-leases"))
            .preferredRegion(resolve("COSMOS_PREFERRED_REGION", textOrNull(cosmos, "preferredRegion"), "West Central US"))
            .opsPerSec(resolveInt("OPS_PER_SEC", intOrNull(ingestor, "opsPerSec"), 5000))
            .docSizeBytes(resolveInt("DOC_SIZE_BYTES", intOrNull(ingestor, "docSizeBytes"), 1024))
            .logicalPartitionCount(resolveInt("LOGICAL_PARTITION_COUNT", intOrNull(ingestor, "logicalPartitionCount"), 100000))
            .durationSeconds(resolveInt("DURATION_SECONDS", intOrNull(ingestor, "durationSeconds"), 3600))
            .workerCount(resolveInt("WORKER_COUNT", intOrNull(ingestor, "workerCount"), 2))
            .build();
    }

    /**
     * Load config from environment variables only (no JSON file).
     */
    public static TestConfig fromEnv() {
        return new Builder()
            .endpoint(envRequired("COSMOS_ENDPOINT"))
            .regionalEndpoint(envOrDefault("COSMOS_REGIONAL_ENDPOINT", ""))
            .key(envRequired("COSMOS_KEY"))
            .database(envOrDefault("COSMOS_DATABASE", "graph_db"))
            .feedContainer(envOrDefault("COSMOS_FEED_CONTAINER", "avad-test"))
            .leaseContainer(envOrDefault("COSMOS_LEASE_CONTAINER", "avad-test-leases"))
            .preferredRegion(envOrDefault("COSMOS_PREFERRED_REGION", "West Central US"))
            .opsPerSec(Integer.parseInt(envOrDefault("OPS_PER_SEC", "5000")))
            .docSizeBytes(Integer.parseInt(envOrDefault("DOC_SIZE_BYTES", "1024")))
            .logicalPartitionCount(Integer.parseInt(envOrDefault("LOGICAL_PARTITION_COUNT", "100000")))
            .durationSeconds(Integer.parseInt(envOrDefault("DURATION_SECONDS", "3600")))
            .workerCount(Integer.parseInt(envOrDefault("WORKER_COUNT", "2")))
            .build();
    }

    /** Resolve: env var > JSON value > default. */
    private static String resolve(String envName, String jsonValue, String defaultValue) {
        String env = envOrNull(envName);
        if (env != null) return env;
        if (jsonValue != null && !jsonValue.trim().isEmpty()) return jsonValue;
        if (defaultValue != null) return defaultValue;
        throw new IllegalStateException("Required config missing: " + envName);
    }

    private static int resolveInt(String envName, Integer jsonValue, int defaultValue) {
        String env = envOrNull(envName);
        if (env != null) return Integer.parseInt(env);
        if (jsonValue != null) return jsonValue;
        return defaultValue;
    }

    private static String textOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private static Integer intOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private static String envOrNull(String name) {
        String val = System.getenv(name);
        if (val != null && !val.trim().isEmpty()) return val;
        val = System.getProperty(name);
        return (val != null && !val.trim().isEmpty()) ? val : null;
    }

    private static String envRequired(String name) {
        String val = envOrNull(name);
        if (val == null) throw new IllegalStateException("Required config missing: " + name);
        return val;
    }

    private static String envOrDefault(String name, String defaultVal) {
        String val = envOrNull(name);
        return val != null ? val : defaultVal;
    }

    public String endpoint() { return endpoint; }
    public String regionalEndpoint() { return regionalEndpoint; }
    public String readerEndpoint() {
        return (regionalEndpoint != null && !regionalEndpoint.trim().isEmpty()) ? regionalEndpoint : endpoint;
    }
    public String key() { return key; }
    public String database() { return database; }
    public String feedContainer() { return feedContainer; }
    public String leaseContainer() { return leaseContainer; }
    public String preferredRegion() { return preferredRegion; }
    public List<String> preferredRegions() { return Collections.singletonList(preferredRegion); }
    public int opsPerSec() { return opsPerSec; }
    public int docSizeBytes() { return docSizeBytes; }
    public int logicalPartitionCount() { return logicalPartitionCount; }
    public int durationSeconds() { return durationSeconds; }
    public int workerCount() { return workerCount; }

    public static final class Builder {
        private String endpoint, regionalEndpoint, key, database, feedContainer, leaseContainer;
        private String preferredRegion;
        private int opsPerSec, docSizeBytes, logicalPartitionCount, durationSeconds, workerCount;

        public Builder endpoint(String v) { this.endpoint = v; return this; }
        public Builder regionalEndpoint(String v) { this.regionalEndpoint = v; return this; }
        public Builder key(String v) { this.key = v; return this; }
        public Builder database(String v) { this.database = v; return this; }
        public Builder feedContainer(String v) { this.feedContainer = v; return this; }
        public Builder leaseContainer(String v) { this.leaseContainer = v; return this; }
        public Builder preferredRegion(String v) { this.preferredRegion = v; return this; }
        public Builder opsPerSec(int v) { this.opsPerSec = v; return this; }
        public Builder docSizeBytes(int v) { this.docSizeBytes = v; return this; }
        public Builder logicalPartitionCount(int v) { this.logicalPartitionCount = v; return this; }
        public Builder durationSeconds(int v) { this.durationSeconds = v; return this; }
        public Builder workerCount(int v) { this.workerCount = v; return this; }
        public TestConfig build() { return new TestConfig(this); }
    }
}
