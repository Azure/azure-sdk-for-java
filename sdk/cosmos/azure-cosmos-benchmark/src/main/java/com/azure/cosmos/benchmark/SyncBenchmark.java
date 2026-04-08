// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
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
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

abstract class SyncBenchmark<T> implements Benchmark {

    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor clientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();

    private final ExecutorService executorService;

    private boolean databaseCreated;
    private boolean collectionCreated;

    static final Logger logger = LoggerFactory.getLogger(SyncBenchmark.class);
    final CosmosClient benchmarkWorkloadClient;
    CosmosContainer cosmosContainer;
    CosmosDatabase cosmosDatabase;

    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
    final List<PojoizedJson> docsToRead;
    final Semaphore concurrencyControlSemaphore;

    static abstract class ResultHandler<T, Throwable> implements BiFunction<T, Throwable, T> {
        ResultHandler() {
        }

        protected void init() {
        }

        @Override
        abstract public T apply(T o, Throwable throwable);
    }

    SyncBenchmark(TenantWorkloadConfig workloadCfg) throws Exception {
        executorService = Executors.newFixedThreadPool(workloadCfg.getConcurrency());
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

        clientBuilderAccessor
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

            concurrencyControlSemaphore = new Semaphore(workloadCfg.getConcurrency());

            ArrayList<CompletableFuture<PojoizedJson>> createDocumentFutureList = new ArrayList<>();

            if (workloadCfg.getOperationType() != Operation.WriteThroughput
                    && workloadCfg.getOperationType() != Operation.ReadMyWrites) {
                String dataFieldValue = RandomStringUtils.randomAlphabetic(workloadCfg.getDocumentDataFieldSize());
                for (int i = 0; i < workloadCfg.getNumberOfPreCreatedDocuments(); i++) {
                    String uuid = UUID.randomUUID().toString();
                    PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                            dataFieldValue,
                            partitionKey,
                            workloadCfg.getDocumentDataFieldCount());
                    CompletableFuture<PojoizedJson> futureResult = CompletableFuture.supplyAsync(() -> {

                        int maxRetries = 5;
                        Exception lastException = null;
                        for (int attempt = 0; attempt <= maxRetries; attempt++) {
                            try {
                                CosmosItemResponse<PojoizedJson> itemResponse = cosmosContainer.createItem(newDoc);
                                return toPojoizedJson(itemResponse);
                            } catch (CosmosException ce) {
                                lastException = ce;
                                if (ce.getStatusCode() == 409) {
                                    // conflict — document already exists, read it back
                                    try {
                                        return cosmosContainer.readItem(
                                            uuid, new PartitionKey(uuid), PojoizedJson.class).getItem();
                                    } catch (Exception readEx) {
                                        throw propagate(readEx);
                                    }
                                }
                                int statusCode = ce.getStatusCode();
                                boolean isTransient = statusCode == 408 || statusCode == 410
                                    || statusCode == 429 || statusCode == 449
                                    || statusCode == 500 || statusCode == 503;
                                if (isTransient && attempt < maxRetries) {
                                    try {
                                        Thread.sleep(1000L * (attempt + 1));
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw propagate(ce);
                                    }
                                    continue;
                                }
                                throw propagate(ce);
                            } catch (Exception e) {
                                lastException = e;
                                throw propagate(e);
                            }
                        }
                        throw new RuntimeException("Exhausted retries for createItem", lastException);

                    }, executorService);

                    createDocumentFutureList.add(futureResult);
                }
            }

            docsToRead = createDocumentFutureList.stream().map(future -> getOrThrow(future)).collect(Collectors.toList());
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
        executorService.shutdown();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract T performWorkload(long i) throws Exception;

    public void run() throws Exception {

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for ( i = 0; BenchmarkHelper.shouldContinue(startTime, i, workloadConfig); i++) {

            ResultHandler<T, Throwable> resultHandler = new ResultHandler<T, Throwable>() {
                @Override
                public T apply(T t, Throwable throwable) {
                    concurrencyControlSemaphore.release();
                    if (t != null) {
                        assert(throwable == null);
                        SyncBenchmark.this.onSuccess();
                        synchronized (count) {
                            count.incrementAndGet();
                            count.notify();
                        }
                    } else {
                        assert(throwable != null);

                        logger.error("Encountered failure {} on thread {}" ,
                                     throwable.getMessage(), Thread.currentThread().getName(), throwable);
                        concurrencyControlSemaphore.release();
                        SyncBenchmark.this.onError(throwable);

                        synchronized (count) {
                            count.incrementAndGet();
                            count.notify();
                        }
                    }

                    return t;
                }
            };

            concurrencyControlSemaphore.acquire();
            final long cnt = i;

            final ResultHandler<T, Throwable> finalResultHandler = resultHandler;

            CompletableFuture<T> futureResult = CompletableFuture.supplyAsync(() -> {
                try {
                    finalResultHandler.init();
                    return performWorkload(cnt);
                } catch (Exception e) {
                    throw propagate(e);
                }

            }, executorService);

            futureResult.handle(resultHandler);
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

    RuntimeException propagate(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

    <V> V getOrThrow(Future<V> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    PojoizedJson toPojoizedJson(CosmosItemResponse<PojoizedJson> resp) throws Exception {
        return resp.getItem();
    }
}
