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
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AsynReadWithMultipleClients<T> {
    private final static String ACCOUNT_ENDPOINT_TAG = "AccountEndpoint=";
    private final static String ACCOUNT_KEY_TAG = "AccountKey=";
    private final Semaphore concurrencyControlSemaphore;
    private final Logger logger;
    private final Configuration configuration;
    private MetricRegistry metricsRegistry = new MetricRegistry();
    private ScheduledReporter reporter;
    private Meter successMeter;
    private Meter failureMeter;
    private Timer latency;
    private Map<CosmosAsyncClient, List<PojoizedJson>> clientDocsMap = new HashMap<>();
    private List<CosmosAsyncDatabase> databaseListToClear = new ArrayList<>();
    private List<CosmosAsyncContainer> collectionListToClear = new ArrayList<>();

    AsynReadWithMultipleClients(Configuration cfg) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.configuration = cfg;
        createClients();
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
        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());
    }

    void run() throws Exception {
        successMeter = metricsRegistry.meter("#Successful Operations");
        failureMeter = metricsRegistry.meter("#Unsuccessful Operations");
        latency = metricsRegistry.register("Latency", new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        AtomicLong count = new AtomicLong(0);
        long i;
        long startTime = System.currentTimeMillis();
        for (i = 0; BenchmarkHelper.shouldContinue(startTime, i, configuration); i++) {

            BaseSubscriber<PojoizedJson> baseSubscriber = new BaseSubscriber<PojoizedJson>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    super.hookOnSubscribe(subscription);
                }

                @Override
                protected void hookOnNext(PojoizedJson value) {
                    logger.debug("hookOnNext: {}, count:{}", value, count.get());
                }

                @Override
                protected void hookOnCancel() {
                    this.hookOnError(new CancellationException());
                }

                @Override
                protected void hookOnComplete() {
                    successMeter.mark();
                    concurrencyControlSemaphore.release();
                    AsynReadWithMultipleClients.this.onSuccess();

                    synchronized (count) {
                        count.incrementAndGet();
                        count.notify();
                    }
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    failureMeter.mark();
                    logger.error("Encountered failure {} on thread {}",
                        throwable.getMessage(), Thread.currentThread().getName(), throwable);
                    concurrencyControlSemaphore.release();
                    AsynReadWithMultipleClients.this.onError(throwable);

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

    void shutdown() {
        for (CosmosAsyncDatabase database : databaseListToClear) {
            database.delete().block();
        }

        if (databaseListToClear.size() > 0) {
            logger.info("Deleted database {} created on accounts for this test", this.configuration.getDatabaseId());
        }

        for (CosmosAsyncContainer container : collectionListToClear) {
            container.delete().block();
        }

        if (collectionListToClear.size() > 0) {
            logger.info("Deleted collection {} created on accounts for this test", this.configuration.getCollectionId());
        }

        for (CosmosAsyncClient asyncClient : clientDocsMap.keySet()) {
            asyncClient.close();
        }
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    private void createClients() {
        String csvFile = "clientHostAndKey.txt";
        String line = "";
        String splitBy = ";";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] hostAndKey = line.split(splitBy);
                if (hostAndKey.length >= 2) {
                    String endpoint = hostAndKey[0].substring(hostAndKey[0].indexOf(ACCOUNT_ENDPOINT_TAG) + ACCOUNT_ENDPOINT_TAG.length());
                    String key = hostAndKey[1].substring(hostAndKey[1].indexOf(ACCOUNT_KEY_TAG) + ACCOUNT_KEY_TAG.length());
                    CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                        .endpoint(endpoint)
                        .key(key)
                        .preferredRegions(this.configuration.getPreferredRegionsList())
                        .consistencyLevel(configuration.getConsistencyLevel())
                        .connectionSharingAcrossClientsEnabled(true)
                        .contentResponseOnWriteEnabled(Boolean.parseBoolean(configuration.isContentResponseOnWriteEnabled()));
                    if (this.configuration.getConnectionMode().equals(ConnectionMode.DIRECT)) {
                        cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
                    } else {
                        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
                        gatewayConnectionConfig.setMaxConnectionPoolSize(this.configuration.getMaxConnectionPoolSize());
                        cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
                    }
                    CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                    List<PojoizedJson> docsToRead = new ArrayList<>();
                    CosmosAsyncDatabase cosmosAsyncDatabase = null;
                    CosmosAsyncContainer cosmosAsyncContainer = null;
                    boolean databaseCreated = false;
                    try {
                        cosmosAsyncDatabase = asyncClient.getDatabase(this.configuration.getDatabaseId());
                        cosmosAsyncDatabase.read().block();
                    } catch (CosmosException e) {
                        if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                            asyncClient.createDatabase(this.configuration.getDatabaseId()).block();
                            cosmosAsyncDatabase = asyncClient.getDatabase(this.configuration.getDatabaseId());
                            logger.info("Database {} is created for this test on host {}", this.configuration.getDatabaseId(), endpoint);
                            databaseCreated = true;
                            databaseListToClear.add(cosmosAsyncDatabase);
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
                            logger.info("Collection {} is created for this test on host {}", this.configuration.getCollectionId(), endpoint);
                            if(!databaseCreated) {
                                collectionListToClear.add(cosmosAsyncContainer);
                            }
                        } else {
                            throw e;
                        }
                    }

                    String partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
                        .getPaths().iterator().next().split("/")[1];
                    String dataFieldValue = RandomStringUtils.randomAlphabetic(this.configuration.getDocumentDataFieldSize());
                    ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

                    for (int i = 0; i < this.configuration.getNumberOfPreCreatedDocuments(); i++) {
                        String uuid = UUID.randomUUID().toString();
                        PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                            dataFieldValue,
                            partitionKey,
                            configuration.getDocumentDataFieldCount());

                        Flux<PojoizedJson> obs = cosmosAsyncContainer.createItem(newDoc).map(resp -> {
                                com.azure.cosmos.benchmark.PojoizedJson x =
                                    resp.getItem();
                                return x;
                            }
                        ).flux();
                        createDocumentObservables.add(obs);
                    }
                    docsToRead = Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block();

                    logger.info("Client have been initialized with data created for host {}", hostAndKey[0]);
                    clientDocsMap.put(asyncClient, docsToRead);
                }
            }
            logger.info("Total number of client created for ReadThroughputWithMultipleClient {}", clientDocsMap.size());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
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

    private void performWorkload(BaseSubscriber<PojoizedJson> baseSubscriber, long i) throws InterruptedException {
        Mono<PojoizedJson> result;
        int clientIndex = (int) (i % clientDocsMap.size());
        CosmosAsyncClient client = (CosmosAsyncClient) clientDocsMap.keySet().toArray()[clientIndex];
        int docIndex = (int) i % clientDocsMap.get(client).size();
        PojoizedJson doc = clientDocsMap.get(client).get(docIndex);

        String partitionKeyValue = doc.getId();
        result = client.getDatabase(configuration.getDatabaseId()).getContainer(configuration.getCollectionId()).readItem(doc.getId(),
            new PartitionKey(partitionKeyValue),
            PojoizedJson.class).map(CosmosItemResponse::getItem);
        concurrencyControlSemaphore.acquire();
        AsyncReadBenchmark.LatencySubscriber<PojoizedJson> latencySubscriber = new AsyncReadBenchmark.LatencySubscriber<>(baseSubscriber);
        latencySubscriber.context = latency.time();
        result.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
    }
}
