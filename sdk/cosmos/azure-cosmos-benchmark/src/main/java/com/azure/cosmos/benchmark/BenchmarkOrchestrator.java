// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

        // Set up shared Micrometer registry (composite: Dropwizard bridge + optional AzureMonitor)
        CompositeMeterRegistry compositeRegistry = new CompositeMeterRegistry();

        // DropwizardMeterRegistry bridges Micrometer meters to a Dropwizard MetricRegistry,
        // which feeds CsvReporter/ConsoleReporter for periodic output. It is a MeterRegistry
        // and is added to the composite so SDK-emitted meters flow through to Dropwizard reporting.
        Path metricsDir = null;
        if (config.getReportingDirectory() != null) {
            metricsDir = Paths.get(config.getReportingDirectory(), "metrics");
        }
        BenchmarkMetricsReporter reporter = new BenchmarkMetricsReporter(metricsDir);
        compositeRegistry.add(reporter);
        reporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);

        MeterRegistry cosmosMicrometerRegistry = buildCosmosMicrometerRegistry();
        if (cosmosMicrometerRegistry != null) {
            compositeRegistry.add(cosmosMicrometerRegistry);
            logger.info("AzureMonitor registry added to composite registry");
        }

        if (config.isEnableJvmStats()) {
            new JvmGcMetrics().bindTo(compositeRegistry);
            new JvmMemoryMetrics().bindTo(compositeRegistry);
            new JvmThreadMetrics().bindTo(compositeRegistry);
            new ThreadPrefixGaugeSet(config.getPrintingInterval()).bindTo(compositeRegistry);
            logger.info("JVM stats enabled (gc, memory, threads, threadPrefix)");
        }

        // Prepare all tenants (inject shared state, set defaults)
        prepareTenants(config, compositeRegistry);

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
                .forRegistry(compositeRegistry,
                    resultUploaderClient
                        .getDatabase(config.getResultUploadDatabase())
                        .getContainer(config.getResultUploadContainer()),
                    operationSummary,
                    config.getTestVariationName(),
                    config.getBranchName(),
                    config.getCommitId(),
                    totalConcurrency)
                .build();
            resultReporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);
            logger.info("Result reporter started -> {}/{}",
                config.getResultUploadDatabase(), config.getResultUploadContainer());
        }

        // Netty HTTP connection pool metrics reporter (only when enabled).
        // Reactor Netty publishes pool gauges to Metrics.globalRegistry, so we add
        // a SimpleMeterRegistry there to capture them (separate from the composite registry
        // used for SDK operation metrics).
        NettyHttpMetricsReporter nettyMetricsReporter = null;
        SimpleMeterRegistry nettyHttpMeterRegistry = null;
        if (config.isEnableNettyHttpMetrics() && config.getReportingDirectory() != null) {
            nettyHttpMeterRegistry = new SimpleMeterRegistry();
            Metrics.addRegistry(nettyHttpMeterRegistry);
            logger.info("SimpleMeterRegistry added to globalRegistry for Reactor Netty pool gauge backing");

            Path nettyMetricsDir = Paths.get(config.getReportingDirectory());
            nettyMetricsReporter = new NettyHttpMetricsReporter(nettyHttpMeterRegistry, nettyMetricsDir);
            nettyMetricsReporter.start(config.getPrintingInterval(), TimeUnit.SECONDS);
        }

        reporter.report();
        logger.info("[LIFECYCLE] PRE_CREATE timestamp={}", Instant.now());
        logger.info("BenchmarkConfig: {}", config);

        // ======== Lifecycle loop ========
        try {
            runLifecycleLoop(config, reporter);
        } finally {
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
            if (nettyMetricsReporter != null) {
                nettyMetricsReporter.stop();
            }
            if (nettyHttpMeterRegistry != null) {
                Metrics.removeRegistry(nettyHttpMeterRegistry);
            }
            clearGlobalSystemProperties();
        }
    }

    // ======== Lifecycle loop (create -> run -> close -> settle x N) ========

    private void runLifecycleLoop(BenchmarkConfig config,
                                  BenchmarkMetricsReporter reporter) throws Exception {
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
                List<Benchmark> benchmarks = createBenchmarks(config);
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

    private List<Benchmark> createBenchmarks(BenchmarkConfig config) throws Exception {
        List<Benchmark> benchmarks = new ArrayList<>();
        for (TenantWorkloadConfig tenant : config.getTenantWorkloads()) {
            benchmarks.add(createBenchmarkForOperation(tenant));
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

    private Benchmark createBenchmarkForOperation(TenantWorkloadConfig cfg) throws Exception {
        // Sync benchmarks
        if (cfg.isSync()) {
            switch (cfg.getOperationType()) {
                case ReadThroughput:
                case ReadLatency:
                    return new SyncReadBenchmark(cfg);
                case WriteThroughput:
                case WriteLatency:
                    return new SyncWriteBenchmark(cfg);
                default:
                    throw new IllegalArgumentException(
                        "Sync mode is not supported for operation: " + cfg.getOperationType());
            }
        }

        // CTL workloads
        if (cfg.getOperationType() == Operation.CtlWorkload) {
            return new AsyncCtlWorkload(cfg);
        }
        if (cfg.getOperationType() == Operation.LinkedInCtlWorkload) {
            return new LICtlWorkload(cfg);
        }

        // Encryption benchmarks
        if (cfg.isEncryptionEnabled()) {
            switch (cfg.getOperationType()) {
                case WriteThroughput:
                case WriteLatency:
                    return new AsyncEncryptionWriteBenchmark(cfg);
                case ReadThroughput:
                case ReadLatency:
                    return new AsyncEncryptionReadBenchmark(cfg);
                case QueryCross:
                case QuerySingle:
                case QueryParallel:
                case QueryOrderby:
                case QueryTopOrderby:
                case QueryInClauseParallel:
                    return new AsyncEncryptionQueryBenchmark(cfg);
                case QuerySingleMany:
                    return new AsyncEncryptionQuerySinglePartitionMultiple(cfg);
                default:
                    throw new IllegalArgumentException(
                        "Encryption is not supported for operation: " + cfg.getOperationType());
            }
        }

        // Default: async benchmarks
        switch (cfg.getOperationType()) {
            case ReadThroughput:
            case ReadLatency:
                return new AsyncReadBenchmark(cfg);
            case WriteThroughput:
            case WriteLatency:
                return new AsyncWriteBenchmark(cfg);
            case QueryCross:
            case QuerySingle:
            case QueryParallel:
            case QueryOrderby:
            case QueryAggregate:
            case QueryTopOrderby:
            case QueryAggregateTopOrderby:
            case QueryInClauseParallel:
            case ReadAllItemsOfLogicalPartition:
                return new AsyncQueryBenchmark(cfg);
            case ReadManyLatency:
            case ReadManyThroughput:
                return new AsyncReadManyBenchmark(cfg);
            case Mixed:
                return new AsyncMixedBenchmark(cfg);
            case QuerySingleMany:
                return new AsyncQuerySinglePartitionMultiple(cfg);
            case ReadMyWrites:
                return new ReadMyWriteWorkflow(cfg);
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
        if (instrumentationKey == null && appInsightsConnStr == null) {
            return null;
        }

        java.time.Duration step = java.time.Duration.ofSeconds(
            Integer.getInteger("azure.cosmos.monitoring.azureMonitor.step", 10));
        String testCategoryTag = System.getProperty("azure.cosmos.monitoring.azureMonitor.testCategory");
        boolean enabled = !Boolean.getBoolean("azure.cosmos.monitoring.azureMonitor.disabled");

        final String connStr = appInsightsConnStr;
        final String instrKey = instrumentationKey;
        final io.micrometer.azuremonitor.AzureMonitorConfig amConfig = new io.micrometer.azuremonitor.AzureMonitorConfig() {
            @Override
            public String get(String key) { return null; }

            @Override
            public String instrumentationKey() {
                return connStr != null ? null : instrKey;
            }

            @Override
            public String connectionString() { return connStr; }

            @Override
            public java.time.Duration step() { return step; }

            @Override
            public boolean enabled() { return enabled; }
        };

        String roleName = System.getenv("APPLICATIONINSIGHTS_ROLE_NAME");
        if (roleName != null) {
            com.microsoft.applicationinsights.TelemetryConfiguration.getActive().setRoleName(roleName);
        }

        MeterRegistry registry = new io.micrometer.azuremonitor.AzureMonitorMeterRegistry(
            amConfig, io.micrometer.core.instrument.Clock.SYSTEM);
        java.util.List<io.micrometer.core.instrument.Tag> globalTags = new java.util.ArrayList<>();
        if (!com.google.common.base.Strings.isNullOrEmpty(testCategoryTag)) {
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
