// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.BridgeInternalBenchmark;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosKeyCredential;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
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
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

abstract class AsyncBenchmark<T> {
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final ScheduledReporter reporter;
    private final String nameCollectionLink;

    private Meter successMeter;
    private Meter failureMeter;

    final Logger logger;
    final CosmosAsyncClient v4Client;
    final AsyncDocumentClient client;
    final DocumentCollection collection;
    final String partitionKey;
    final Configuration configuration;
    final List<Document> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    AsyncBenchmark(Configuration cfg) {
        v4Client = new CosmosClientBuilder()
            .setEndpoint(cfg.getServiceEndpoint())
            .setKey(cfg.getMasterKey())
            .setConnectionPolicy(cfg.getConnectionPolicy())
            .setConsistencyLevel(cfg.getConsistencyLevel())
            .buildAsyncClient();

        logger = LoggerFactory.getLogger(this.getClass());

        client =  BridgeInternalBenchmark.getOldClient(v4Client);

        Database database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
        collection = DocDBUtils.getCollection(client, database.getSelfLink(), cfg.getCollectionId());
        nameCollectionLink = String.format("dbs/%s/colls/%s", database.getId(), collection.getId());
        partitionKey = collection.getPartitionKey().getPaths().iterator().next().split("/")[1];
        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());
        configuration = cfg;

        ArrayList<Flux<Document>> createDocumentObservables = new ArrayList<>();

        if (configuration.getOperationType() != Configuration.Operation.WriteLatency
                && configuration.getOperationType() != Configuration.Operation.WriteThroughput
                && configuration.getOperationType() != Configuration.Operation.ReadMyWrites) {
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                Document newDoc = new Document();
                newDoc.setId(uuid);
                BridgeInternal.setProperty(newDoc, partitionKey, uuid);
                BridgeInternal.setProperty(newDoc, "dataField1", dataFieldValue);
                BridgeInternal.setProperty(newDoc, "dataField2", dataFieldValue);
                BridgeInternal.setProperty(newDoc, "dataField3", dataFieldValue);
                BridgeInternal.setProperty(newDoc, "dataField4", dataFieldValue);
                BridgeInternal.setProperty(newDoc, "dataField5", dataFieldValue);
                Flux<Document> obs = client.createDocument(collection.getSelfLink(), newDoc, null, false)
                                                 .map(ResourceResponse::getResource);
                createDocumentObservables.add(obs);
            }
        }

        docsToRead = Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block();
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
        v4Client.close();
    }

    protected void onSuccess() {
    }

    protected void onError(Throwable throwable) {
    }

    protected String getCollectionLink() {
        if (configuration.isUseNameLink()) {
            return this.nameCollectionLink;
        } else {
            return collection.getSelfLink();
        }
    }

    protected String getDocumentLink(Document doc) {
        if (configuration.isUseNameLink()) {
            return this.nameCollectionLink + "/docs/" + doc.getId();
        } else {
            return doc.getSelfLink();
        }
    }

    protected abstract void performWorkload(BaseSubscriber<T> baseSubscriber, long i) throws Exception;

    private boolean shouldContinue(long startTimeMillis, long iterationCount) {

        Duration maxDurationTime = configuration.getMaxRunningTimeDuration();
        int maxNumberOfOperations = configuration.getNumberOfOperations();

        if (maxDurationTime == null) {
            return iterationCount < maxNumberOfOperations;
        }

        if (startTimeMillis + maxDurationTime.toMillis() < System.currentTimeMillis()) {
            return false;
        }

        if (maxNumberOfOperations < 0) {
            return true;
        }

        return iterationCount < maxNumberOfOperations;
    }

    void run() throws Exception {

        successMeter = metricsRegistry.meter("#Successful Operations");
        failureMeter = metricsRegistry.meter("#Unsuccessful Operations");

        if (configuration.getOperationType() == Configuration.Operation.ReadLatency
                || configuration.getOperationType() == Configuration.Operation.WriteLatency) {
            latency = metricsRegistry.timer("Latency");
        }

        reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for ( i = 0; shouldContinue(startTime, i); i++) {

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
