/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.benchmark;

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
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.benchmark.Configuration.Operation;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

abstract class AsyncBenchmark<T> {
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final ScheduledReporter reporter;
    private final CountDownLatch operationCounterLatch;
    private final String nameCollectionLink;

    private Meter successMeter;
    private Meter failureMeter;

    final Logger logger;
    final AsyncDocumentClient client;
    final DocumentCollection collection;
    final String partitionKey;
    final Configuration configuration;
    final List<Document> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    AsyncBenchmark(Configuration cfg) {
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cfg.getServiceEndpoint())
                .withMasterKeyOrResourceToken(cfg.getMasterKey())
                .withConnectionPolicy(cfg.getConnectionPolicy())
                .withConsistencyLevel(cfg.getConsistencyLevel())
                .build();

        logger = LoggerFactory.getLogger(this.getClass());

        Database database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
        collection = DocDBUtils.getCollection(client, database.getSelfLink(), cfg.getCollectionId());
        nameCollectionLink = String.format("dbs/%s/colls/%s", database.getId(), collection.getId());
        partitionKey = collection.getPartitionKey().getPaths().iterator().next().split("/")[1];
        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());
        operationCounterLatch = new CountDownLatch(cfg.getNumberOfOperations());
        configuration = cfg;

        ArrayList<Observable<Document>> createDocumentObservables = new ArrayList<>();

        if (configuration.getOperationType() != Operation.WriteLatency
                && configuration.getOperationType() != Operation.WriteThroughput
                && configuration.getOperationType() != Operation.ReadMyWrites) {
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                Document newDoc = new Document();
                newDoc.setId(uuid);
                newDoc.set(partitionKey, uuid);
                newDoc.set("dataField1", dataFieldValue);
                newDoc.set("dataField2", dataFieldValue);
                newDoc.set("dataField3", dataFieldValue);
                newDoc.set("dataField4", dataFieldValue);
                newDoc.set("dataField5", dataFieldValue);
                Observable<Document> obs = client.createDocument(collection.getSelfLink(), newDoc, null, false)
                        .map(ResourceResponse::getResource);
                createDocumentObservables.add(obs);
            }
        }

        docsToRead = Observable.merge(createDocumentObservables, 100).toList().toBlocking().single();
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
    }

    protected void init() {
    }

    void shutdown() {
        client.close();
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

    protected abstract void performWorkload(Subscriber<T> subs, long i) throws Exception;

    void run() throws Exception {

        successMeter = metricsRegistry.meter("#Successful Operations");
        failureMeter = metricsRegistry.meter("#Unsuccessful Operations");
        if (configuration.getOperationType() == Operation.ReadLatency
                || configuration.getOperationType() == Operation.WriteLatency)
            latency = metricsRegistry.timer("Latency");

        reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);

        long startTime = System.currentTimeMillis();

        for (long i = 1; i <= configuration.getNumberOfOperations(); i++) {

            Subscriber<T> subs = new Subscriber<T>() {

                @Override
                public void onStart() {
                }

                @Override
                public void onCompleted() {
                    successMeter.mark();
                    concurrencyControlSemaphore.release();
                    operationCounterLatch.countDown();
                    AsyncBenchmark.this.onSuccess();
                }

                @Override
                public void onError(Throwable e) {
                    failureMeter.mark();
                    logger.error("Encountered failure {} on thread {}" ,
                                 e.getMessage(), Thread.currentThread().getName(), e);
                    concurrencyControlSemaphore.release();
                    operationCounterLatch.countDown();
                    AsyncBenchmark.this.onError(e);
                }

                @Override
                public void onNext(T value) {
                }
            };

            performWorkload(subs, i);
        }

        operationCounterLatch.await();
        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
                    configuration.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();
    }
}
