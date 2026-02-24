// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.ScheduledReporter;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Benchmark orchestrator. Sets up infrastructure (metrics, reporters, system properties),
 * then runs the lifecycle loop (create clients -> run workload -> close -> settle x N cycles).
 */
public class BenchmarkOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkOrchestrator.class);

    public void run(BenchmarkConfig config) throws Exception {
        String testRunId = String.format("bench-%s",
            Instant.now().toString().replace(':', '-'));

        logger.info("=== Benchmark Orchestrator ===");
        logger.info("  Cycles:    {}", config.getCycles());
        logger.info("  Tenants:   {}", config.getTenantWorkloads().size());
        logger.info("  Run ID:    {}", testRunId);
        logger.info("  Output:    {}", config.getReportingDirectory());

        if (config.getTenantWorkloads().isEmpty()) {
            logger.error("No tenants provided");
            return;
        }

        setGlobalSystemProperties(config);

        // Set up shared metric registry
        MetricRegistry registry = new MetricRegistry();
        if (config.isEnableJvmStats()) {
            registry.register("gc", new GarbageCollectorMetricSet());
            registry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            registry.register("memory", new MemoryUsageGaugeSet());
            registry.register("threadPrefix", new ThreadPrefixGaugeSet());
            logger.info("JVM stats enabled (gc, threads, memory, threadPrefix)");
        }

        // Prepare all tenants (inject shared state, set defaults)
        prepareTenants(config);

        // Reporter selection: CSV > Console
        ScheduledReporter reporter;
        if (config.getReportingDirectory() != null) {
            Path metricsDir = Paths.get(config.getReportingDirectory(), "metrics");
            Files.createDirectories(metricsDir);
            reporter = CsvReporter.forRegistry(registry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(metricsDir.toFile());
            logger.info("CSV metrics reporter started -> {}", metricsDir);
        } else {
            reporter = ConsoleReporter.forRegistry(registry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
            logger.info("Console reporter started");
        }
        reporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);

        // Optional: Result uploader
        CosmosClient resultUploaderClient = null;
        CosmosTotalResultReporter resultReporter = null;
        if (config.getResultUploadDatabase() != null
            && config.getResultUploadContainer() != null
            && config.getResultUploadEndpoint() != null) {
            resultUploaderClient = new CosmosClientBuilder()
                .endpoint(config.getResultUploadEndpoint())
                .key(config.getResultUploadKey())
                .buildClient();
            Set<String> ops = new LinkedHashSet<>();
            int totalConcurrency = 0;
            for (TenantWorkloadConfig t : config.getTenantWorkloads()) {
                ops.add(t.getOperation() != null ? t.getOperation() : "Unknown");
                totalConcurrency += t.getConcurrency();
            }
            String operationSummary = String.join("+", ops);
            resultReporter = CosmosTotalResultReporter
                .forRegistry(registry,
                    resultUploaderClient
                        .getDatabase(config.getResultUploadDatabase())
                        .getContainer(config.getResultUploadContainer()),
                    operationSummary,
                    config.getTestVariationName(),
                    config.getBranchName(),
                    config.getCommitId(),
                    totalConcurrency)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
            resultReporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);
            logger.info("Result reporter started -> {}/{}",
                config.getResultUploadDatabase(), config.getResultUploadContainer());
        }

        reporter.report();
        logger.info("[LIFECYCLE] PRE_CREATE timestamp={}", Instant.now());
        logger.info("BenchmarkConfig: {}", config);

        // ======== Lifecycle loop ========
        runLifecycleLoop(config, registry, reporter);

        // Cleanup reporters
        reporter.report();
        reporter.stop();
        if (resultReporter != null) {
            resultReporter.report();
            resultReporter.stop();
        }
        if (resultUploaderClient != null) {
            resultUploaderClient.close();
        }
        clearGlobalSystemProperties();
    }

    // ======== Lifecycle loop (create -> run -> close -> settle x N) ========

    private void runLifecycleLoop(BenchmarkConfig config, MetricRegistry registry,
                                  ScheduledReporter reporter) throws Exception {
        int totalCycles = config.getCycles();
        List<TenantWorkloadConfig> tenants = config.getTenantWorkloads();

        logger.info("Starting benchmark: {} cycles x {} tenants", totalCycles, tenants.size());
        long startTime = System.currentTimeMillis();

        AtomicInteger threadCounter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(tenants.size(), r -> {
            Thread t = new Thread(r, "tenant-worker-" + threadCounter.getAndIncrement());
            t.setDaemon(false);
            return t;
        });

        try {
            for (int cycle = 1; cycle <= totalCycles; cycle++) {
                reporter.report();
                logger.info("[LIFECYCLE] CYCLE_START cycle={} timestamp={}", cycle, Instant.now());

                // 1. Create clients
                List<AsyncBenchmark<?>> benchmarks = createBenchmarks(config, registry);
                reporter.report();
                logger.info("[LIFECYCLE] POST_CREATE cycle={} clients={} timestamp={}",
                    cycle, benchmarks.size(), Instant.now());

                // 2. Run workload in parallel
                runWorkload(benchmarks, cycle, executor);
                reporter.report();
                logger.info("[LIFECYCLE] POST_WORKLOAD cycle={} timestamp={}", cycle, Instant.now());

                // 3. Close all clients
                shutdownBenchmarks(benchmarks, cycle);
                reporter.report();
                logger.info("[LIFECYCLE] POST_CLOSE cycle={} timestamp={}", cycle, Instant.now());

                // 4. Settle
                if (config.getSettleTimeMs() > 0) {
                    logger.info("  Settling for {}ms...", config.getSettleTimeMs());
                    long halfSettle = config.getSettleTimeMs() / 2;
                    Thread.sleep(halfSettle);
                    if (config.isGcBetweenCycles()) {
                        System.gc();
                    }
                    Thread.sleep(config.getSettleTimeMs() - halfSettle);
                    if (config.isGcBetweenCycles()) {
                        System.gc();
                    }
                }
                reporter.report();
                logger.info("[LIFECYCLE] POST_SETTLE cycle={} timestamp={}", cycle, Instant.now());
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.warn("Executor did not terminate within the timeout");
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.error("Executor did not terminate after shutdownNow");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while awaiting executor termination", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        long durationSec = (System.currentTimeMillis() - startTime) / 1000;
        logger.info("[LIFECYCLE] COMPLETE cycles={} duration={}s timestamp={}",
            totalCycles, durationSec, Instant.now());
    }

    private List<AsyncBenchmark<?>> createBenchmarks(BenchmarkConfig config, MetricRegistry registry) {
        List<AsyncBenchmark<?>> benchmarks = new ArrayList<>();
        for (TenantWorkloadConfig tenant : config.getTenantWorkloads()) {
            benchmarks.add(createBenchmarkForOperation(tenant, registry));
        }
        return benchmarks;
    }

    private void runWorkload(List<AsyncBenchmark<?>> benchmarks, int cycle, ExecutorService executor) throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        final int currentCycle = cycle;
        for (AsyncBenchmark<?> benchmark : benchmarks) {
            futures.add(executor.submit(() -> {
                try {
                    benchmark.run();
                } catch (Exception e) {
                    logger.error("Benchmark failed in cycle " + currentCycle, e);
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get();
        }
    }

    private void shutdownBenchmarks(List<AsyncBenchmark<?>> benchmarks, int cycle) {
        for (AsyncBenchmark<?> benchmark : benchmarks) {
            try {
                benchmark.shutdown();
            } catch (Exception e) {
                logger.error("Shutdown failed in cycle " + cycle, e);
            }
        }
        benchmarks.clear();
    }

    /**
     * Prepare all tenant configs before the lifecycle loop.
     * Centralizes all tenant mutation: suppressCleanup, applicationName suffix,
     * micrometer registry injection.
     */
    private void prepareTenants(BenchmarkConfig config) {
        MeterRegistry cosmosMicrometerRegistry = buildCosmosMicrometerRegistry();
        if (cosmosMicrometerRegistry != null) {
            logger.info("Cosmos micrometer registry: {}", cosmosMicrometerRegistry.getClass().getSimpleName());
        }

        for (TenantWorkloadConfig tenant : config.getTenantWorkloads()) {
            // Inject shared micrometer registry
            tenant.setCosmosMicrometerRegistry(cosmosMicrometerRegistry);

            // Propagate suppressCleanup from orchestrator config
            if (config.isSuppressCleanup()) {
                tenant.setSuppressCleanup(true);
            }

            // Ensure unique applicationName per tenant
            if (tenant.getApplicationName() == null || tenant.getApplicationName().isEmpty()) {
                tenant.setApplicationName("mt-bench-" + tenant.getId());
            } else if (!tenant.getApplicationName().contains(tenant.getId())) {
                tenant.setApplicationName(tenant.getApplicationName() + "-" + tenant.getId());
            }
        }
    }

    // ======== Benchmark factory ========

    private AsyncBenchmark<?> createBenchmarkForOperation(TenantWorkloadConfig cfg, MetricRegistry registry) {
        switch (cfg.getOperationType()) {
            case ReadThroughput:
            case ReadLatency:
                return new AsyncReadBenchmark(cfg, registry);
            case WriteThroughput:
            case WriteLatency:
                return new AsyncWriteBenchmark(cfg, registry);
            case QueryCross:
            case QuerySingle:
            case QueryParallel:
            case QueryOrderby:
            case QueryAggregate:
            case QueryTopOrderby:
            case QueryAggregateTopOrderby:
            case QueryInClauseParallel:
            case ReadAllItemsOfLogicalPartition:
                return new AsyncQueryBenchmark(cfg, registry);
            case ReadManyLatency:
            case ReadManyThroughput:
                return new AsyncReadManyBenchmark(cfg, registry);
            case Mixed:
                return new AsyncMixedBenchmark(cfg, registry);
            case QuerySingleMany:
                return new AsyncQuerySinglePartitionMultiple(cfg, registry);
            case ReadMyWrites:
                return new ReadMyWriteWorkflow(cfg, registry);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + cfg.getOperationType());
        }
    }

    // ======== Cosmos micrometer registry ========

    private MeterRegistry buildCosmosMicrometerRegistry() {
        String instrumentationKey = System.getProperty("azure.cosmos.monitoring.azureMonitor.instrumentationKey",
            StringUtils.defaultString(
                com.google.common.base.Strings.emptyToNull(
                    System.getenv("AZURE_INSTRUMENTATION_KEY")), null));
        String appInsightsConnStr = System.getProperty("applicationinsights.connection.string",
            StringUtils.defaultString(
                com.google.common.base.Strings.emptyToNull(
                    System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING")), null));
        if (instrumentationKey != null || appInsightsConnStr != null) {
            Configuration tempCfg = new Configuration();
            return tempCfg.getAzureMonitorMeterRegistry();
        }

        return null;
    }

    // ======== Global system properties ========

    private void clearGlobalSystemProperties() {
        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
        System.clearProperty("COSMOS.STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS");
        System.clearProperty("COSMOS.ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS");
        System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
        System.clearProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED");
        System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF");
        System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF");
        System.clearProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT");
    }

    private void setGlobalSystemProperties(BenchmarkConfig config) {
        if (config.isPartitionLevelCircuitBreakerEnabled()) {
            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"CONSECUTIVE_EXCEPTION_COUNT_BASED\","
                    + "\"consecutiveExceptionCountToleratedForReads\": 10,"
                    + "\"consecutiveExceptionCountToleratedForWrites\": 5}");
            System.setProperty("COSMOS.STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS", "60");
            System.setProperty("COSMOS.ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS", "30");
        }

        if (config.isPerPartitionAutomaticFailoverRequired()) {
            System.setProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED", "true");
            System.setProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED", "true");
            System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF", "5");
            System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF", "120");
        }

        if (config.getMinConnectionPoolSizePerEndpoint() >= 1) {
            System.setProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT",
                String.valueOf(config.getMinConnectionPoolSizePerEndpoint()));
        }

        logger.info("Global system properties set (circuit breaker: {}, PPAF: {}, minConnPoolSize: {})",
            config.isPartitionLevelCircuitBreakerEnabled(),
            config.isPerPartitionAutomaticFailoverRequired(),
            config.getMinConnectionPoolSizePerEndpoint());
    }
}
