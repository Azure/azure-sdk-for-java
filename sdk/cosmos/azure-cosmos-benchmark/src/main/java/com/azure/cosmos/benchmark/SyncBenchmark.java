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
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
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

abstract class SyncBenchmark<T> {

    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor clientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();

    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final ScheduledReporter reporter;

    private final ScheduledReporter resultReporter;
    private final ExecutorService executorService;

    private Meter successMeter;
    private Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    final Logger logger;
    final CosmosClient benchmarkWorkloadClient;
    final CosmosClient resultUploaderClient;
    CosmosContainer cosmosContainer;
    CosmosDatabase cosmosDatabase;

    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
    final BenchmarkConfig benchConfig;
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

    SyncBenchmark(TenantWorkloadConfig workloadCfg, BenchmarkConfig benchCfg) throws Exception {
        executorService = Executors.newFixedThreadPool(workloadCfg.getConcurrency());
        workloadConfig = workloadCfg;
        benchConfig = benchCfg;
        logger = LoggerFactory.getLogger(this.getClass());

        boolean isManagedIdentityRequired = workloadCfg.isManagedIdentityRequired();

        final TokenCredential credential = isManagedIdentityRequired
            ? workloadCfg.buildTokenCredential()
            : null;

        CosmosClientBuilder benchmarkSpecificClientBuilder = isManagedIdentityRequired ?
                new CosmosClientBuilder()
                        .credential(credential) :
                new CosmosClientBuilder()
                        .key(workloadCfg.getMasterKey());

        CosmosClientBuilder resultUploadClientBuilder = new CosmosClientBuilder();

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
        this.resultUploaderClient = resultUploadClientBuilder
                .endpoint(StringUtils.isNotEmpty(benchConfig.getResultUploadEndpoint()) ? benchConfig.getResultUploadEndpoint() : workloadCfg.getServiceEndpoint())
                .key(StringUtils.isNotEmpty(benchConfig.getResultUploadKey()) ? benchConfig.getResultUploadKey() : workloadCfg.getMasterKey())
                .buildClient();

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

            if (workloadCfg.getOperationType() != Operation.WriteLatency
                    && workloadCfg.getOperationType() != Operation.WriteThroughput
                    && workloadCfg.getOperationType() != Operation.ReadMyWrites) {
                String dataFieldValue = RandomStringUtils.randomAlphabetic(workloadCfg.getDocumentDataFieldSize());
                for (int i = 0; i < workloadCfg.getNumberOfPreCreatedDocuments(); i++) {
                    String uuid = UUID.randomUUID().toString();
                    PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                            dataFieldValue,
                            partitionKey,
                            workloadCfg.getDocumentDataFieldCount());
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

            if (benchConfig.isEnableJvmStats()) {
                metricsRegistry.register("gc", new GarbageCollectorMetricSet());
                metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
                metricsRegistry.register("memory", new MemoryUsageGaugeSet());
            }

            if (benchConfig.getReportingDirectory() != null) {
                reporter = CsvReporter.forRegistry(metricsRegistry).convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.MILLISECONDS).build(new java.io.File(benchConfig.getReportingDirectory()));
            } else {
                reporter = ConsoleReporter.forRegistry(metricsRegistry).convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.MILLISECONDS).build();
            }

            if (benchConfig.getResultUploadDatabase() != null && benchConfig.getResultUploadContainer() != null) {
                String op = workloadConfig.isSync()
                    ? "SYNC_" + workloadCfg.getOperationType().name()
                    : workloadCfg.getOperationType().name();
                resultReporter = CosmosTotalResultReporter
                        .forRegistry(
                                metricsRegistry,
                                this.resultUploaderClient.getDatabase(benchConfig.getResultUploadDatabase()).getContainer(benchConfig.getResultUploadContainer()),
                                op,
                                benchConfig.getTestVariationName(),
                                benchConfig.getBranchName(),
                                benchConfig.getCommitId(),
                                workloadCfg.getConcurrency())
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS).build();
            } else {
                resultReporter = null;
            }

    }

    protected void init() {
    }

    void shutdown() {

        if (this.databaseCreated) {
            cosmosDatabase.delete();
            logger.info("Deleted temporary database {} created for this test", workloadConfig.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosContainer.delete();
            logger.info("Deleted temporary collection {} created for this test", workloadConfig.getContainerId());
        }

        resultUploaderClient.close();
        benchmarkWorkloadClient.close();
        executorService.shutdown();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract T performWorkload(long i) throws Exception;

    void run() throws Exception {

        successMeter = metricsRegistry.meter(TenantWorkloadConfig.SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(TenantWorkloadConfig.FAILURE_COUNTER_METER_NAME);

        switch (workloadConfig.getOperationType()) {
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
                latency = metricsRegistry.register(TenantWorkloadConfig.LATENCY_METER_NAME, new Timer(new HdrHistogramResetOnSnapshotReservoir()));
                break;
            default:
                break;
        }

        reporter.start(benchConfig.getPrintingInterval(), TimeUnit.SECONDS);
        if (resultReporter != null) {
            resultReporter.start(benchConfig.getPrintingInterval(), TimeUnit.SECONDS);
        }
        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for ( i = 0; BenchmarkHelper.shouldContinue(startTime, i, workloadConfig); i++) {

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

            switch (workloadConfig.getOperationType()) {
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
            workloadConfig.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();

        if (resultReporter != null) {
            resultReporter.report();
            resultReporter.close();
        }
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
