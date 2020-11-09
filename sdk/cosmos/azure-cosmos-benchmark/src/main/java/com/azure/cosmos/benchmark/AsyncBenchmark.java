// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

abstract class AsyncBenchmark<T> extends BenchmarkBase {

    private volatile Meter successMeter;
    private volatile Meter failureMeter;

    final Logger logger;
    CosmosAsyncContainer cosmosAsyncContainer;
    private boolean collectionCreated;

    final String partitionKey;
    List<PojoizedJson> docsToRead;
    Timer latency;

    private static final String SUCCESS_COUNTER_METER_NAME = "#Successful Operations";
    private static final String FAILURE_COUNTER_METER_NAME = "#Unsuccessful Operations";
    private static final String LATENCY_METER_NAME = "latency";

    AsyncBenchmark(Configuration cfg) {
        super(cfg);

        this.logger = LoggerFactory.getLogger(this.getClass());
        this.createContainer();
        this.partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];
        this.createPrePopulatedDocs();

        init();
    }

    private void createContainer() {
        try {
            cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId());
            cosmosAsyncContainer.read().block();

        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosAsyncDatabase.createContainer(
                    this.configuration.getCollectionId(),
                    Configuration.DEFAULT_PARTITION_KEY_PATH,
                    ThroughputProperties.createManualThroughput(this.configuration.getThroughput())
                ).block();

                cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId());
                logger.info("Collection {} is created for this test", this.configuration.getCollectionId());
                collectionCreated = true;
            } else {
                throw e;
            }
        }
    }

    private void createPrePopulatedDocs() {
        ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

        if (this.configuration.getOperationType() != Configuration.Operation.WriteLatency
            && this.configuration.getOperationType() != Configuration.Operation.WriteThroughput
            && this.configuration.getOperationType() != Configuration.Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", this.configuration.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(this.configuration.getDocumentDataFieldSize());
            for (int i = 0; i < this.configuration.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    configuration.getDocumentDataFieldCount());
                Flux<PojoizedJson> obs = cosmosAsyncContainer
                    .createItem(newDoc)
                    .retryWhen(Retry.max(5).filter((error) -> {
                        if (!(error instanceof CosmosException)) {
                            return false;
                        }
                        final CosmosException cosmosException = (CosmosException)error;
                        if (cosmosException.getStatusCode() == 410 ||
                            cosmosException.getStatusCode() == 408 ||
                            cosmosException.getStatusCode() == 429 ||
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
                            final CosmosException cosmosException = (CosmosException)error;
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
        logger.info("Finished pre-populating {} documents", this.configuration.getNumberOfPreCreatedDocuments());
    }

    protected void init() {
    }

    @Override
    protected void shutdown() {
        if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", this.configuration.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosAsyncContainer.delete().block();
            logger.info("Deleted temporary collection {} created for this test", this.configuration.getCollectionId());
        }

        cosmosClient.close();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract void performWorkload(BaseSubscriber<T> baseSubscriber, long i) throws Exception;

    @Override
    protected void resetMeters() {
        metricsRegistry.remove(SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            metricsRegistry.remove(LATENCY_METER_NAME);
        }
    }

    @Override
    protected void initializeMeters() {
        successMeter = metricsRegistry.meter(SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            latency = metricsRegistry.register(LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        }
    }

    private boolean latencyAwareOperations(Configuration.Operation operation) {
        switch (configuration.getOperationType()) {
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
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void run() throws Exception {
        initializeMeters();
        if (configuration.getSkipWarmUpOperations() > 0) {
            logger.info("Starting warm up phase. Executing {} operations to warm up ...", configuration.getSkipWarmUpOperations());
            warmupMode.set(true);
        } else {
            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        }

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for ( i = 0; BenchmarkHelper.shouldContinue(startTime, i, configuration); i++) {

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
            configuration.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();
    }
}
