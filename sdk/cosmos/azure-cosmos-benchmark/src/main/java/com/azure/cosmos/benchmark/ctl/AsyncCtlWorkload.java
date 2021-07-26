// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.ctl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.BenchmarkRequestSubscriber;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.ScheduledReporterFactory;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class AsyncCtlWorkload {
    private final String PERCENT_PARSING_ERROR = "Unable to parse user provided readWriteQueryPct ";
    private final String prefixUuidForCreate;
    private final String dataFieldValue;
    private final String partitionKey;
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final Logger logger;
    private final CosmosAsyncClient cosmosClient;
    private final Configuration configuration;
    private final Map<String, List<PojoizedJson>> docsToRead = new HashMap<>();
    private final Semaphore concurrencyControlSemaphore;
    private final Random random;

    private Timer readLatency;
    private Timer writeLatency;
    private Timer queryLatency;
    private ScheduledReporter reporter;

    private Meter readSuccessMeter;
    private Meter readFailureMeter;
    private Meter writeSuccessMeter;
    private Meter writeFailureMeter;
    private Meter querySuccessMeter;
    private Meter queryFailureMeter;

    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private List<CosmosAsyncContainer> containers = new ArrayList<>();
    private List<String> containerToClearAfterTest = new ArrayList<>();
    private boolean databaseCreated;
    private int readPct;
    private int writePct;
    private int queryPct;

    public AsyncCtlWorkload(Configuration cfg) {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(cfg.getServiceEndpoint())
            .key(cfg.getMasterKey())
            .preferredRegions(cfg.getPreferredRegionsList())
            .consistencyLevel(cfg.getConsistencyLevel())
            .clientTelemetryEnabled(true)
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

        parsedReadWriteQueryPct(configuration.getReadWriteQueryPct());

        createDatabaseAndContainers(configuration);

        partitionKey = containers.get(0).read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];

        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());

        logger.info("PRE-populating {} documents ....", cfg.getNumberOfPreCreatedDocuments());
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
        createPrePopulatedDocs(configuration.getNumberOfPreCreatedDocuments());

        if (configuration.isEnableJvmStats()) {
            metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        reporter = ScheduledReporterFactory.create(cfg, metricsRegistry);

        MeterRegistry registry = configuration.getAzureMonitorMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }

        registry = configuration.getGraphiteMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }
        prefixUuidForCreate = UUID.randomUUID().toString();
        random = new Random();
    }

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
        CosmosAsyncContainer container = containers.get((int) i % containers.size());
        if (type.equals(OperationType.Create)) {
            PojoizedJson data = BenchmarkHelper.generateDocument(prefixUuidForCreate + i,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount());
            obs = container.createItem(data).flux();
        } else if (type.equals(OperationType.Query)) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = container.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10);
        } else {
            int index = random.nextInt(docsToRead.get(container.getId()).size());
            RequestOptions options = new RequestOptions();
            String partitionKeyValue = docsToRead.get(container.getId()).get(index).getId();
            options.setPartitionKey(new PartitionKey(partitionKeyValue));
            obs = container.readItem(docsToRead.get(container.getId()).get(index).getId(),
                new PartitionKey(partitionKeyValue),
                PojoizedJson.class)
                .flux();
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(documentSubscriber);
    }

    public void run() throws Exception {
        readSuccessMeter = metricsRegistry.meter("#Read Successful Operations");
        readFailureMeter = metricsRegistry.meter("#Read Unsuccessful Operations");
        writeSuccessMeter = metricsRegistry.meter("#Write Successful Operations");
        writeFailureMeter = metricsRegistry.meter("#Write Unsuccessful Operations");
        querySuccessMeter = metricsRegistry.meter("#Query Successful Operations");
        queryFailureMeter = metricsRegistry.meter("#Query Unsuccessful Operations");
        readLatency = metricsRegistry.register("Read Latency", new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        writeLatency = metricsRegistry.register("Write Latency", new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        queryLatency = metricsRegistry.register("Query Latency", new Timer(new HdrHistogramResetOnSnapshotReservoir()));

        reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
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
                    configuration.getDiagnosticsThresholdDuration());
                readSubscriber.context = readLatency.time();
                performWorkload(readSubscriber, OperationType.Read, i);
            } else if (index < writeRange) {
                BenchmarkRequestSubscriber<Object> writeSubscriber = new BenchmarkRequestSubscriber<>(writeSuccessMeter,
                    writeFailureMeter,
                    concurrencyControlSemaphore,
                    count,
                    configuration.getDiagnosticsThresholdDuration());
                writeSubscriber.context = writeLatency.time();
                performWorkload(writeSubscriber, OperationType.Create, i);

            } else {
                BenchmarkRequestSubscriber<Object> querySubscriber = new BenchmarkRequestSubscriber<>(querySuccessMeter,
                    queryFailureMeter,
                    concurrencyControlSemaphore,
                    count,
                    configuration.getDiagnosticsThresholdDuration());
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

    private void createDatabaseAndContainers(Configuration cfg) {
        try {
            cosmosAsyncDatabase = cosmosClient.getDatabase(this.configuration.getDatabaseId());
            cosmosAsyncDatabase.read().block();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(cfg.getDatabaseId(), ThroughputProperties.createManualThroughput(this.configuration.getThroughput())).block();
                cosmosAsyncDatabase = cosmosClient.getDatabase(cfg.getDatabaseId());
                logger.info("Database {} is created for this test", this.configuration.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        int numberOfCollection = cfg.getNumberOfCollectionForCtl();
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
}
