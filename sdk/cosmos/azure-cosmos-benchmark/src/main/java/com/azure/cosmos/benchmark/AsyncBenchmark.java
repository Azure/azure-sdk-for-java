// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

abstract class AsyncBenchmark<T> {

    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor clientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();

    private final MetricRegistry metricsRegistry;

    private volatile Meter successMeter;
    private volatile Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    final Logger logger;
    final CosmosAsyncClient benchmarkWorkloadClient;
    CosmosAsyncContainer cosmosAsyncContainer;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
    final List<PojoizedJson> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    private AtomicBoolean warmupMode = new AtomicBoolean(false);

    AsyncBenchmark(TenantWorkloadConfig cfg, MetricRegistry sharedRegistry) {

        logger = LoggerFactory.getLogger(this.getClass());
        workloadConfig = cfg;
        this.metricsRegistry = sharedRegistry;

        final TokenCredential credential = cfg.isManagedIdentityRequired()
            ? cfg.buildTokenCredential()
            : null;

        boolean isManagedIdentityRequired = cfg.isManagedIdentityRequired();

        CosmosClientBuilder benchmarkSpecificClientBuilder = isManagedIdentityRequired ?
                new CosmosClientBuilder()
                        .credential(credential) :
                new CosmosClientBuilder()
                        .key(cfg.getMasterKey());

        benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder
                .endpoint(cfg.getServiceEndpoint())
                .preferredRegions(cfg.getPreferredRegionsList())
                .consistencyLevel(cfg.getConsistencyLevel())
                .userAgentSuffix(cfg.getApplicationName())
                .contentResponseOnWriteEnabled(cfg.isContentResponseOnWriteEnabled());

        clientBuilderAccessor
            .setRegionScopedSessionCapturingEnabled(benchmarkSpecificClientBuilder, cfg.isRegionScopedSessionContainerEnabled());

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(
                new CosmosDiagnosticsThresholds()
                    .setPointOperationLatencyThreshold(cfg.getPointOperationThreshold())
                    .setNonPointOperationLatencyThreshold(cfg.getNonPointOperationThreshold())
            );

        if (cfg.isDefaultLog4jLoggerEnabled()) {
            logger.info("Diagnostics thresholds Point: {}, Non-Point: {}",
                cfg.getPointOperationThreshold(),
                cfg.getNonPointOperationThreshold());
            telemetryConfig.diagnosticsHandler(
                new CosmosSamplingDiagnosticsLogger(10, 10_000)
            );
        }

        // Cosmos SDK micrometer metrics registry (injected by orchestrator)
        MeterRegistry micrometerRegistry = cfg.getCosmosMicrometerRegistry();
        if (micrometerRegistry != null) {
            logger.info("Using injected Cosmos micrometer registry: {}", micrometerRegistry.getClass().getSimpleName());
            CosmosMicrometerMetricsOptions metricOptions = new CosmosMicrometerMetricsOptions()
                .meterRegistry(micrometerRegistry)
                .applyDiagnosticThresholdsForTransportLevelMeters(true)
                .setEnabled(true);
            telemetryConfig.metricsOptions(metricOptions);
        } else {
            telemetryConfig.metricsOptions(
                new CosmosMicrometerMetricsOptions().meterRegistry(null));
        }

        benchmarkSpecificClientBuilder.clientTelemetryConfig(telemetryConfig);

        if (cfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(cfg.getMaxConnectionPoolSize());
            benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder.gatewayMode(gatewayConnectionConfig);
        }

        benchmarkWorkloadClient = benchmarkSpecificClientBuilder.buildAsyncClient();

        try {
            cosmosAsyncDatabase = benchmarkWorkloadClient.getDatabase(cfg.getDatabaseId());
            cosmosAsyncDatabase.read().block();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {

                if (isManagedIdentityRequired) {
                    throw new IllegalStateException("If managed identity is required, " +
                        "either pre-create a database and a container or use the management SDK.");
                }

                benchmarkWorkloadClient.createDatabase(cfg.getDatabaseId()).block();
                cosmosAsyncDatabase = benchmarkWorkloadClient.getDatabase(cfg.getDatabaseId());
                logger.info("Database {} is created for this test", cfg.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        try {
            cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(cfg.getContainerId());
            cosmosAsyncContainer.read().block();

        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {

                if (isManagedIdentityRequired) {
                    throw new IllegalStateException("If managed identity is required, " +
                        "either pre-create a database and a container or use the management SDK.");
                }

                cosmosAsyncDatabase.createContainer(
                    cfg.getContainerId(),
                    TenantWorkloadConfig.DEFAULT_PARTITION_KEY_PATH,
                    ThroughputProperties.createManualThroughput(cfg.getThroughput())
                ).block();

                cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(cfg.getContainerId());

                try {
                    Thread.sleep(30_000);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                logger.info("Collection {} is created for this test", cfg.getContainerId());
                collectionCreated = true;
            } else {
                throw e;
            }
        }

        partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());

        ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

        if (cfg.getOperationType() != Operation.WriteLatency
            && cfg.getOperationType() != Operation.WriteThroughput
            && cfg.getOperationType() != Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", cfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    cfg.getDocumentDataFieldCount());
                Flux<PojoizedJson> obs = cosmosAsyncContainer
                    .createItem(newDoc)
                    .retryWhen(Retry.max(5).filter((error) -> {
                        if (!(error instanceof CosmosException)) {
                            return false;
                        }
                        final CosmosException cosmosException = (CosmosException) error;
                        if (cosmosException.getStatusCode() == 410 ||
                            cosmosException.getStatusCode() == 408 ||
                            cosmosException.getStatusCode() == 429 ||
                            cosmosException.getStatusCode() == 500 ||
                            cosmosException.getStatusCode() == 503) {
                            return true;
                        }

                        return false;
                    }))
                    .onErrorResume(
                        (error) -> {
                            if (!(error instanceof CosmosException)) {
                                return false;
                            }
                            final CosmosException cosmosException = (CosmosException) error;
                            if (cosmosException.getStatusCode() == 409) {
                                return true;
                            }

                            return false;
                        },
                        (conflictException) -> cosmosAsyncContainer.readItem(
                            uuid, new PartitionKey(partitionKey), PojoizedJson.class)
                    )
                    .map(resp -> {
                        PojoizedJson x =
                            resp.getItem();
                        return x;
                    })
                    .flux();
                createDocumentObservables.add(obs);
            }
        }

        docsToRead = Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block();
        logger.info("Finished pre-populating {} documents", cfg.getNumberOfPreCreatedDocuments());

        init();

        // Proactive connection management (Direct mode only)
        boolean shouldOpenConnectionsAndInitCaches = cfg.getConnectionMode() == ConnectionMode.DIRECT
                && cfg.isProactiveConnectionManagementEnabled()
                && !cfg.isUseUnWarmedUpContainer();

        if (shouldOpenConnectionsAndInitCaches) {
            logger.info("Proactively establishing connections...");

            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();
            cosmosContainerIdentities.add(new CosmosContainerIdentity(
                cfg.getDatabaseId(), cfg.getContainerId()));

            CosmosContainerProactiveInitConfigBuilder proactiveInitConfigBuilder =
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(cfg.getProactiveConnectionRegionsCount());

            CosmosClientBuilder proactiveClientBuilder = isManagedIdentityRequired ?
                new CosmosClientBuilder().credential(credential) :
                new CosmosClientBuilder().key(cfg.getMasterKey());
            proactiveClientBuilder = proactiveClientBuilder
                .endpoint(cfg.getServiceEndpoint())
                .preferredRegions(cfg.getPreferredRegionsList())
                .directMode();

            if (cfg.getAggressiveWarmupDuration().equals(Duration.ZERO)) {
                proactiveClientBuilder = proactiveClientBuilder
                    .openConnectionsAndInitCaches(proactiveInitConfigBuilder.build())
                    .endpointDiscoveryEnabled(true);
            } else {
                logger.info("Setting aggressive proactive connection establishment duration: {}",
                    cfg.getAggressiveWarmupDuration());
                proactiveInitConfigBuilder.setAggressiveWarmupDuration(cfg.getAggressiveWarmupDuration());
                benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder
                    .openConnectionsAndInitCaches(proactiveInitConfigBuilder.build())
                    .endpointDiscoveryEnabled(true);
            }

            try (CosmosAsyncClient openConnectionsClient = proactiveClientBuilder.buildAsyncClient()) {
                if (!isManagedIdentityRequired) {
                    openConnectionsClient.createDatabaseIfNotExists(cosmosAsyncDatabase.getId()).block();
                }
                CosmosAsyncDatabase dbForProactive = openConnectionsClient.getDatabase(cosmosAsyncDatabase.getId());
                if (!isManagedIdentityRequired) {
                    dbForProactive.createContainerIfNotExists(cfg.getContainerId(), "/id").block();
                }
                cosmosAsyncContainer = dbForProactive.getContainer(cfg.getContainerId());
            }
        }

        // Unwarmed container mode (Direct mode only)
        if (!cfg.isProactiveConnectionManagementEnabled() && cfg.isUseUnWarmedUpContainer()) {
            logger.info("Creating unwarmed container");

            CosmosClientBuilder unwarmedBuilder = isManagedIdentityRequired ?
                new CosmosClientBuilder().credential(credential) :
                new CosmosClientBuilder().key(cfg.getMasterKey());

            try (CosmosAsyncClient unwarmedClient = unwarmedBuilder
                    .endpoint(cfg.getServiceEndpoint())
                    .preferredRegions(cfg.getPreferredRegionsList())
                    .directMode()
                    .buildAsyncClient()) {

                if (!isManagedIdentityRequired) {
                    unwarmedClient.createDatabaseIfNotExists(cfg.getDatabaseId()).block();
                }
                CosmosAsyncDatabase dbForUnwarmed = unwarmedClient.getDatabase(cfg.getDatabaseId());
                if (!isManagedIdentityRequired) {
                    dbForUnwarmed.createContainerIfNotExists(cfg.getContainerId(), "/id").block();
                }
                cosmosAsyncContainer = dbForUnwarmed.getContainer(cfg.getContainerId());
            }
        }
    }

    protected void init() {
    }

    void shutdown() {
        if (workloadConfig.isSuppressCleanup()) {
            logger.info("Skipping cleanup of database/container (suppressCleanup=true)");
        } else if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", workloadConfig.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosAsyncContainer.delete().block();
            logger.info("Deleted temporary collection {} created for this test", workloadConfig.getContainerId());
        }

        benchmarkWorkloadClient.close();
    }

    protected void onSuccess() {
    }

    protected void initializeMetersIfSkippedEnoughOperations(AtomicLong count) {
        if (workloadConfig.getSkipWarmUpOperations() > 0) {
            if (count.get() >= workloadConfig.getSkipWarmUpOperations()) {
                if (warmupMode.get()) {
                    synchronized (this) {
                        if (warmupMode.get()) {
                            logger.info("Warmup phase finished. Starting capturing perf numbers ....");
                            resetMeters();
                            initializeMeter();
                            warmupMode.set(false);
                        }
                    }
                }
            }
        }
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract void performWorkload(BaseSubscriber<T> baseSubscriber, long i) throws Exception;

    private void resetMeters() {
        metricsRegistry.remove(TenantWorkloadConfig.SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(TenantWorkloadConfig.FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(workloadConfig.getOperationType())) {
            metricsRegistry.remove(TenantWorkloadConfig.LATENCY_METER_NAME);
        }
    }

    private void initializeMeter() {
        successMeter = metricsRegistry.meter(TenantWorkloadConfig.SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(TenantWorkloadConfig.FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(workloadConfig.getOperationType())) {
            latency = metricsRegistry.register(TenantWorkloadConfig.LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        }
    }

    private boolean latencyAwareOperations(Operation operation) {
        switch (workloadConfig.getOperationType()) {
            case ReadLatency:
            case WriteLatency:
            case QueryInClauseParallel:
            case QueryCross:
            case QuerySingle:
            case QuerySingleMany:
            case QueryParallel:
            case QueryOrderby:
            case QueryAggregate:
            case QueryAggregateTopOrderby:
            case QueryTopOrderby:
            case Mixed:
            case ReadAllItemsOfLogicalPartition:
            case ReadManyLatency:
                return true;
            default:
                return false;
        }
    }

    void run() throws Exception {
        initializeMeter();
        if (workloadConfig.getSkipWarmUpOperations() > 0) {
            logger.info("Starting warm up phase. Executing {} operations to warm up ...", workloadConfig.getSkipWarmUpOperations());
            warmupMode.set(true);
        }

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for (i = 0; shouldContinue(startTime, i); i++) {

            BaseSubscriber<T> baseSubscriber = new BaseSubscriber<T>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    super.hookOnSubscribe(subscription);
                }

                @Override
                protected void hookOnNext(T value) {
                    logger.debug("hookOnNext: {}, count:{}", value, count.get());
                }

                @Override
                protected void hookOnCancel() {
                    this.hookOnError(new CancellationException());
                }

                @Override
                protected void hookOnComplete() {
                    initializeMetersIfSkippedEnoughOperations(count);
                    successMeter.mark();
                    concurrencyControlSemaphore.release();
                    AsyncBenchmark.this.onSuccess();

                    synchronized (count) {
                        count.incrementAndGet();
                        count.notify();
                    }
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    initializeMetersIfSkippedEnoughOperations(count);
                    failureMeter.mark();

                    logger.error("Encountered failure {} on thread {}" ,
                        throwable.getMessage(), Thread.currentThread().getName(), throwable);
                    concurrencyControlSemaphore.release();
                    AsyncBenchmark.this.onError(throwable);

                    synchronized (count) {
                        count.incrementAndGet();
                        count.notify();
                    }
                }
            };

            performWorkload(baseSubscriber, i);
        }

        synchronized (count) {
            while (count.get() < i) {
                count.wait();
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            workloadConfig.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));
    }

    /**
     * Check if the benchmark should continue running.
     * Supports both count-based (numberOfOperations) and time-based (maxRunningTimeDuration) termination.
     */
    private boolean shouldContinue(long startTimeMillis, long iterationCount) {
        Duration maxDuration = workloadConfig.getMaxRunningTimeDuration();
        if (maxDuration == null) {
            return iterationCount < workloadConfig.getNumberOfOperations();
        }
        return startTimeMillis + maxDuration.toMillis() > System.currentTimeMillis();
    }

    protected Mono sparsityMono(long i) {
        Duration duration = workloadConfig.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            if (workloadConfig.getSkipWarmUpOperations() > i) {
                // don't wait during warmup
                return null;
            }
            return Mono.delay(duration);
        }
        return null;
    }

}
