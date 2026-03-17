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
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.ThroughputProperties;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

abstract class AsyncBenchmark<T> implements Benchmark {

    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor clientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();

    // Shared Reactor scheduler for benchmark workload dispatch.
    // Uses Schedulers.parallel() (global shared singleton). Must NOT be disposed by the benchmark.
    final Scheduler benchmarkScheduler;

    private boolean databaseCreated;
    private boolean collectionCreated;

    final Logger logger;
    final CosmosAsyncClient benchmarkWorkloadClient;
    CosmosAsyncContainer cosmosAsyncContainer;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
    final List<PojoizedJson> docsToRead;

    AsyncBenchmark(TenantWorkloadConfig cfg, Scheduler scheduler) {

        logger = LoggerFactory.getLogger(this.getClass());
        workloadConfig = cfg;
        this.benchmarkScheduler = scheduler;

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
                .contentResponseOnWriteEnabled(cfg.isContentResponseOnWriteEnabled())
                .connectionSharingAcrossClientsEnabled(cfg.isConnectionSharingAcrossClientsEnabled());

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
            if (cfg.isHttp2Enabled()) {
                logger.warn("HTTP/2 is enabled but connection mode is DIRECT; HTTP/2 settings are only applied in GATEWAY mode and will be ignored");
            }
            benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(cfg.getMaxConnectionPoolSize());
            if (cfg.isHttp2Enabled()) {
                Http2ConnectionConfig http2Config = gatewayConnectionConfig.getHttp2ConnectionConfig();
                http2Config.setEnabled(true);
                if (cfg.getHttp2MaxConcurrentStreams() != null) {
                    http2Config.setMaxConcurrentStreams(cfg.getHttp2MaxConcurrentStreams());
                }
                logger.info("HTTP/2 enabled with maxConcurrentStreams: {}",
                    http2Config.getMaxConcurrentStreams());
            }
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

        if (cfg.getOperationType() != Operation.WriteThroughput
            && cfg.getOperationType() != Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", cfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            List<PojoizedJson> generatedDocs = new ArrayList<>();
            List<CosmosItemOperation> bulkOperations = new ArrayList<>();

            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    cfg.getDocumentDataFieldCount());
                generatedDocs.add(newDoc);
                bulkOperations.add(CosmosBulkOperations.getCreateItemOperation(newDoc, new PartitionKey(uuid)));
            }

            CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
            List<CosmosBulkOperationResponse<Object>> failedResponses = Collections.synchronizedList(new ArrayList<>());
            cosmosAsyncContainer
                .executeBulkOperations(Flux.fromIterable(bulkOperations), bulkExecutionOptions)
                .doOnNext(response -> {
                    if (response.getResponse() == null || !response.getResponse().isSuccessStatusCode()) {
                        failedResponses.add(response);
                    }
                })
                .blockLast(Duration.ofMinutes(10));

            BenchmarkHelper.retryFailedBulkOperations(failedResponses, cosmosAsyncContainer);

            docsToRead = generatedDocs;
        } else {
            docsToRead = new ArrayList<>();
        }
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

    public void shutdown() {
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

    protected void onError(Throwable throwable) {
    }

    protected abstract Mono<T> performWorkload(long i);

    @SuppressWarnings("unchecked")
    public void run() throws Exception {

        long startTime = System.currentTimeMillis();
        int concurrency = workloadConfig.getConcurrency();

        Flux<Long> source;
        Duration maxDuration = workloadConfig.getMaxRunningTimeDuration();
        if (maxDuration != null) {
            // Time-based termination
            final long deadline = startTime + maxDuration.toMillis();
            source = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    if (System.currentTimeMillis() < deadline) {
                        sink.next(state.getAndIncrement());
                    } else {
                        sink.complete();
                    }
                    return state;
                });
        } else {
            // Count-based termination using Flux.generate to avoid long-to-int truncation
            long numberOfOps = workloadConfig.getNumberOfOperations();
            source = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long current = state.getAndIncrement();
                    if (current < numberOfOps) {
                        sink.next(current);
                    } else {
                        sink.complete();
                    }
                    return state;
                });
        }

        AtomicLong completedCount = new AtomicLong(0);

        source
            .flatMap(i -> {
                Mono<T> workload = performWorkload(i);
                Mono<T> delayed = sparsityMono(i);
                if (delayed != null) {
                    workload = delayed.then(workload);
                }
                return workload
                    .subscribeOn(benchmarkScheduler)
                    .doOnSuccess(v -> {
                        completedCount.incrementAndGet();
                        AsyncBenchmark.this.onSuccess();
                    })
                    .doOnError(e -> {
                        completedCount.incrementAndGet();
                        logger.error("Encountered failure {} on thread {}",
                            e.getMessage(), Thread.currentThread().getName(), e);
                        AsyncBenchmark.this.onError(e);
                    })
                    .onErrorResume(e -> Mono.empty());
            }, concurrency)
            .blockLast();

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            completedCount.get(), (int) ((endTime - startTime) / 1000));
    }

    protected Mono sparsityMono(long i) {
        Duration duration = workloadConfig.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            return Mono.delay(duration);
        }
        return null;
    }

}
