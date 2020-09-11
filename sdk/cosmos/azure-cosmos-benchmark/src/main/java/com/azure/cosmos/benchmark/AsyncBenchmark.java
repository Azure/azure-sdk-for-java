// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

abstract class AsyncBenchmark<T> {
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private ScheduledReporter reporter;

    private volatile Meter successMeter;
    private volatile Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    final Logger logger;
    final CosmosAsyncClient cosmosClient;
    CosmosAsyncContainer cosmosAsyncContainer;
    CosmosAsyncDatabase cosmosAsyncDatabase;

    final String partitionKey;
    final Configuration configuration;
    final List<PojoizedJson> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    private static final String SUCCESS_COUNTER_METER_NAME = "#Successful Operations";
    private static final String FAILURE_COUNTER_METER_NAME = "#Unsuccessful Operations";
    private static final String LATENCY_METER_NAME = "latency";

    private AtomicBoolean warmupMode = new AtomicBoolean(false);

    AsyncBenchmark(Configuration cfg) {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(cfg.getServiceEndpoint())
            .key(cfg.getMasterKey())
            .consistencyLevel(cfg.getConsistencyLevel())
            .contentResponseOnWriteEnabled(Boolean.parseBoolean(cfg.isContentResponseOnWriteEnabled()));
        if (cfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(cfg.getMaxConnectionPoolSize());
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }
        cosmosClient = cosmosClientBuilder.buildAsyncClient();
        configuration = cfg;
        logger = LoggerFactory.getLogger(this.getClass());

        try {
            cosmosAsyncDatabase = cosmosClient.getDatabase(this.configuration.getDatabaseId());
            cosmosAsyncDatabase.read().block();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(cfg.getDatabaseId()).block();
                cosmosAsyncDatabase = cosmosClient.getDatabase(cfg.getDatabaseId());
                logger.info("Database {} is created for this test", this.configuration.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

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

        partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());

        ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

        if (configuration.getOperationType() != Configuration.Operation.WriteLatency
                && configuration.getOperationType() != Configuration.Operation.WriteThroughput
                && configuration.getOperationType() != Configuration.Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", cfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    configuration.getDocumentDataFieldCount());
                Flux<PojoizedJson> obs = cosmosAsyncContainer.createItem(newDoc).map(resp -> {
                    PojoizedJson x =
                        resp.getItem();
                    return x;
                }).flux();
                createDocumentObservables.add(obs);
            }
            logger.info("Finished pre-populating {} documents", cfg.getNumberOfPreCreatedDocuments());
        }

        docsToRead = Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block();
        init();

        if (configuration.isEnableJvmStats()) {
            metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        if (configuration.getGraphiteEndpoint() != null) {
            final Graphite graphite = new Graphite(new InetSocketAddress(
                    configuration.getGraphiteEndpoint(),
                    configuration.getGraphiteEndpointPort()));
            reporter = GraphiteReporter.forRegistry(metricsRegistry)
                .prefixedWith(configuration.getOperationType().name())
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        } else if (configuration.getReportingDirectory() != null) {
            reporter = CsvReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(configuration.getReportingDirectory());
        } else {
            reporter = ConsoleReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
        }

        MeterRegistry registry = configuration.getAzureMonitorMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }

        registry = configuration.getGraphiteMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }
    }

    protected void init() {
    }

    void shutdown() {
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

    protected void initializeMetersIfSkippedEnoughOperations(AtomicLong count) {
        if (configuration.getSkipWarmUpOperations() > 0) {
            if (count.get() >= configuration.getSkipWarmUpOperations()) {
                if (warmupMode.get()) {
                    synchronized (this) {
                        if (warmupMode.get()) {
                            logger.info("Warmup phase finished. Starting capturing perf numbers ....");
                            resetMeters();
                            initializeMeter();
                            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
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
        metricsRegistry.remove(SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            metricsRegistry.remove(LATENCY_METER_NAME);
        }
    }

    private void initializeMeter() {
        successMeter = metricsRegistry.meter(SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            latency = metricsRegistry.timer(LATENCY_METER_NAME);
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

    void run() throws Exception {
        initializeMeter();
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

    protected Mono sparsityMono(long i) {
        Duration duration = configuration.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            if (configuration.getSkipWarmUpOperations() > i) {
                // don't wait on the initial warm up time.
                return null;
            }

            return Mono.delay(duration);
        }
        else return null;
    }
}