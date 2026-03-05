// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Internal benchmark configuration built from CLI-parsed {@link Configuration}.
 * Contains lifecycle params, reporting config, and fully-resolved tenant workloads.
 *
 * <p>Each {@link TenantWorkloadConfig} carries its complete effective config
 * (account info + workload params), so no separate tenantDefaults map is needed.</p>
 *
 * <p>When {@code cycles > 1}, sensible defaults are applied automatically
 * unless explicitly overridden (settleTimeMs=90s, suppressCleanup=true).</p>
 */
public class BenchmarkConfig {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkConfig.class);
    private static final long DEFAULT_SETTLE_TIME_MS = 90_000;

    // -- Lifecycle --
    private int cycles = 1;
    private long settleTimeMs = 0;
    private boolean suppressCleanup = false;
    private boolean gcBetweenCycles = true;
    private boolean enableJvmStats = false;
    private boolean enableNettyHttpMetrics = false;

    // -- Reporting --
    private String reportingDirectory;
    private int printingInterval = 10;
    private String resultUploadEndpoint;
    private String resultUploadKey;
    private String resultUploadDatabase;
    private String resultUploadContainer;

    // -- Run metadata (for result reporting) --
    private String testVariationName = "";
    private String branchName = "";
    private String commitId = "";

    // -- JVM-global system properties (apply to all tenants, set once at startup) --
    private boolean isPartitionLevelCircuitBreakerEnabled = true;
    private boolean isPerPartitionAutomaticFailoverRequired = true;
    private int minConnectionPoolSizePerEndpoint = 0;

    // -- Tenants (each carries its full effective config) --
    private List<TenantWorkloadConfig> tenantWorkloads = Collections.emptyList();

    public BenchmarkConfig() {}

    /**
     * Build a BenchmarkConfig from CLI-parsed Configuration.
     */
    public static BenchmarkConfig fromConfiguration(Configuration cfg) throws IOException {
        BenchmarkConfig config = new BenchmarkConfig();

        // Lifecycle with smart defaults for cycles > 1
        config.cycles = cfg.getCycles();
        config.settleTimeMs = cfg.getSettleTimeMs();
        config.suppressCleanup = cfg.isSuppressCleanup();

        if (config.cycles > 1) {
            long configuredSettleTimeMs = cfg.getSettleTimeMs();
            // Only apply the default settle time when the configuration uses the sentinel -1.
            // An explicit value (including 0 to disable settling) should be respected.
            config.settleTimeMs = (configuredSettleTimeMs == -1)
                ? DEFAULT_SETTLE_TIME_MS
                : configuredSettleTimeMs;
            config.suppressCleanup = true; // suppress container/database cleanup
        }

        config.gcBetweenCycles = cfg.isGcBetweenCycles();

        // Workload config - ALWAYS from config file
        String workloadConfigPath = cfg.getWorkloadConfig();
        if (workloadConfigPath == null || !new File(workloadConfigPath).exists()) {
            throw new IllegalArgumentException(
                "A workload configuration file is required. Use -workloadConfig to specify the path."
                + (workloadConfigPath != null ? " File not found: " + workloadConfigPath : ""));
        }

        logger.info("Loading workload configs from {}.", workloadConfigPath);
        File workloadFile = new File(workloadConfigPath);
        config.tenantWorkloads = TenantWorkloadConfig.parseWorkloadConfig(workloadFile);
        config.loadWorkloadConfigSections(workloadFile);

        return config;
    }

    // ======== Getters ========

    public int getCycles() { return cycles; }
    public long getSettleTimeMs() { return settleTimeMs; }
    public boolean isSuppressCleanup() { return suppressCleanup; }
    public boolean isGcBetweenCycles() { return gcBetweenCycles; }
    public boolean isEnableJvmStats() { return enableJvmStats; }
    public boolean isEnableNettyHttpMetrics() { return enableNettyHttpMetrics; }

    public String getReportingDirectory() { return reportingDirectory; }
    public int getPrintingInterval() { return printingInterval; }
    public String getResultUploadEndpoint() { return resultUploadEndpoint; }
    public String getResultUploadKey() { return resultUploadKey; }
    public String getResultUploadDatabase() { return resultUploadDatabase; }
    public String getResultUploadContainer() { return resultUploadContainer; }

    public String getTestVariationName() { return testVariationName; }
    public String getBranchName() { return branchName; }
    public String getCommitId() { return commitId; }

    public boolean isPartitionLevelCircuitBreakerEnabled() { return isPartitionLevelCircuitBreakerEnabled; }
    public boolean isPerPartitionAutomaticFailoverRequired() { return isPerPartitionAutomaticFailoverRequired; }
    public int getMinConnectionPoolSizePerEndpoint() { return minConnectionPoolSizePerEndpoint; }

    public List<TenantWorkloadConfig> getTenantWorkloads() { return tenantWorkloads; }

    @Override
    public String toString() {
        return String.format(
            "BenchmarkConfig{cycles=%d, settleTimeMs=%d, suppressCleanup=%s, " +
            "gcBetweenCycles=%s, tenants=%d, reportingDirectory=%s, " +
            "circuitBreaker=%s, ppaf=%s, minConnPoolSize=%d}",
            cycles, settleTimeMs, suppressCleanup, gcBetweenCycles,
            tenantWorkloads.size(), reportingDirectory,
            isPartitionLevelCircuitBreakerEnabled, isPerPartitionAutomaticFailoverRequired,
            minConnectionPoolSizePerEndpoint);
    }

    /**
     * Loads all non-tenant sections from the workload config file:
     * JVM system properties, metrics config, result upload, and run metadata.
     */
    private void loadWorkloadConfigSections(File workloadConfigFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(workloadConfigFile);

        loadJvmSystemProperties(root);
        loadMetricsConfig(root);
        loadResultUploadConfig(root);
        loadRunMetadata(root);
    }

    /**
     * JVM-global system properties from the tenantDefaults section.
     * These are JVM-wide and cannot vary per tenant.
     */
    private void loadJvmSystemProperties(JsonNode root) {
        JsonNode defaults = root.get("tenantDefaults");
        if (defaults != null && defaults.isObject()) {
            if (defaults.has("isPartitionLevelCircuitBreakerEnabled")) {
                isPartitionLevelCircuitBreakerEnabled =
                    Boolean.parseBoolean(defaults.get("isPartitionLevelCircuitBreakerEnabled").asText());
            }
            if (defaults.has("isPerPartitionAutomaticFailoverRequired")) {
                isPerPartitionAutomaticFailoverRequired =
                    Boolean.parseBoolean(defaults.get("isPerPartitionAutomaticFailoverRequired").asText());
            }
            if (defaults.has("minConnectionPoolSizePerEndpoint")) {
                minConnectionPoolSizePerEndpoint =
                    Integer.parseInt(defaults.get("minConnectionPoolSizePerEndpoint").asText());
            }
        }
    }

    /**
     * Metrics and reporting settings from the top-level "metrics" section.
     */
    private void loadMetricsConfig(JsonNode root) {
        JsonNode metrics = root.get("metrics");
        if (metrics != null && metrics.isObject()) {
            if (metrics.has("enableJvmStats")) {
                enableJvmStats = Boolean.parseBoolean(metrics.get("enableJvmStats").asText());
            }
            if (metrics.has("enableNettyHttpMetrics")) {
                enableNettyHttpMetrics = Boolean.parseBoolean(metrics.get("enableNettyHttpMetrics").asText());
            }
            if (metrics.has("printingInterval")) {
                printingInterval = Integer.parseInt(metrics.get("printingInterval").asText());
            }
            if (metrics.has("reportingDirectory")) {
                reportingDirectory = metrics.get("reportingDirectory").asText();
            }
        }
    }

    /**
     * Result upload configuration from "metrics.resultUpload".
     */
    private void loadResultUploadConfig(JsonNode root) {
        JsonNode metrics = root.get("metrics");
        if (metrics == null || !metrics.isObject()) {
            return;
        }
        JsonNode resultUpload = metrics.get("resultUpload");
        if (resultUpload != null && resultUpload.isObject()) {
            if (resultUpload.has("serviceEndpoint")) {
                resultUploadEndpoint = resultUpload.get("serviceEndpoint").asText();
            }
            if (resultUpload.has("masterKey")) {
                resultUploadKey = resultUpload.get("masterKey").asText();
            }
            if (resultUpload.has("database")) {
                resultUploadDatabase = resultUpload.get("database").asText();
            }
            if (resultUpload.has("container")) {
                resultUploadContainer = resultUpload.get("container").asText();
            }
        }
    }

    /**
     * Run metadata from "metrics.runMetadata" (tagged on uploaded results).
     */
    private void loadRunMetadata(JsonNode root) {
        JsonNode metrics = root.get("metrics");
        if (metrics == null || !metrics.isObject()) {
            return;
        }
        JsonNode runMetadata = metrics.get("runMetadata");
        if (runMetadata != null && runMetadata.isObject()) {
            if (runMetadata.has("testVariationName")) {
                testVariationName = runMetadata.get("testVariationName").asText();
            }
            if (runMetadata.has("branchName")) {
                branchName = runMetadata.get("branchName").asText();
            }
            if (runMetadata.has("commitId")) {
                commitId = runMetadata.get("commitId").asText();
            }
        }
    }
}
