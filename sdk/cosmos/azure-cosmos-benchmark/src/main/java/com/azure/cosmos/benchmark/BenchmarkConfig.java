// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

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
    private String graphiteEndpoint;
    private int graphiteEndpointPort = 2003;
    private int printingInterval = 10;
    private String resultUploadEndpoint;
    private String resultUploadKey;
    private String resultUploadDatabase;
    private String resultUploadContainer;

    // -- Run metadata (for result reporting) --
    private String testVariationName = "";
    private String branchName = "";
    private String commitId = "";

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
          config.settleTimeMs = Math.max(DEFAULT_SETTLE_TIME_MS, cfg.getSettleTimeMs());
          config.suppressCleanup = true; // suppress container/database cleanup
        }

        config.gcBetweenCycles = cfg.isGcBetweenCycles();
        config.enableJvmStats = cfg.isEnableJvmStats();

        // Reporting
        config.reportingDirectory = cfg.getReportingDirectory() != null
            ? cfg.getReportingDirectory().getPath() : null;
        if (cfg.getGraphiteEndpoint() != null) {
            config.graphiteEndpoint = cfg.getGraphiteEndpoint();
            config.graphiteEndpointPort = cfg.getGraphiteEndpointPort();
        }
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
        } else {
            // Single tenant from CLI args - use fromConfiguration() to copy ALL fields
            config.tenantWorkloads = Collections.singletonList(
                TenantWorkloadConfig.fromConfiguration(cfg));
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
    public String getGraphiteEndpoint() { return graphiteEndpoint; }
    public int getGraphiteEndpointPort() { return graphiteEndpointPort; }
    public int getPrintingInterval() { return printingInterval; }
    public String getResultUploadEndpoint() { return resultUploadEndpoint; }
    public String getResultUploadKey() { return resultUploadKey; }
    public String getResultUploadDatabase() { return resultUploadDatabase; }
    public String getResultUploadContainer() { return resultUploadContainer; }

    public String getTestVariationName() { return testVariationName; }
    public String getBranchName() { return branchName; }
    public String getCommitId() { return commitId; }

    public List<TenantWorkloadConfig> getTenantWorkloads() { return tenantWorkloads; }

    @Override
    public String toString() {
        return String.format(
            "BenchmarkConfig{cycles=%d, settleTimeMs=%d, suppressCleanup=%s, " +
            "gcBetweenCycles=%s, tenants=%d, reportingDirectory=%s}",
            cycles, settleTimeMs, suppressCleanup, gcBetweenCycles,
            tenantWorkloads.size(), reportingDirectory);
    }
}
