package com.azure.cosmos.avadtest.config;

import java.util.List;

/**
 * Configuration loaded from environment variables or system properties.
 * Env vars take precedence over system properties.
 */
public final class TestConfig {

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
    private final String producedLogFile;
    private final String consumedLogFile;
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
        this.producedLogFile = builder.producedLogFile;
        this.consumedLogFile = builder.consumedLogFile;
        this.durationSeconds = builder.durationSeconds;
        this.workerCount = builder.workerCount;
    }

    public static TestConfig fromEnv() {
        return new Builder()
            .endpoint(env("COSMOS_ENDPOINT"))
            .regionalEndpoint(envOrDefault("COSMOS_REGIONAL_ENDPOINT", ""))
            .key(env("COSMOS_KEY"))
            .database(envOrDefault("COSMOS_DATABASE", "graph_db"))
            .feedContainer(envOrDefault("COSMOS_FEED_CONTAINER", "avad-test"))
            .leaseContainer(envOrDefault("COSMOS_LEASE_CONTAINER", "avad-test-leases"))
            .preferredRegion(envOrDefault("COSMOS_PREFERRED_REGION", "West Central US"))
            .opsPerSec(Integer.parseInt(envOrDefault("OPS_PER_SEC", "5000")))
            .docSizeBytes(Integer.parseInt(envOrDefault("DOC_SIZE_BYTES", "1024")))
            .logicalPartitionCount(Integer.parseInt(envOrDefault("LOGICAL_PARTITION_COUNT", "100000")))
            .producedLogFile(envOrDefault("PRODUCED_LOG", "produced.log"))
            .consumedLogFile(envOrDefault("CONSUMED_LOG", "consumed.log"))
            .durationSeconds(Integer.parseInt(envOrDefault("DURATION_SECONDS", "3600")))
            .workerCount(Integer.parseInt(envOrDefault("WORKER_COUNT", "2")))
            .build();
    }

    private static String env(String name) {
        String val = System.getenv(name);
        if (val == null || val.isBlank()) {
            val = System.getProperty(name);
        }
        if (val == null || val.isBlank()) {
            throw new IllegalStateException("Required config missing: " + name);
        }
        return val;
    }

    private static String envOrDefault(String name, String defaultVal) {
        String val = System.getenv(name);
        if (val == null || val.isBlank()) {
            val = System.getProperty(name);
        }
        return (val != null && !val.isBlank()) ? val : defaultVal;
    }

    public String endpoint() { return endpoint; }
    public String regionalEndpoint() { return regionalEndpoint; }
    /** Returns regional endpoint if set, otherwise global endpoint. For use by readers. */
    public String readerEndpoint() {
        return (regionalEndpoint != null && !regionalEndpoint.isBlank()) ? regionalEndpoint : endpoint;
    }
    public String key() { return key; }
    public String database() { return database; }
    public String feedContainer() { return feedContainer; }
    public String leaseContainer() { return leaseContainer; }
    public String preferredRegion() { return preferredRegion; }
    public List<String> preferredRegions() { return List.of(preferredRegion); }
    public int opsPerSec() { return opsPerSec; }
    public int docSizeBytes() { return docSizeBytes; }
    public int logicalPartitionCount() { return logicalPartitionCount; }
    public String producedLogFile() { return producedLogFile; }
    public String consumedLogFile() { return consumedLogFile; }
    /** Duration in seconds. 0 = run forever until killed. */
    public int durationSeconds() { return durationSeconds; }
    /** Number of CFP worker instances per reader mode. */
    public int workerCount() { return workerCount; }

    public static final class Builder {
        private String endpoint, regionalEndpoint, key, database, feedContainer, leaseContainer;
        private String preferredRegion, producedLogFile, consumedLogFile;
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
        public Builder producedLogFile(String v) { this.producedLogFile = v; return this; }
        public Builder consumedLogFile(String v) { this.consumedLogFile = v; return this; }
        public Builder durationSeconds(int v) { this.durationSeconds = v; return this; }
        public Builder workerCount(int v) { this.workerCount = v; return this; }
        public TestConfig build() { return new TestConfig(this); }
    }
}
