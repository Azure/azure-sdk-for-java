// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.ctl;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.BenchmarkBase;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.BenchmarkRequestSubscriber;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncCtlWorkload extends BenchmarkBase {
    private final String PERCENT_PARSING_ERROR = "Unable to parse user provided readWriteQueryPct ";
    private final String prefixUuidForCreate;
    private final String dataFieldValue;
    private final String partitionKey;
    private final Logger logger;
    private final Map<String, List<PojoizedJson>> docsToRead = new HashMap<>();
    private final Random random;

    private Timer readLatency;
    private Timer writeLatency;
    private Timer queryLatency;

    private Meter readSuccessMeter;
    private Meter readFailureMeter;
    private Meter writeSuccessMeter;
    private Meter writeFailureMeter;
    private Meter querySuccessMeter;
    private Meter queryFailureMeter;

    private List<CosmosAsyncContainer> containers = new ArrayList<>();
    private List<String> containerToClearAfterTest = new ArrayList<>();
    private int readPct;
    private int writePct;
    private int queryPct;

    private static final String READ_SUCCESS_COUNTER_METER_NAME = "#Read Successful Operations";
    private static final String READ_FAILURE_COUNTER_METER_NAME = "#Read Unsuccessful Operations";
    private static final String READ_LATENCY_METER_NAME = "Read Latency";
    private static final String WRITE_SUCCESS_COUNTER_METER_NAME = "#Write Successful Operations";
    private static final String WRITE_FAILURE_COUNTER_METER_NAME = "#Write Unsuccessful Operations";
    private static final String WRITE_LATENCY_METER_NAME = "Write Latency";
    private static final String QUERY_SUCCESS_COUNTER_METER_NAME = "#Query Successful Operations";
    private static final String QUERY_FAILURE_COUNTER_METER_NAME = "#Query Unsuccessful Operations";
    private static final String QUERY_LATENCY_METER_NAME = "Query Latency";

    public AsyncCtlWorkload(Configuration cfg) {
        super(cfg);

        logger = LoggerFactory.getLogger(this.getClass());

        parsedReadWriteQueryPct(configuration.getReadWriteQueryPct());

        this.createContainers();

        partitionKey = containers.get(0).read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
        createPrePopulatedDocs(configuration.getNumberOfPreCreatedDocuments());

        prefixUuidForCreate = UUID.randomUUID().toString();
        random = new Random();
    }

    @Override
    public void shutdown() {
        if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", this.configuration.getDatabaseId());
        } else if (containerToClearAfterTest.size() > 0) {
            for (String id : containerToClearAfterTest) {
                cosmosAsyncDatabase.getContainer(id).delete().block();
                logger.info("Deleted temporary collection {} created for this test", id);
            }
        }
        cosmosClient.close();
    }

    private void performWorkload(BaseSubscriber<Object> documentSubscriber, OperationType type, long i) throws Exception {
        Flux<? extends Object> obs;
        Mono sparsitySleepMono = sparsityMono(i);
        CosmosAsyncContainer container = containers.get((int) i % containers.size());
        if (type.equals(OperationType.Create)) {
            PojoizedJson data = BenchmarkHelper.generateDocument(prefixUuidForCreate + i,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount());
            obs = sparsitySleepMono.flux().flatMap(
                null,
                null,
                () -> container.createItem(data));
        } else if (type.equals(OperationType.Query)) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = sparsitySleepMono.flux().flatMap(
                null,
                null,
                () -> container.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10));
        } else {
            int index = random.nextInt(docsToRead.get(container.getId()).size());
            RequestOptions options = new RequestOptions();
            String partitionKeyValue = docsToRead.get(container.getId()).get(index).getId();
            options.setPartitionKey(new PartitionKey(partitionKeyValue));

            obs = sparsitySleepMono.flux().flatMap(
                null,
                null,
                () -> container.readItem(docsToRead.get(container.getId()).get(index).getId(),
                    new PartitionKey(partitionKeyValue),
                    PojoizedJson.class));
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(documentSubscriber);
    }

    @Override
    public void run() throws Exception {

        this.initializeMeters();
        if (configuration.getSkipWarmUpOperations() > 0) {
            logger.info("Starting warm up phase. Executing {} operations to warm up ...", configuration.getSkipWarmUpOperations());
            warmupMode.set(true);
        } else {
            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        }

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;
        int writeRange = readPct + writePct;
        for (i = 0; BenchmarkHelper.shouldContinue(startTime, i, configuration); i++) {
            int index = (int) i % 100;
            if (index < readPct) {
                BenchmarkRequestSubscriber<Object> readSubscriber = new BenchmarkRequestSubscriber<>(readSuccessMeter,
                    readFailureMeter,
                    concurrencyControlSemaphore,
                    count,
                    configuration.getDiagnosticsThresholdDuration(),
                    (operationCounts) -> this.initializeMetersIfSkippedEnoughOperations(operationCounts));
                readSubscriber.context = readLatency.time();
                performWorkload(readSubscriber, OperationType.Read, i);
            } else if (index < writeRange) {
                BenchmarkRequestSubscriber<Object> writeSubscriber = new BenchmarkRequestSubscriber<>(writeSuccessMeter,
                    writeFailureMeter,
                    concurrencyControlSemaphore,
                    count,
                    configuration.getDiagnosticsThresholdDuration(),
                    (operationCounts) -> this.initializeMetersIfSkippedEnoughOperations(operationCounts));
                writeSubscriber.context = writeLatency.time();
                performWorkload(writeSubscriber, OperationType.Create, i);

            } else {
                BenchmarkRequestSubscriber<Object> querySubscriber = new BenchmarkRequestSubscriber<>(querySuccessMeter,
                    queryFailureMeter,
                    concurrencyControlSemaphore,
                    count,
                    configuration.getDiagnosticsThresholdDuration(),
                    (operationCounts) -> this.initializeMetersIfSkippedEnoughOperations(operationCounts));
                querySubscriber.context = queryLatency.time();
                performWorkload(querySubscriber, OperationType.Query, i);
            }
        }

        synchronized (count) {
            while (count.get() < i) {
                count.wait();
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            configuration.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();
    }

    private void parsedReadWriteQueryPct(String readWriteQueryPct) {
        String[] readWriteQueryPctList = readWriteQueryPct.split(",");
        if (readWriteQueryPctList.length == 3) {
            try {
                if (Integer.valueOf(readWriteQueryPctList[0]) + Integer.valueOf(readWriteQueryPctList[1]) + Integer.valueOf(readWriteQueryPctList[2]) == 100) {
                    readPct = Integer.valueOf(readWriteQueryPctList[0]);
                    writePct = Integer.valueOf(readWriteQueryPctList[1]);
                    queryPct = Integer.valueOf(readWriteQueryPctList[2]);
                } else {
                    throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryPct);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryPct);
            }
        } else {
            throw new IllegalArgumentException(PERCENT_PARSING_ERROR + readWriteQueryPct);
        }
    }

    private void createPrePopulatedDocs(int numberOfPreCreatedDocuments) {
        logger.info("PRE-populating {} documents ....", configuration.getNumberOfPreCreatedDocuments());

        for (CosmosAsyncContainer container : containers) {
            AtomicLong successCount = new AtomicLong(0);
            AtomicLong failureCount = new AtomicLong(0);
            ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();
            for (int i = 0; i < numberOfPreCreatedDocuments; i++) {
                String uId = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uId,
                    dataFieldValue,
                    partitionKey,
                    configuration.getDocumentDataFieldCount());

                Flux<PojoizedJson> obs = container.createItem(newDoc).map(resp -> {
                    PojoizedJson x =
                        resp.getItem();
                    return x;
                }).onErrorResume(throwable -> {
                    failureCount.incrementAndGet();
                    logger.error("Error during pre populating item ", throwable.getMessage());
                    return Mono.empty();
                }).doOnSuccess(pojoizedJson -> {
                    successCount.incrementAndGet();
                }).flux();
                createDocumentObservables.add(obs);
            }
            docsToRead.put(container.getId(),
                Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block());
            logger.info("Finished pre-populating {} documents for container {}",
                successCount.get() - failureCount.get(), container.getId());
            if (failureCount.get() > 0) {
                logger.info("Failed pre-populating {} documents for container {}",
                    failureCount.get(), container.getId());
            }
        }
    }

    private void createContainers() {
        int numberOfCollection = this.configuration.getNumberOfCollectionForCtl();
        if (numberOfCollection < 1) {
            numberOfCollection = 1;
        }

        for (int i = 1; i <= numberOfCollection; i++) {
            try {
                CosmosAsyncContainer cosmosAsyncContainer =
                    cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId() + "_" + i);

                cosmosAsyncContainer.read().block();
                containers.add(cosmosAsyncContainer);

            } catch (CosmosException e) {
                if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    cosmosAsyncDatabase.createContainer(
                        this.configuration.getCollectionId() + "_" + i,
                        Configuration.DEFAULT_PARTITION_KEY_PATH
                    ).block();

                    CosmosAsyncContainer cosmosAsyncContainer =
                        cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId() + "_" + i);
                    logger.info("Collection {} is created for this test",
                        this.configuration.getCollectionId() + "_" + i);
                    containers.add(cosmosAsyncContainer);
                    containerToClearAfterTest.add(cosmosAsyncContainer.getId());
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    protected void initializeMeters() {
        readSuccessMeter = metricsRegistry.meter(READ_SUCCESS_COUNTER_METER_NAME);
        readFailureMeter = metricsRegistry.meter(READ_FAILURE_COUNTER_METER_NAME);
        readLatency = metricsRegistry.register(READ_LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));

        writeSuccessMeter = metricsRegistry.meter(WRITE_SUCCESS_COUNTER_METER_NAME);
        writeFailureMeter = metricsRegistry.meter(WRITE_FAILURE_COUNTER_METER_NAME);
        writeLatency = metricsRegistry.register(WRITE_LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));

        querySuccessMeter = metricsRegistry.meter(QUERY_SUCCESS_COUNTER_METER_NAME);
        queryFailureMeter = metricsRegistry.meter(QUERY_FAILURE_COUNTER_METER_NAME);
        queryLatency = metricsRegistry.register(QUERY_LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));
    }

    @Override
    protected void resetMeters() {
        metricsRegistry.remove(READ_SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(READ_FAILURE_COUNTER_METER_NAME);
        metricsRegistry.remove(READ_LATENCY_METER_NAME);

        metricsRegistry.remove(WRITE_SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(WRITE_FAILURE_COUNTER_METER_NAME);
        metricsRegistry.remove(WRITE_LATENCY_METER_NAME);

        metricsRegistry.remove(QUERY_SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(QUERY_FAILURE_COUNTER_METER_NAME);
        metricsRegistry.remove(QUERY_LATENCY_METER_NAME);
    }
}
