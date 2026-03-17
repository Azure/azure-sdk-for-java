// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.ctl;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.benchmark.Benchmark;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;


public class AsyncCtlWorkload implements Benchmark {

    // Dedicated scheduler for CTL benchmark workload dispatch.
    // Owned and disposed by the orchestrator (or test harness) that creates the benchmark.
    private final Scheduler benchmarkScheduler;

    private final String PERCENT_PARSING_ERROR = "Unable to parse user provided readWriteQueryReadManyPct ";
    private final String prefixUuidForCreate;
    private final String dataFieldValue;
    private final String partitionKey;
    private final Logger logger;
    private final CosmosAsyncClient cosmosClient;
    private final TenantWorkloadConfig workloadConfig;
    private final Map<String, List<PojoizedJson>> docsToRead = new HashMap<>();
    private final Map<String, List<CosmosItemIdentity>> itemIdentityMap = new HashMap<>();
    private final Random random;

    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private List<CosmosAsyncContainer> containers = new ArrayList<>();
    private List<String> containerToClearAfterTest = new ArrayList<>();
    private boolean databaseCreated;
    private int readPct;
    private int writePct;
    private int queryPct;
    private int readManyPct;

    public AsyncCtlWorkload(TenantWorkloadConfig workloadCfg, Scheduler scheduler) {
        this.benchmarkScheduler = scheduler;
        logger = LoggerFactory.getLogger(this.getClass());

        final TokenCredential credential = workloadCfg.isManagedIdentityRequired()
            ? workloadCfg.buildTokenCredential()
            : null;

        CosmosClientBuilder cosmosClientBuilder = workloadCfg.isManagedIdentityRequired() ?
                new CosmosClientBuilder().credential(credential) :
                new CosmosClientBuilder().key(workloadCfg.getMasterKey());

        cosmosClientBuilder
                .preferredRegions(workloadCfg.getPreferredRegionsList())
                .endpoint(workloadCfg.getServiceEndpoint())
                .consistencyLevel(workloadCfg.getConsistencyLevel())
                .contentResponseOnWriteEnabled(workloadCfg.isContentResponseOnWriteEnabled());

        if (workloadCfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(workloadCfg.getMaxConnectionPoolSize());
            if (workloadCfg.isHttp2Enabled()) {
                Http2ConnectionConfig http2Config = gatewayConnectionConfig.getHttp2ConnectionConfig();
                http2Config.setEnabled(true);
                if (workloadCfg.getHttp2MaxConcurrentStreams() != null) {
                    http2Config.setMaxConcurrentStreams(workloadCfg.getHttp2MaxConcurrentStreams());
                }
            }
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }
        cosmosClient = cosmosClientBuilder.buildAsyncClient();
        workloadConfig = workloadCfg;

        parsedReadWriteQueryReadManyPct(workloadConfig.getReadWriteQueryReadManyPct());

        createDatabaseAndContainers(workloadCfg);

        partitionKey = containers.get(0).read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        logger.info("PRE-populating {} documents ....", workloadCfg.getNumberOfPreCreatedDocuments());
        dataFieldValue = RandomStringUtils.randomAlphabetic(workloadConfig.getDocumentDataFieldSize());
        createPrePopulatedDocs(workloadConfig.getNumberOfPreCreatedDocuments());
        createItemIdentityMap(docsToRead);

        prefixUuidForCreate = UUID.randomUUID().toString();
        random = new Random();
    }

    public void shutdown() {
        if (workloadConfig.isSuppressCleanup()) {
            logger.info("Skipping cleanup of database/container (suppressCleanup=true)");
        } else if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", this.workloadConfig.getDatabaseId());
        } else if (containerToClearAfterTest.size() > 0) {
            for (String id : containerToClearAfterTest) {
                cosmosAsyncDatabase.getContainer(id).delete().block();
                logger.info("Deleted temporary collection {} created for this test", id);
            }
        }
        cosmosClient.close();
    }

    private Mono<Object> performWorkload(OperationType type, long i, boolean isReadMany) {
        CosmosAsyncContainer container = containers.get((int) i % containers.size());
        if (type.equals(OperationType.Create)) {
            PojoizedJson data = BenchmarkHelper.generateDocument(prefixUuidForCreate + i,
                dataFieldValue,
                partitionKey,
                workloadConfig.getDocumentDataFieldCount());
            return container.createItem(data).map(r -> (Object) r);
        } else if (type.equals(OperationType.Query) && !isReadMany) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            String sqlQuery = "Select top 100 * from c order by c._ts";
            return container.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10).last().map(r -> (Object) r);
        } else if (type.equals(OperationType.Read)){
            int index = random.nextInt(docsToRead.get(container.getId()).size());
            String partitionKeyValue = docsToRead.get(container.getId()).get(index).getId();
            return container.readItem(docsToRead.get(container.getId()).get(index).getId(),
                new PartitionKey(partitionKeyValue),
                PojoizedJson.class)
                .map(r -> (Object) r);
        } else {
            List<CosmosItemIdentity> itemIdentityList = itemIdentityMap.get(container.getId());
            return container.readMany(itemIdentityList,
                PojoizedJson.class).map(r -> (Object) r);
        }
    }

    private Mono<Object> selectAndPerformWorkload(long i) {
        int index = (int) i % 100;
        int writeRange = readPct + writePct;
        int queryRange = readPct + writePct + queryPct;

        if (index < readPct) {
            return performWorkload(OperationType.Read, i, false);
        } else if (index < writeRange) {
            return performWorkload(OperationType.Create, i, false);
        } else if (index < queryRange) {
            return performWorkload(OperationType.Query, i, false);
        } else {
            return performWorkload(OperationType.Query, i, true);
        }
    }

    public void run() throws Exception {

        long startTime = System.currentTimeMillis();
        int concurrency = workloadConfig.getConcurrency();

        Flux<Long> source;
        Duration maxDuration = workloadConfig.getMaxRunningTimeDuration();
        if (maxDuration != null) {
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
            .flatMap(
                i -> selectAndPerformWorkload(i)
                    .subscribeOn(benchmarkScheduler)
                    .doOnSuccess(v -> completedCount.incrementAndGet())
                    .doOnError(e -> {
                        completedCount.incrementAndGet();
                        logger.error("Encountered failure {} on thread {}",
                            e.getMessage(), Thread.currentThread().getName(), e);
                    })
                    .onErrorResume(e -> Mono.empty()),
                concurrency)
            .blockLast();

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            completedCount.get(), (int) ((endTime - startTime) / 1000));
    }

    private void parsedReadWriteQueryReadManyPct(String readWriteQueryReadManyPct) {
        String[] readWriteQueryReadManyPctList = readWriteQueryReadManyPct.split(",");
        if (readWriteQueryReadManyPctList.length == 4) {
            try {
                if (Integer.valueOf(readWriteQueryReadManyPctList[0]) + Integer.valueOf(readWriteQueryReadManyPctList[1]) + Integer.valueOf(readWriteQueryReadManyPctList[2]) + Integer.valueOf(readWriteQueryReadManyPctList[3]) == 100) {
                    readPct = Integer.valueOf(readWriteQueryReadManyPctList[0]);
                    writePct = Integer.valueOf(readWriteQueryReadManyPctList[1]);
                    queryPct = Integer.valueOf(readWriteQueryReadManyPctList[2]);
                    readManyPct = Integer.valueOf(readWriteQueryReadManyPctList[3]);
                } else {
                    throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryReadManyPct);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryReadManyPct);
            }
        } else {
            throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryReadManyPct);
        }
    }

    private void createPrePopulatedDocs(int numberOfPreCreatedDocuments) {
        for (CosmosAsyncContainer container : containers) {
            List<PojoizedJson> generatedDocs = new ArrayList<>();

            Flux<CosmosItemOperation> bulkOperationFlux = Flux.range(0, numberOfPreCreatedDocuments)
                .map(i -> {
                    String uId = UUID.randomUUID().toString();
                    PojoizedJson newDoc = BenchmarkHelper.generateDocument(uId,
                        dataFieldValue,
                        partitionKey,
                        workloadConfig.getDocumentDataFieldCount());
                    generatedDocs.add(newDoc);
                    return CosmosBulkOperations.getCreateItemOperation(newDoc, new PartitionKey(uId));
                });

            AtomicLong successCount = new AtomicLong(0);
            AtomicLong failureCount = new AtomicLong(0);
            List<CosmosBulkOperationResponse<Object>> failedResponses = Collections.synchronizedList(new ArrayList<>());
            CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
            container.executeBulkOperations(bulkOperationFlux, bulkExecutionOptions)
                .doOnNext(response -> {
                    if (response.getResponse() != null && response.getResponse().isSuccessStatusCode()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                        failedResponses.add(response);
                        logger.debug("Error during pre populating item {}",
                            response.getException() != null ? response.getException().getMessage() : "unknown error");
                    }
                })
                .blockLast(Duration.ofMinutes(10));

            if (failureCount.get() > 0) {
                logger.warn("Bulk pre-population encountered {} failures out of {} items for container {}",
                    failureCount.get(), numberOfPreCreatedDocuments, container.getId());
            }

            BenchmarkHelper.retryFailedBulkOperations(failedResponses, container);

            docsToRead.put(container.getId(), generatedDocs);
            logger.info("Finished pre-populating {} documents for container {}",
                successCount.get(), container.getId());
        }
    }

    private void createItemIdentityMap(Map<String, List<PojoizedJson>> docsToRead) {
        docsToRead.entrySet().stream()
            .forEach(doc -> doc.getValue()
                .forEach(pojoizedJson -> itemIdentityMap
                    .computeIfAbsent(doc.getKey(), d -> new ArrayList<>())
                        .add(new CosmosItemIdentity(new PartitionKey(pojoizedJson.getId()), pojoizedJson.getId()))));
    }

    private void createDatabaseAndContainers(TenantWorkloadConfig cfg) {
        try {
            cosmosAsyncDatabase = cosmosClient.getDatabase(cfg.getDatabaseId());
            cosmosAsyncDatabase.read().block();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(cfg.getDatabaseId(), ThroughputProperties.createManualThroughput(cfg.getThroughput())).block();
                cosmosAsyncDatabase = cosmosClient.getDatabase(cfg.getDatabaseId());
                logger.info("Database {} is created for this test", cfg.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        int numberOfCollection = workloadConfig.getNumberOfCollectionForCtl();
        if (numberOfCollection < 1) {
            numberOfCollection = 1;
        }

        for (int i = 1; i <= numberOfCollection; i++) {
            try {
                CosmosAsyncContainer cosmosAsyncContainer =
                    cosmosAsyncDatabase.getContainer(cfg.getContainerId() + "_" + i);

                cosmosAsyncContainer.read().block();
                containers.add(cosmosAsyncContainer);

            } catch (CosmosException e) {
                if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    cosmosAsyncDatabase.createContainer(
                        cfg.getContainerId() + "_" + i,
                        TenantWorkloadConfig.DEFAULT_PARTITION_KEY_PATH
                    ).block();

                    CosmosAsyncContainer cosmosAsyncContainer =
                        cosmosAsyncDatabase.getContainer(cfg.getContainerId() + "_" + i);
                    logger.info("Collection {} is created for this test",
                        cfg.getContainerId() + "_" + i);
                    containers.add(cosmosAsyncContainer);
                    containerToClearAfterTest.add(cosmosAsyncContainer.getId());
                } else {
                    throw e;
                }
            }
        }
    }
}
