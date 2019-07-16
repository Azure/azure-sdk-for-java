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

package com.azure.data.cosmos.rx.examples.multimaster.samples;


import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Worker {
    private final static Logger logger = LoggerFactory.getLogger(Worker.class);

    private final AsyncDocumentClient client;
    private final String documentCollectionUri;

    // scheduler for blocking work
    private final Scheduler schedulerForBlockingWork;
    private final ExecutorService executor;

    public Worker(AsyncDocumentClient client, String databaseName, String collectionName) {
        this.client = client;
        this.documentCollectionUri = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
        this.executor = Executors.newSingleThreadExecutor();
        this.schedulerForBlockingWork = Schedulers.fromExecutor(executor);
    }

    public Mono<Void> runLoopAsync(int documentsToInsert) {
        return Mono.defer(() -> {

            int iterationCount = 0;

            List<Long> latency = new ArrayList<>();
            while (iterationCount++ < documentsToInsert) {
                long startTick = System.currentTimeMillis();

                Document d = new Document();
                d.id(UUID.randomUUID().toString());

                this.client.createDocument(this.documentCollectionUri, d, null, false)
                        .subscribeOn(schedulerForBlockingWork).single().block();

                long endTick = System.currentTimeMillis();

                latency.add(endTick - startTick);
            }

            Collections.sort(latency);
            int p50Index = (latency.size() / 2);

            logger.info("Inserted {} documents at {} with p50 {} ms",
                    documentsToInsert,
                    this.client.getWriteEndpoint(),
                    latency.get(p50Index));

            return Mono.empty();

        });

    }


    public Mono<Void> readAllAsync(int expectedNumberOfDocuments) {

        return Mono.defer(() -> {

            while (true) {
                int totalItemRead = 0;
                FeedResponse<Document> response = null;
                do {

                    FeedOptions options = new FeedOptions();
                    options.requestContinuation(response != null ? response.continuationToken() : null);

                    response = this.client.readDocuments(this.documentCollectionUri, options).take(1)
                            .subscribeOn(schedulerForBlockingWork).single().block();

                    totalItemRead += response.results().size();
                } while (response.continuationToken() != null);

                if (totalItemRead < expectedNumberOfDocuments) {
                    logger.info("Total item read {} from {} is less than {}, retrying reads",
                            totalItemRead,
                            this.client.getReadEndpoint(),
                            expectedNumberOfDocuments);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        logger.info("interrupted");
                        break;
                    }
                    continue;
                } else {
                    logger.info("READ {} items from {}", totalItemRead, this.client.getReadEndpoint());
                    break;
                }
            }

            return Mono.empty();
        });
    }

    void deleteAll() {
        List<Document> documents = new ArrayList<>();
        FeedResponse<Document> response = null;
        do {

            FeedOptions options = new FeedOptions();
            options.requestContinuation(response != null ? response.continuationToken() : null);

            response = this.client.readDocuments(this.documentCollectionUri, options).take(1)
                    .subscribeOn(schedulerForBlockingWork).single().block();

            documents.addAll(response.results());
        } while (response.continuationToken() != null);

        for (Document document : documents) {
            try {
                this.client.deleteDocument(document.selfLink(), null)
                        .subscribeOn(schedulerForBlockingWork).single().block();
            } catch (RuntimeException exEx) {
                CosmosClientException dce = getDocumentClientExceptionCause(exEx);

                if (dce.statusCode() != 404) {
                    logger.info("Error occurred while deleting {} from {}", dce, client.getWriteEndpoint());
                }
            }
        }

        logger.info("Deleted all documents from region {}", this.client.getWriteEndpoint());
    }

    private CosmosClientException getDocumentClientExceptionCause(Throwable e) {
        while (e != null) {

            if (e instanceof CosmosClientException) {
                return (CosmosClientException) e;
            }

            e = e.getCause();
        }

        return null;
    }

    public void shutdown() {
        executor.shutdown();
        client.close();
    }
}
