// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

abstract class SyncBenchmark<T> implements Benchmark {

    private static ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor clientBuilderAccessor() {
        return ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    }

    private static ImplementationBridgeHelpers.CosmosClientHelper.CosmosClientAccessor clientAccessor() {
        return ImplementationBridgeHelpers.CosmosClientHelper.getCosmosClientAccessor();
    }

    private final AtomicLong operationCounter = new AtomicLong(0);

    private boolean databaseCreated;
    private boolean collectionCreated;

    static final Logger logger = LoggerFactory.getLogger(SyncBenchmark.class);
    final CosmosClient benchmarkWorkloadClient;
    CosmosContainer cosmosContainer;
    CosmosDatabase cosmosDatabase;

    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
    final List<PojoizedJson> docsToRead;

    SyncBenchmark(TenantWorkloadConfig workloadCfg) throws Exception {
        workloadConfig = workloadCfg;

        boolean isManagedIdentityRequired = workloadCfg.isManagedIdentityRequired();

        final TokenCredential credential = isManagedIdentityRequired
            ? workloadCfg.buildTokenCredential()
            : null;

        CosmosClientBuilder benchmarkSpecificClientBuilder = isManagedIdentityRequired ?
                new CosmosClientBuilder()
                        .credential(credential) :
                new CosmosClientBuilder()
                        .key(workloadCfg.getMasterKey());

        benchmarkSpecificClientBuilder.preferredRegions(workloadCfg.getPreferredRegionsList())
                .endpoint(workloadCfg.getServiceEndpoint())
                .userAgentSuffix(workloadCfg.getApplicationName())
                .consistencyLevel(workloadCfg.getConsistencyLevel())
                .contentResponseOnWriteEnabled(workloadCfg.isContentResponseOnWriteEnabled());

        clientBuilderAccessor()
            .setRegionScopedSessionCapturingEnabled(benchmarkSpecificClientBuilder, workloadCfg.isRegionScopedSessionContainerEnabled());

        if (workloadCfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(workloadCfg.getMaxConnectionPoolSize());
            benchmarkSpecificClientBuilder = benchmarkSpecificClientBuilder.gatewayMode(gatewayConnectionConfig);
        }

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(
                new CosmosDiagnosticsThresholds()
                    .setPointOperationLatencyThreshold(workloadCfg.getPointOperationThreshold())
                    .setNonPointOperationLatencyThreshold(workloadCfg.getNonPointOperationThreshold())
            );

        if (workloadCfg.isDefaultLog4jLoggerEnabled()) {
            telemetryConfig.diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER);
        }

        benchmarkWorkloadClient = benchmarkSpecificClientBuilder.buildClient();

            try {
                cosmosDatabase = benchmarkWorkloadClient.getDatabase(workloadCfg.getDatabaseId());
                cosmosDatabase.read();
                logger.info("Database {} is created for this test", workloadCfg.getDatabaseId());
            } catch (CosmosException e) {
                if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {

                    if (isManagedIdentityRequired) {
                        throw new IllegalStateException("If managed identity is required, " +
                                "either pre-create a database and a container or use the management SDK.");
                    }

                    benchmarkWorkloadClient.createDatabase(workloadCfg.getDatabaseId());
                    cosmosDatabase = benchmarkWorkloadClient.getDatabase(workloadCfg.getDatabaseId());
                    databaseCreated = true;
                } else {
                    throw e;
                }
            }

            try {
                cosmosContainer = cosmosDatabase.getContainer(workloadCfg.getContainerId());
                cosmosContainer.read();
            } catch (CosmosException e) {
                if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {

                    if (isManagedIdentityRequired) {
                        throw new IllegalStateException("If managed identity is required, " +
                                "either pre-create a database and a container or use the management SDK.");
                    }

                    cosmosDatabase.createContainer(workloadCfg.getContainerId(),
                            TenantWorkloadConfig.DEFAULT_PARTITION_KEY_PATH,
                            ThroughputProperties.createManualThroughput(workloadCfg.getThroughput()));
                    cosmosContainer = cosmosDatabase.getContainer(workloadCfg.getContainerId());
                    logger.info("Collection {} is created for this test", workloadCfg.getContainerId());

                    // add some delay to allow container to be created across multiple regions
                    // container creation across regions is an async operation
                    // without the delay a container may not be available to process reads / writes
                    try {
                        Thread.sleep(30_000);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }

                    collectionCreated = true;
                } else {
                    throw e;
                }
            }

            partitionKey = cosmosContainer.read().getProperties().getPartitionKeyDefinition()
                    .getPaths().iterator().next().split("/")[1];

            if (workloadCfg.getOperationType() != Operation.WriteThroughput
                    && workloadCfg.getOperationType() != Operation.ReadMyWrites) {
                logger.info("PRE-populating {} documents ....", workloadCfg.getNumberOfPreCreatedDocuments());
                String dataFieldValue = RandomStringUtils.randomAlphabetic(workloadCfg.getDocumentDataFieldSize());
                List<PojoizedJson> generatedDocs = new ArrayList<>();

                // Use internal async container for bulk ingestion
                CosmosAsyncContainer asyncContainer = clientAccessor()
                    .getCosmosAsyncClient(benchmarkWorkloadClient)
                    .getDatabase(workloadCfg.getDatabaseId())
                    .getContainer(workloadCfg.getContainerId());

                Flux<CosmosItemOperation> bulkOperationFlux = Flux.range(0, workloadCfg.getNumberOfPreCreatedDocuments())
                    .map(i -> {
                        String uuid = UUID.randomUUID().toString();
                        PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                            dataFieldValue,
                            partitionKey,
                            workloadCfg.getDocumentDataFieldCount());
                        generatedDocs.add(newDoc);
                        return CosmosBulkOperations.getCreateItemOperation(newDoc, new PartitionKey(uuid));
                    });

                CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
                bulkExecutionOptions.setExcludedRegions(workloadCfg.getExcludedRegionsList());
                List<CosmosBulkOperationResponse<Object>> failedResponses = Collections.synchronizedList(new ArrayList<>());
                asyncContainer
                    .executeBulkOperations(bulkOperationFlux, bulkExecutionOptions)
                    .doOnNext(response -> {
                        if (response.getResponse() == null || !response.getResponse().isSuccessStatusCode()) {
                            failedResponses.add(response);
                        }
                    })
                    .blockLast(Duration.ofMinutes(10));

                BenchmarkHelper.retryFailedBulkOperations(failedResponses, asyncContainer,
                    workloadCfg.getIngestionRetryConcurrency());

                docsToRead = generatedDocs;
            } else {
                docsToRead = new ArrayList<>();
            }
            logger.info("Finished pre-populating {} documents", workloadCfg.getNumberOfPreCreatedDocuments());
            init();
    }

    protected void init() {
    }

    public void shutdown() {
        if (workloadConfig.isSuppressCleanup()) {
            logger.info("Skipping cleanup of database/container (suppressCleanup=true)");
        } else if (this.databaseCreated) {
            cosmosDatabase.delete();
            logger.info("Deleted temporary database {} created for this test", workloadConfig.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosContainer.delete();
            logger.info("Deleted temporary collection {} created for this test", workloadConfig.getContainerId());
        }

        benchmarkWorkloadClient.close();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract T performWorkload(long i) throws Exception;

    @Override
    public Mono<?> performSingleOperation() {
        long operationIndex = operationCounter.getAndIncrement();
        return Mono.fromCallable(() -> performWorkload(operationIndex))
            .doOnSuccess(v -> SyncBenchmark.this.onSuccess())
            .doOnError(e -> {
                logger.error("Encountered failure {} on thread {}",
                    e.getMessage(), Thread.currentThread().getName(), e);
                SyncBenchmark.this.onError(e);
            })
            .onErrorResume(e -> Mono.empty());
    }
}
