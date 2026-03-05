// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            // Configuration holds only CLI lifecycle params (cycles, settleTimeMs, etc.).
            // BenchmarkConfig consumes them and loads all workload config from the JSON file.
            // BenchmarkOrchestrator handles dispatch for all benchmark types (async, sync,
            // CTL, encryption, LinkedIn) based on operationType and flags in TenantWorkloadConfig.
            BenchmarkConfig benchConfig = BenchmarkConfig.fromConfiguration(cfg);

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
            case WriteLatency:
            case WriteThroughput:
                break;
            default:
                if (!workloadCfg.isContentResponseOnWriteEnabled()) {
                    throw new IllegalArgumentException("contentResponseOnWriteEnabled parameter can only be set to false " +
                        "for write latency and write throughput operations");
                }
        }

        switch (workloadCfg.getOperationType()) {
            case ReadLatency:
            case ReadThroughput:
                break;
            default:
                if (workloadCfg.getSparsityWaitTime() != null) {
                    throw new IllegalArgumentException("sparsityWaitTime is not supported for " + workloadCfg.getOperationType());
                }
        }
    }
}
