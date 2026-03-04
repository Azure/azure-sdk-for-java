// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.benchmark.ctl.AsyncCtlWorkload;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionBenchmark;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionQueryBenchmark;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionQuerySinglePartitionMultiple;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionReadBenchmark;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionWriteBenchmark;
import com.azure.cosmos.benchmark.linkedin.LICtlWorkload;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.azure.cosmos.benchmark.Operation.CtlWorkload;
import static com.azure.cosmos.benchmark.Operation.LinkedInCtlWorkload;

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

            // Build BenchmarkConfig (requires workload config file)
            BenchmarkConfig benchConfig = BenchmarkConfig.fromConfiguration(cfg);
            TenantWorkloadConfig firstTenant = benchConfig.getTenantWorkloads().get(0);

            validateConfiguration(firstTenant, cfg);

            if (firstTenant.isSync()) {
                syncBenchmark(firstTenant, benchConfig);
            } else {
                if (firstTenant.getOperationType().equals(CtlWorkload)) {
                    asyncCtlWorkload(firstTenant, benchConfig);
                } else if (firstTenant.getOperationType().equals(LinkedInCtlWorkload)) {
                    linkedInCtlWorkload(firstTenant, benchConfig);
                } else if (firstTenant.isEncryptionEnabled()) {
                    asyncEncryptionBenchmark(firstTenant, benchConfig);
                } else {
                    asyncBenchmark(benchConfig);
                }
            }
        } catch (ParameterException e) {
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        }
    }

    private static void validateConfiguration(TenantWorkloadConfig workloadCfg, Configuration cfg) {
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

    private static void syncBenchmark(TenantWorkloadConfig workloadCfg, BenchmarkConfig benchConfig) throws Exception {
        LOGGER.info("Sync benchmark ...");
        SyncBenchmark<?> benchmark = null;
        try {
            switch (workloadCfg.getOperationType()) {
                case ReadThroughput:
                case ReadLatency:
                    benchmark = new SyncReadBenchmark(workloadCfg, benchConfig);
                    break;
                case WriteLatency:
                case WriteThroughput:
                    benchmark = new SyncWriteBenchmark(workloadCfg, benchConfig);
                    break;
                default:
                    throw new RuntimeException(workloadCfg.getOperationType() + " is not supported");
            }
            LOGGER.info("Starting {}", workloadCfg.getOperationType());
            benchmark.run();
        } finally {
            if (benchmark != null) {
                benchmark.shutdown();
            }
        }
    }

    /**
     * Async benchmark path: builds BenchmarkConfig from CLI args and delegates to BenchmarkOrchestrator.
     * Handles both single-tenant and multi-tenant modes via workload config file.
     */
    private static void asyncBenchmark(BenchmarkConfig benchConfig) throws Exception {
        LOGGER.info("Async benchmark via BenchmarkOrchestrator ({} tenants, {} cycles)...",
            benchConfig.getTenantWorkloads().size(), benchConfig.getCycles());
        new BenchmarkOrchestrator().run(benchConfig);
    }

    private static void asyncEncryptionBenchmark(TenantWorkloadConfig workloadCfg, BenchmarkConfig benchConfig) throws Exception {
        LOGGER.info("Async encryption benchmark ...");
        AsyncEncryptionBenchmark<?> benchmark = null;
        try {
            switch (workloadCfg.getOperationType()) {
                case WriteThroughput:
                case WriteLatency:
                    benchmark = new AsyncEncryptionWriteBenchmark(workloadCfg, benchConfig);
                    break;
                case ReadThroughput:
                case ReadLatency:
                    benchmark = new AsyncEncryptionReadBenchmark(workloadCfg, benchConfig);
                    break;
                case QueryCross:
                case QuerySingle:
                case QueryParallel:
                case QueryOrderby:
                case QueryTopOrderby:
                case QueryInClauseParallel:
                    benchmark = new AsyncEncryptionQueryBenchmark(workloadCfg, benchConfig);
                    break;
                case QuerySingleMany:
                    benchmark = new AsyncEncryptionQuerySinglePartitionMultiple(workloadCfg, benchConfig);
                    break;
                default:
                    throw new RuntimeException(workloadCfg.getOperationType() + " is not supported");
            }
            LOGGER.info("Starting {}", workloadCfg.getOperationType());
            benchmark.run();
        } finally {
            if (benchmark != null) {
                benchmark.shutdown();
            }
        }
    }

    private static void asyncCtlWorkload(TenantWorkloadConfig workloadCfg, BenchmarkConfig benchConfig) throws Exception {
        LOGGER.info("Async ctl workload");
        AsyncCtlWorkload benchmark = null;
        try {
            benchmark = new AsyncCtlWorkload(workloadCfg, benchConfig);
            LOGGER.info("Starting {}", workloadCfg.getOperationType());
            benchmark.run();
        } finally {
            if (benchmark != null) {
                benchmark.shutdown();
            }
        }
    }

    private static void linkedInCtlWorkload(TenantWorkloadConfig workloadCfg, BenchmarkConfig benchConfig) {
        LOGGER.info("Executing the LinkedIn ctl workload");
        LICtlWorkload workload = null;
        try {
            workload = new LICtlWorkload(workloadCfg, benchConfig);
            LOGGER.info("Setting up the LinkedIn ctl workload");
            workload.setup();
            LOGGER.info("Starting the LinkedIn ctl workload");
            workload.run();
        } catch (Exception e) {
            LOGGER.error("Exception received while executing the LinkedIn ctl workload", e);
            throw e;
        } finally {
            Optional.ofNullable(workload)
                .ifPresent(LICtlWorkload::shutdown);
        }
        LOGGER.info("Completed LinkedIn ctl workload execution");
    }
}
