// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.ConsoleReporter;
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
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
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

abstract class SyncBenchmark<T> {
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final ScheduledReporter reporter;
    private final ExecutorService executorService;

    private Meter successMeter;
    private Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    final Logger logger;
    final CosmosClient cosmosClient;
    CosmosContainer cosmosContainer;
    CosmosDatabase cosmosDatabase;

    final String partitionKey;
    final Configuration configuration;
    final List<PojoizedJson> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    static abstract class ResultHandler<T, Throwable> implements BiFunction<T, Throwable, T> {
        ResultHandler() {
        }

        protected void init() {
        }

        @Override
        abstract public T apply(T o, Throwable throwable);
    }

    static class LatencyListener<T> extends ResultHandler<T, Throwable> {
        private final ResultHandler<T, Throwable> baseFunction;
        private final Timer latencyTimer;
        Timer.Context context;
        LatencyListener(ResultHandler<T, Throwable> baseFunction, Timer latencyTimer) {
            this.baseFunction = baseFunction;
            this.latencyTimer = latencyTimer;
        }

        protected void init() {
            super.init();
            context = latencyTimer.time();
        }

        @Override
        public T apply(T o, Throwable throwable) {
            context.stop();
            return baseFunction.apply(o, throwable);
        }
    }

    SyncBenchmark(Configuration cfg) throws Exception {
        executorService = Executors.newFixedThreadPool(cfg.getConcurrency());
        configuration = cfg;
        logger = LoggerFactory.getLogger(this.getClass());

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(cfg.getServiceEndpoint())
            .preferredRegions(cfg.getPreferredRegionsList())
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
        cosmosClient = cosmosClientBuilder.buildClient();
        try {
            cosmosDatabase = cosmosClient.getDatabase(this.configuration.getDatabaseId());
            cosmosDatabase.read();
            logger.info("Database {} is created for this test", this.configuration.getDatabaseId());
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(cfg.getDatabaseId());
                cosmosDatabase = cosmosClient.getDatabase(cfg.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        try {
            cosmosContainer = cosmosDatabase.getContainer(this.configuration.getCollectionId());
            cosmosContainer.read();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosDatabase.createContainer(this.configuration.getCollectionId(),
                    Configuration.DEFAULT_PARTITION_KEY_PATH,
                    ThroughputProperties.createManualThroughput(this.configuration.getThroughput()));
                cosmosContainer = cosmosDatabase.getContainer(this.configuration.getCollectionId());
                logger.info("Collection {} is created for this test", this.configuration.getCollectionId());
                collectionCreated = true;
            } else {
                throw e;
            }
        }

        partitionKey = cosmosContainer.read().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());

        ArrayList<CompletableFuture<PojoizedJson>> createDocumentFutureList = new ArrayList<>();

        if (configuration.getOperationType() != Configuration.Operation.WriteLatency
                && configuration.getOperationType() != Configuration.Operation.WriteThroughput
                && configuration.getOperationType() != Configuration.Operation.ReadMyWrites) {
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    configuration.getDocumentDataFieldCount());
                CompletableFuture<PojoizedJson> futureResult = CompletableFuture.supplyAsync(() -> {

                    try {
                        CosmosItemResponse<PojoizedJson> itemResponse = cosmosContainer.createItem(newDoc);
                        return toPojoizedJson(itemResponse);

                    } catch (Exception e) {
                        throw propagate(e);
                    }

                }, executorService);

                createDocumentFutureList.add(futureResult);
            }
        }

        docsToRead = createDocumentFutureList.stream().map(future -> getOrThrow(future)).collect(Collectors.toList());
        init();

        if (configuration.isEnableJvmStats()) {
            metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        if (configuration.getGraphiteEndpoint() != null) {
            final Graphite graphite = new Graphite(new InetSocketAddress(configuration.getGraphiteEndpoint(), configuration.getGraphiteEndpointPort()));
            reporter = GraphiteReporter.forRegistry(metricsRegistry)
                                       .prefixedWith(configuration.getOperationType().name())
                                       .convertRatesTo(TimeUnit.SECONDS)
                                       .convertDurationsTo(TimeUnit.MILLISECONDS)
                                       .filter(MetricFilter.ALL)
                                       .build(graphite);
        } else {
            reporter = ConsoleReporter.forRegistry(metricsRegistry).convertRatesTo(TimeUnit.SECONDS)
                                      .convertDurationsTo(TimeUnit.MILLISECONDS).build();
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
            cosmosDatabase.delete();
            logger.info("Deleted temporary database {} created for this test", this.configuration.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosContainer.delete();
            logger.info("Deleted temporary collection {} created for this test", this.configuration.getCollectionId());
        }

        cosmosClient.close();
        executorService.shutdown();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract T performWorkload(long i) throws Exception;

    void run() throws Exception {

        successMeter = metricsRegistry.meter("#Successful Operations");
        failureMeter = metricsRegistry.meter("#Unsuccessful Operations");

        switch (configuration.getOperationType()) {
            case ReadLatency:
            case WriteLatency:
                // TODO: support for other operationTypes will be added later
//            case QueryInClauseParallel:
//            case QueryCross:
//            case QuerySingle:
//            case QuerySingleMany:
//            case QueryParallel:
//            case QueryOrderby:
//            case QueryAggregate:
//            case QueryAggregateTopOrderby:
//            case QueryTopOrderby:
            case Mixed:
                latency = metricsRegistry.register("Latency", new Timer(new HdrHistogramResetOnSnapshotReservoir()));
                break;
            default:
                break;
        }

        reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for ( i = 0; BenchmarkHelper.shouldContinue(startTime, i, configuration); i++) {

            ResultHandler<T, Throwable> resultHandler = new ResultHandler<T, Throwable>() {
                @Override
                public T apply(T t, Throwable throwable) {
                    successMeter.mark();
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

                        failureMeter.mark();
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

            switch (configuration.getOperationType()) {
                case ReadLatency:
                case WriteLatency:
                    // TODO: support for other operation types will be added later
//                case QueryInClauseParallel:
//                case QueryCross:
//                case QuerySingle:
//                case QuerySingleMany:
//                case QueryParallel:
//                case QueryOrderby:
//                case QueryAggregate:
//                case QueryAggregateTopOrderby:
//                case QueryTopOrderby:
//                case Mixed:
                    LatencyListener<T> latencyListener = new LatencyListener(resultHandler, latency);
                    latencyListener.context = latency.time();
                    resultHandler = latencyListener;
                    break;
                default:
                    break;
            }

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
            configuration.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();
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
