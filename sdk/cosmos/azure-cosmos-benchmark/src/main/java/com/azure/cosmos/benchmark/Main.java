// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try {
            LOGGER.debug("Parsing the arguments ...");
            Configuration cfg = new Configuration();

            JCommander jcommander = new JCommander(cfg, args);
            if (cfg.isHelp()) {
                jcommander.usage();
                return;
            }

            // All configuration lives in the JSON file.
            // Configuration only provides the path to it.
            File configFile = new File(cfg.getWorkloadConfig());
            if (!configFile.exists()) {
                throw new IllegalArgumentException(
                    "Workload configuration file not found: " + configFile.getAbsolutePath());
            }

            BenchmarkConfig benchConfig = BenchmarkConfig.fromFile(configFile);

            if (benchConfig.getTenantWorkloads().isEmpty()) {
                throw new IllegalArgumentException(
                    "No tenants defined in workload config. The 'tenants' array must contain at least one entry.");
            }

            for (TenantWorkloadConfig tenant : benchConfig.getTenantWorkloads()) {
                validateConfiguration(tenant);
            }

            new BenchmarkOrchestrator().run(benchConfig);
        } catch (ParameterException e) {
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        }
    }

    private static void validateConfiguration(TenantWorkloadConfig workloadCfg) {
        switch (workloadCfg.getOperationType()) {
            case WriteThroughput:
                break;
            default:
                if (!workloadCfg.isContentResponseOnWriteEnabled()) {
                    throw new IllegalArgumentException("contentResponseOnWriteEnabled parameter can only be set to false " +
                        "for write operations");
                }
        }

        switch (workloadCfg.getOperationType()) {
            case ReadThroughput:
                break;
            default:
                if (workloadCfg.getSparsityWaitTime() != null) {
                    throw new IllegalArgumentException("sparsityWaitTime is not supported for " + workloadCfg.getOperationType());
                }
        }
    }
}
