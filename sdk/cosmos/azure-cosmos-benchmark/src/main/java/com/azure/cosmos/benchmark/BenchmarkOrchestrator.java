// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.benchmark.ctl.AsyncCtlWorkload;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionQueryBenchmark;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionQuerySinglePartitionMultiple;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionReadBenchmark;
import com.azure.cosmos.benchmark.encryption.AsyncEncryptionWriteBenchmark;
import com.azure.cosmos.benchmark.linkedin.LICtlWorkload;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

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
        logger.info("  Output:    {}", config.getReportingDestination());

        if (config.getTenantWorkloads().isEmpty()) {
            logger.error("No tenants provided");
            return;
        }

        setGlobalSystemProperties(config);

        // Console logging is always active — provides real-time visibility regardless of destination.
        LoggingMeterRegistry loggingRegistry = LoggingMeterRegistry.builder(
            new LoggingRegistryConfig() {
                @Override
                public String get(String key) { return null; }

                @Override
                public java.time.Duration step() {
                    return java.time.Duration.ofSeconds(config.getPrintingInterval());
                }
            }).build();
        logger.info("Console reporter started (LoggingMeterRegistry, interval={}s)",
            config.getPrintingInterval());

        JvmGcMetrics gcMetrics = null;
        ThreadPrefixGaugeSet threadPrefixGaugeSet = null;

        if (config.isEnableJvmStats()) {
            // JVM metrics are system-wide and live across all cycles.
            // Bind them to the loggingRegistry so they appear in console output.
            gcMetrics = new JvmGcMetrics();
            gcMetrics.bindTo(loggingRegistry);
            new JvmMemoryMetrics().bindTo(loggingRegistry);
            new JvmThreadMetrics().bindTo(loggingRegistry);
            threadPrefixGaugeSet = new ThreadPrefixGaugeSet(config.getPrintingInterval());
            threadPrefixGaugeSet.bindTo(loggingRegistry);
            logger.info("JVM stats enabled (gc, memory, threads, threadPrefix)");
        }

        logger.info("[LIFECYCLE] PRE_CREATE timestamp={}", Instant.now());
        logger.info("BenchmarkConfig: {}", config);

        // ======== Lifecycle loop ========
        // Reporters and per-cycle registries are created inside the loop so that
        // each cycle starts with fresh metrics.  The SDK calls registry.clear() +
        // registry.close() when a CosmosClient is destroyed; by giving each cycle
        // its own registry we avoid cross-cycle contamination.
        try {
            runLifecycleLoop(config, loggingRegistry);
        } finally {
            loggingRegistry.close();
            if (gcMetrics != null) {
                gcMetrics.close();
            }
            if (threadPrefixGaugeSet != null) {
                threadPrefixGaugeSet.close();
            }
            clearGlobalSystemProperties();
        }
    }

    // ======== Lifecycle loop (create -> run -> close -> settle x N) ========

    private void runLifecycleLoop(BenchmarkConfig config,
                                  LoggingMeterRegistry loggingRegistry) throws Exception {
        int totalCycles = config.getCycles();
        List<TenantWorkloadConfig> tenants = config.getTenantWorkloads();

        logger.info("Starting benchmark: {} cycles x {} tenants", totalCycles, tenants.size());
        long startTime = System.currentTimeMillis();

        Scheduler benchmarkScheduler = Schedulers.parallel();

        AtomicInteger threadCounter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(tenants.size(), r -> {
            Thread t = new Thread(r, "tenant-worker-" + threadCounter.getAndIncrement());
            t.setDaemon(false);
            return t;
        });

        try {
            for (int cycle = 1; cycle <= totalCycles; cycle++) {
                logger.info("[LIFECYCLE] CYCLE_START cycle={} timestamp={}", cycle, Instant.now());

                // --- Fresh per-cycle metrics infrastructure ---
                // Each cycle gets its own registry + reporter. The Cosmos SDK calls
                // registry.clear() + registry.close() when a CosmosClient is destroyed,
                // so we give it a disposable registry that we flush before shutdown.
                CompositeMeterRegistry cycleRegistry = new CompositeMeterRegistry();
                cycleRegistry.add(loggingRegistry);

                // Netty HTTP connection pool metrics
                boolean addedToGlobal = false;
                if (config.isEnableNettyHttpMetrics()) {
                    Metrics.addRegistry(cycleRegistry);
                    addedToGlobal = true;
                }

                CsvMetricsReporter csvReporter = null;
                CosmosMetricsReporter cosmosReporter = null;
                MeterRegistry appInsightsRegistry = null;

                try {
                    ReportingDestination destination = config.getReportingDestination();
                    if (destination != null) {
                        switch (destination) {
                            case CSV:
                                SimpleMeterRegistry csvRegistry = new SimpleMeterRegistry();
                                cycleRegistry.add(csvRegistry);
                                csvReporter = new CsvMetricsReporter(
                                    csvRegistry, config.getCsvReporterConfig().getReportingDirectory());
                                csvReporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);
                                break;

                            case COSMOSDB:
                                SimpleMeterRegistry cosmosSimpleRegistry = new SimpleMeterRegistry();
                                cycleRegistry.add(cosmosSimpleRegistry);
                                Set<String> ops = new LinkedHashSet<>();
                                int totalConcurrency = 0;
                                for (TenantWorkloadConfig t : tenants) {
                                    ops.add(t.getOperation() != null ? t.getOperation() : "Unknown");
                                    totalConcurrency += t.getConcurrency();
                                }
                                cosmosReporter = CosmosMetricsReporter.create(
                                    cosmosSimpleRegistry, config.getCosmosReporterConfig(),
                                    String.join("+", ops), totalConcurrency);
                                cosmosReporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);
                                break;

                            case APPLICATION_INSIGHTS:
                                appInsightsRegistry = buildAppInsightsMeterRegistry(
                                    config.getAppInsightsReporterConfig());
                                if (appInsightsRegistry != null) {
                                    cycleRegistry.add(appInsightsRegistry);
                                } else {
                                    logger.warn("APPLICATION_INSIGHTS reporting destination selected, but no "
                                        + "Application Insights meter registry was created. Metrics will not be "
                                        + "exported for this cycle. Please verify the Application Insights "
                                        + "configuration, including the connection string.");
                                }
                                break;
                        }
                    }

                    // Inject fresh registry into tenants for this cycle
                    prepareTenants(config, cycleRegistry);

                    // 1. Capture baseline CPU before benchmark creation (which includes data ingestion)
                    double baselineCpu = CpuMonitor.captureProcessCpuLoad();

                    // 2. Create clients (constructors perform data ingestion)
                    List<Benchmark> benchmarks = createBenchmarks(config, benchmarkScheduler);
                    logger.info("[LIFECYCLE] POST_CREATE cycle={} clients={} timestamp={}",
                        cycle, benchmarks.size(), Instant.now());

                    // 3. Cool-down: wait for CPU to settle after data ingestion before measuring workload
                    CpuMonitor.awaitCoolDown(baselineCpu);

                    // 4. Run workload in parallel
                    runWorkload(benchmarks, cycle, executor);
                    logger.info("[LIFECYCLE] POST_WORKLOAD cycle={} timestamp={}", cycle, Instant.now());

                    // 5. Flush reporters before shutdown destroys the registry
                    if (csvReporter != null) {
                        csvReporter.stop();
                    }
                    if (cosmosReporter != null) {
                        cosmosReporter.stop();
                    }

                    // 6. Disconnect loggingRegistry before SDK clears the cycle registry
                    cycleRegistry.remove(loggingRegistry);
                    if (addedToGlobal) {
                        Metrics.removeRegistry(cycleRegistry);
                        addedToGlobal = false;
                    }

                    // 7. Close all clients (SDK will clear+close cycleRegistry — safe now)
                    shutdownBenchmarks(benchmarks, cycle);
                    logger.info("[LIFECYCLE] POST_CLOSE cycle={} timestamp={}", cycle, Instant.now());

                    if (appInsightsRegistry != null) {
                        appInsightsRegistry.close();
                        appInsightsRegistry = null;
                    }
                } finally {
                    // Ensure cleanup even if an exception occurred mid-cycle
                    if (csvReporter != null) {
                        try { csvReporter.stop(); } catch (Exception e) { /* already stopped or best-effort */ }
                    }
                    if (cosmosReporter != null) {
                        try { cosmosReporter.stop(); } catch (Exception e) { /* already stopped or best-effort */ }
                    }
                    cycleRegistry.remove(loggingRegistry);
                    if (addedToGlobal) {
                        Metrics.removeRegistry(cycleRegistry);
                    }
                    if (appInsightsRegistry != null) {
                        try { appInsightsRegistry.close(); } catch (Exception e) { /* best-effort */ }
                    }
                }

                // 8. Settle
                if (config.getSettleTimeMs() > 0) {
                    logger.info(" Settling for {}ms...", config.getSettleTimeMs());
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
                logger.info("[LIFECYCLE] POST_SETTLE cycle={} timestamp={}", cycle, Instant.now());
            }
        } finally {
            logger.info("[LIFECYCLE] Shutting down executor...");
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

    private List<Benchmark> createBenchmarks(BenchmarkConfig config, Scheduler scheduler) throws Exception {
        List<Benchmark> benchmarks = new ArrayList<>();
        for (TenantWorkloadConfig tenant : config.getTenantWorkloads()) {
            benchmarks.add(createBenchmarkForOperation(tenant, scheduler));
        }
        return benchmarks;
    }

    private void runWorkload(List<Benchmark> benchmarks, int cycle, ExecutorService executor) throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        final int currentCycle = cycle;
        for (Benchmark benchmark : benchmarks) {
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

    private void shutdownBenchmarks(List<Benchmark> benchmarks, int cycle) {
        for (Benchmark benchmark : benchmarks) {
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
    private void prepareTenants(BenchmarkConfig config, MeterRegistry sharedRegistry) {
        for (TenantWorkloadConfig tenant : config.getTenantWorkloads()) {
            // Inject shared micrometer registry for SDK telemetry
            tenant.setCosmosMicrometerRegistry(sharedRegistry);

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

    private Benchmark createBenchmarkForOperation(TenantWorkloadConfig cfg, Scheduler scheduler) throws Exception {
        // Sync benchmarks
        if (cfg.isSync()) {
            switch (cfg.getOperationType()) {
                case ReadThroughput:
                    return new SyncReadBenchmark(cfg);
                case WriteThroughput:
                    return new SyncWriteBenchmark(cfg);
                default:
                    throw new IllegalArgumentException(
                        "Sync mode is not supported for operation: " + cfg.getOperationType());
            }
        }

        // CTL workloads
        if (cfg.getOperationType() == Operation.CtlWorkload) {
            return new AsyncCtlWorkload(cfg, scheduler);
        }
        if (cfg.getOperationType() == Operation.LinkedInCtlWorkload) {
            return new LICtlWorkload(cfg);
        }

        // Encryption benchmarks
        if (cfg.isEncryptionEnabled()) {
            switch (cfg.getOperationType()) {
                case WriteThroughput:
                    return new AsyncEncryptionWriteBenchmark(cfg, scheduler);
                case ReadThroughput:
                    return new AsyncEncryptionReadBenchmark(cfg, scheduler);
                case QueryCross:
                case QuerySingle:
                case QueryParallel:
                case QueryOrderby:
                case QueryTopOrderby:
                case QueryInClauseParallel:
                    return new AsyncEncryptionQueryBenchmark(cfg, scheduler);
                case QuerySingleMany:
                    return new AsyncEncryptionQuerySinglePartitionMultiple(cfg, scheduler);
                default:
                    throw new IllegalArgumentException(
                        "Encryption is not supported for operation: " + cfg.getOperationType());
            }
        }

        // Default: async benchmarks
        switch (cfg.getOperationType()) {
            case ReadThroughput:
                return new AsyncReadBenchmark(cfg, scheduler);
            case WriteThroughput:
                return new AsyncWriteBenchmark(cfg, scheduler);
            case QueryCross:
            case QuerySingle:
            case QueryParallel:
            case QueryOrderby:
            case QueryAggregate:
            case QueryTopOrderby:
            case QueryAggregateTopOrderby:
            case QueryInClauseParallel:
            case ReadAllItemsOfLogicalPartition:
                return new AsyncQueryBenchmark(cfg, scheduler);
            case ReadManyThroughput:
                return new AsyncReadManyBenchmark(cfg, scheduler);
            case Mixed:
                return new AsyncMixedBenchmark(cfg, scheduler);
            case QuerySingleMany:
                return new AsyncQuerySinglePartitionMultiple(cfg, scheduler);
            case ReadMyWrites:
                return new ReadMyWriteWorkflow(cfg, scheduler);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + cfg.getOperationType());
        }
    }

    // ======== Application Insights registry ========

    private MeterRegistry buildAppInsightsMeterRegistry(AppInsightsReporterConfig config) {
        String connStr = config.getConnectionString();

        if (connStr == null) {
            return null;
        }

        java.time.Duration step = java.time.Duration.ofSeconds(config.getStepSeconds());
        String testCategoryTag = config.getTestCategory();

        final String finalConnStr = connStr;
        final io.micrometer.azuremonitor.AzureMonitorConfig amConfig = new io.micrometer.azuremonitor.AzureMonitorConfig() {
            @Override
            public String get(String key) { return null; }

            @Override
            public String connectionString() { return finalConnStr; }

            @Override
            public java.time.Duration step() { return step; }

            @Override
            public boolean enabled() { return true; }
        };

        String roleName = System.getenv("APPLICATIONINSIGHTS_ROLE_NAME");
        if (roleName != null) {
            com.microsoft.applicationinsights.TelemetryConfiguration.getActive().setRoleName(roleName);
        }

        MeterRegistry registry = new io.micrometer.azuremonitor.AzureMonitorMeterRegistry(
            amConfig, io.micrometer.core.instrument.Clock.SYSTEM);
        java.util.List<io.micrometer.core.instrument.Tag> globalTags = new java.util.ArrayList<>();
        if (testCategoryTag != null && !testCategoryTag.isEmpty()) {
            globalTags.add(io.micrometer.core.instrument.Tag.of("TestCategory", testCategoryTag));
        }

        String roleInstance = System.getenv("APPLICATIONINSIGHTS_ROLE_INSTANCE");
        if (roleInstance != null) {
            globalTags.add(io.micrometer.core.instrument.Tag.of("cloud_RoleInstance", roleInstance));
        }

        registry.config().commonTags(globalTags);
        return registry;
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
        System.clearProperty("COSMOS.NETTY_HTTP_CLIENT_METRICS_ENABLED");
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

        if (config.isEnableNettyHttpMetrics()) {
            System.setProperty("COSMOS.NETTY_HTTP_CLIENT_METRICS_ENABLED", "true");
            logger.info("Reactor Netty HTTP connection pool metrics enabled");
        }

        logger.info("Global system properties set (circuit breaker: {}, PPAF: {}, minConnPoolSize: {})",
            config.isPartitionLevelCircuitBreakerEnabled(),
            config.isPerPartitionAutomaticFailoverRequired(),
            config.getMinConnectionPoolSizePerEndpoint());
    }
}
