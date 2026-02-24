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
 * (account info + workload params), so no separate globalDefaults map is needed.</p>
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
        config.enableJvmStats = cfg.isEnableJvmStats();

        // Reporting
        config.reportingDirectory = cfg.getReportingDirectory() != null
            ? cfg.getReportingDirectory().getPath() : null;
        config.printingInterval = cfg.getPrintingInterval();
        config.resultUploadEndpoint = cfg.getServiceEndpointForRunResultsUploadAccount();
        config.resultUploadKey = cfg.getMasterKeyForRunResultsUploadAccount();
        config.resultUploadDatabase = cfg.getResultUploadDatabase();
        config.resultUploadContainer = cfg.getResultUploadContainer();

        // Run metadata
        config.testVariationName = cfg.getTestVariationName();
        config.branchName = cfg.getBranchName();
        config.commitId = cfg.getCommitId();

        // Tenants
        String tenantsFile = cfg.getTenantsFile();
        if (tenantsFile != null && new File(tenantsFile).exists()) {
            // tenants.json takes priority over CLI workload args (operation, concurrency, etc.)
            logger.info("Loading tenant configs from {}. " +
                "Workload parameters from tenants.json will take priority over CLI args.", tenantsFile);
            config.tenantWorkloads = TenantWorkloadConfig.parseTenantsFile(new File(tenantsFile));

            // Extract JVM-global system properties from globalDefaults
            config.loadGlobalSystemPropertiesFromTenantsFile(new File(tenantsFile));
        } else {
            // Single tenant from CLI args - use fromConfiguration() to copy ALL fields
            config.tenantWorkloads = Collections.singletonList(
                TenantWorkloadConfig.fromConfiguration(cfg));

            // JVM-global system properties from CLI
            config.isPartitionLevelCircuitBreakerEnabled = cfg.isPartitionLevelCircuitBreakerEnabled();
            config.isPerPartitionAutomaticFailoverRequired = cfg.isPerPartitionAutomaticFailoverRequired();
            config.minConnectionPoolSizePerEndpoint = cfg.getMinConnectionPoolSizePerEndpoint();
        }

        return config;
    }

    // ======== Getters ========

    public int getCycles() { return cycles; }
    public long getSettleTimeMs() { return settleTimeMs; }
    public boolean isSuppressCleanup() { return suppressCleanup; }
    public boolean isGcBetweenCycles() { return gcBetweenCycles; }
    public boolean isEnableJvmStats() { return enableJvmStats; }

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
     * Reads JVM-global system properties from the globalDefaults section of a tenants.json file.
     * These properties are JVM-wide and cannot vary per tenant.
     */
    private void loadGlobalSystemPropertiesFromTenantsFile(File tenantsFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(tenantsFile);
        JsonNode defaults = root.get("globalDefaults");
        if (defaults == null || !defaults.isObject()) {
            return;
        }

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
