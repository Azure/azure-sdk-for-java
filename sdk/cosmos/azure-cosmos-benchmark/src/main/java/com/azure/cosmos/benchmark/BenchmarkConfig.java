// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Root benchmark configuration, deserialized directly from a single JSON file.
 * All settings live here — no CLI arguments except the config file path itself.
 *
 * <p>Settings are grouped into two top-level categories:</p>
 * <ul>
 *   <li>{@link LifecycleConfig} — cycle control, settle time, cleanup</li>
 *   <li>{@link OrchestratorConfig} — dispatch concurrency, operation count, time limit,
 *       JVM-wide system properties, metrics, tenant defaults, and tenants</li>
 * </ul>
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "lifecycle": { "cycles": 1, "settleTimeMs": 0, "suppressCleanup": false, "gcBetweenCycles": true },
 *   "orchestrator": {
 *     "concurrency": 10,
 *     "numberOfOperations": 100000,
 *     "maxRunningTimeDuration": "PT1H",
 *     "isPartitionLevelCircuitBreakerEnabled": true,
 *     "isPerPartitionAutomaticFailoverRequired": true,
 *     "minConnectionPoolSizePerEndpoint": 0,
 *     "metrics": { "printingInterval": 10, "destination": { "csv": { "reportingDirectory": "/tmp/csv" } } },
 *     "tenantDefaults": { "connectionMode": "DIRECT" },
 *     "tenants": [{ "serviceEndpoint": "...", "masterKey": "...", "databaseId": "...", "containerId": "..." }]
 *   }
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenchmarkConfig {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkConfig.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    // ======== Grouped configuration sections ========

    @JsonProperty("lifecycle")
    private LifecycleConfig lifecycle = new LifecycleConfig();

    @JsonProperty("orchestrator")
    private OrchestratorConfig orchestrator = new OrchestratorConfig();

    // ======== Tenants (parsed from orchestrator.tenants via TenantWorkloadConfig.parseWorkloadConfig) ========
    private transient List<TenantWorkloadConfig> tenantWorkloads = Collections.emptyList();

    public BenchmarkConfig() {}

    // ======== Factory ========

    /**
     * Deserialize a BenchmarkConfig from a JSON file.
     * The file contains all configuration: lifecycle and orchestrator (which includes
     * dispatch settings, JVM properties, metrics, tenant defaults, and tenant definitions).
     *
     * <p>After deserialization, applies smart defaults for multi-cycle runs:
     * settleTimeMs defaults to 90s when cycles &gt; 1 and not explicitly set.</p>
     *
     * @param configFile the JSON configuration file
     * @return a fully populated BenchmarkConfig
     * @throws IOException if the file cannot be read or parsed
     */
    public static BenchmarkConfig fromFile(File configFile) throws IOException {
        logger.info("Loading benchmark config from {}.", configFile.getAbsolutePath());

        BenchmarkConfig config = OBJECT_MAPPER.readValue(configFile, BenchmarkConfig.class);

        // Smart defaults for multi-cycle runs
        if (config.lifecycle.cycles > 1) {
            if (config.lifecycle.settleTimeMs == -1) {
                config.lifecycle.settleTimeMs = LifecycleConfig.DEFAULT_SETTLE_TIME_MS;
            }
            config.lifecycle.suppressCleanup = true;
        } else if (config.lifecycle.settleTimeMs == -1) {
            config.lifecycle.settleTimeMs = 0;
        }

        // Merge tenantDefaults into each tenant and validate
        TenantDefaultConfig defaults = config.orchestrator.tenantDefaults;
        List<TenantWorkloadConfig> resolvedTenants = new ArrayList<>();
        for (TenantWorkloadConfig tenant : config.orchestrator.tenants) {
            if (defaults != null) {
                defaults.applyTo(tenant);
            }
            validateTenantConfig(tenant);
            resolvedTenants.add(tenant);
        }
        config.tenantWorkloads = resolvedTenants;

        // Validate orchestrator settings
        if (config.orchestrator.concurrency < 1) {
            throw new IllegalArgumentException("orchestrator.concurrency must be >= 1, got: "
                + config.orchestrator.concurrency);
        }

        return config;
    }

    private static void validateTenantConfig(TenantWorkloadConfig tenant) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isEmpty(tenant.getServiceEndpoint())) {
            missing.add("serviceEndpoint");
        }
        if (StringUtils.isEmpty(tenant.getDatabaseId())) {
            missing.add("databaseId");
        }
        if (StringUtils.isEmpty(tenant.getContainerId())) {
            missing.add("containerId");
        }
        if (!tenant.isManagedIdentityRequired()
            && StringUtils.isEmpty(tenant.getMasterKey())) {
            missing.add("masterKey (required when isManagedIdentityRequired is not true)");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                "Tenant '" + tenant.getId() + "' is missing required configuration: " + missing);
        }
    }

    // ======== Convenience getters (delegate to nested configs) ========

    // -- Lifecycle --
    public int getCycles() { return lifecycle.cycles; }
    public long getSettleTimeMs() { return lifecycle.settleTimeMs; }
    public boolean isSuppressCleanup() { return lifecycle.suppressCleanup; }
    public boolean isGcBetweenCycles() { return lifecycle.gcBetweenCycles; }

    // -- Orchestrator dispatch --
    public int getConcurrency() { return orchestrator.concurrency; }
    public int getNumberOfOperations() { return orchestrator.numberOfOperations; }
    public Duration getMaxRunningTimeDuration() { return orchestrator.maxRunningTimeDuration; }

    // -- JVM system properties (now under orchestrator) --
    public boolean isPartitionLevelCircuitBreakerEnabled() { return orchestrator.isPartitionLevelCircuitBreakerEnabled; }
    public boolean isPerPartitionAutomaticFailoverRequired() { return orchestrator.isPerPartitionAutomaticFailoverRequired; }
    public int getMinConnectionPoolSizePerEndpoint() { return orchestrator.minConnectionPoolSizePerEndpoint; }

    // -- Metrics (now under orchestrator) --
    public boolean isEnableJvmStats() { return orchestrator.metrics.enableJvmStats; }
    public boolean isEnableNettyHttpMetrics() { return orchestrator.metrics.enableNettyHttpMetrics; }
    public int getPrintingInterval() { return orchestrator.metrics.printingInterval; }

    public CsvReporterConfig getCsvReporterConfig() {
        return orchestrator.metrics.destination != null ? orchestrator.metrics.destination.csv : null;
    }

    public CosmosReporterConfig getCosmosReporterConfig() {
        return orchestrator.metrics.destination != null ? orchestrator.metrics.destination.cosmos : null;
    }

    public AppInsightsReporterConfig getAppInsightsReporterConfig() {
        return orchestrator.metrics.destination != null ? orchestrator.metrics.destination.applicationInsights : null;
    }

    /**
     * Determine the reporting destination from which config is present.
     */
    public ReportingDestination getReportingDestination() {
        if (getCsvReporterConfig() != null) return ReportingDestination.CSV;
        if (getCosmosReporterConfig() != null) return ReportingDestination.COSMOSDB;
        if (getAppInsightsReporterConfig() != null) return ReportingDestination.APPLICATION_INSIGHTS;
        return null;
    }

    // -- Tenants --
    public List<TenantWorkloadConfig> getTenantWorkloads() { return tenantWorkloads; }

    @Override
    public String toString() {
        return String.format(
            "BenchmarkConfig{lifecycle=%s, orchestrator=%s, tenants=%d, reportingDestination=%s}",
            lifecycle, orchestrator, tenantWorkloads.size(), getReportingDestination());
    }

    // ======== Nested configuration classes ========

    /**
     * Lifecycle settings: cycle control, settle time between cycles, cleanup behavior.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LifecycleConfig {
        static final long DEFAULT_SETTLE_TIME_MS = 90_000;

        @JsonProperty("cycles")
        int cycles = 1;

        /** Milliseconds to wait between cycles. -1 = auto-default (90s when cycles &gt; 1). */
        @JsonProperty("settleTimeMs")
        long settleTimeMs = -1;

        @JsonProperty("suppressCleanup")
        boolean suppressCleanup = false;

        @JsonProperty("gcBetweenCycles")
        boolean gcBetweenCycles = true;

        @Override
        public String toString() {
            return String.format("Lifecycle{cycles=%d, settleTimeMs=%d, suppressCleanup=%s, gcBetweenCycles=%s}",
                cycles, settleTimeMs, suppressCleanup, gcBetweenCycles);
        }
    }

    /**
     * Orchestrator-level configuration.
     * Contains dispatch settings, JVM-wide system properties (flattened),
     * metrics configuration, tenant defaults, and tenant definitions.
     *
     * <p>Dispatch settings control how many total operations to run and with what concurrency.
     * They are NOT per-tenant — the orchestrator owns dispatch across all tenants.</p>
     *
     * <p>JVM-global system properties apply to all tenants and are set once at startup.</p>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrchestratorConfig {
        // -- Dispatch settings --
        @JsonProperty("concurrency")
        int concurrency = 10;

        @JsonProperty("numberOfOperations")
        int numberOfOperations = 100000;

        /**
         * Maximum wall-clock duration for the workload (ISO-8601 duration, e.g. "PT1H").
         * When set, the orchestrator runs until this duration elapses (ignoring numberOfOperations).
         * When null, the orchestrator runs until numberOfOperations are completed.
         */
        @JsonProperty("maxRunningTimeDuration")
        Duration maxRunningTimeDuration;

        // -- JVM system properties (flattened from former jvmProperties section) --
        @JsonProperty("isPartitionLevelCircuitBreakerEnabled")
        boolean isPartitionLevelCircuitBreakerEnabled = true;

        @JsonProperty("isPerPartitionAutomaticFailoverRequired")
        boolean isPerPartitionAutomaticFailoverRequired = true;

        @JsonProperty("minConnectionPoolSizePerEndpoint")
        int minConnectionPoolSizePerEndpoint = 0;

        // -- Metrics (nested under orchestrator) --
        @JsonProperty("metrics")
        MetricsConfig metrics = new MetricsConfig();

        // -- Tenant configuration (deserialized by Jackson) --
        @JsonProperty("tenantDefaults")
        TenantDefaultConfig tenantDefaults;

        @JsonProperty("tenants")
        List<TenantWorkloadConfig> tenants = Collections.emptyList();

        @Override
        public String toString() {
            return String.format(
                "Orchestrator{concurrency=%d, numberOfOperations=%d, maxRunningTimeDuration=%s, "
                + "circuitBreaker=%s, ppaf=%s, minConnPoolSize=%d}",
                concurrency, numberOfOperations, maxRunningTimeDuration,
                isPartitionLevelCircuitBreakerEnabled, isPerPartitionAutomaticFailoverRequired,
                minConnectionPoolSizePerEndpoint);
        }
    }

    /**
     * Metrics and reporting configuration.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetricsConfig {
        @JsonProperty("enableJvmStats")
        boolean enableJvmStats = false;

        @JsonProperty("enableNettyHttpMetrics")
        boolean enableNettyHttpMetrics = false;

        @JsonProperty("printingInterval")
        int printingInterval = 10;

        @JsonProperty("destination")
        MetricsDestinationConfig destination;
    }

    /**
     * Metrics destination — at most one should be configured.
     * Destinations are mutually exclusive (CSV, Cosmos DB, or Application Insights).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetricsDestinationConfig {
        @JsonProperty("csv")
        CsvReporterConfig csv;

        @JsonProperty("cosmos")
        CosmosReporterConfig cosmos;

        @JsonProperty("applicationInsights")
        AppInsightsReporterConfig applicationInsights;
    }
}
