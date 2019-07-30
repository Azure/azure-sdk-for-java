// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

class AsyncWriteBenchmark extends AsyncBenchmark<ResourceResponse<Document>> {

    private final String uuid;
    private final String dataFieldValue;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<ResourceResponse<Document>> baseSubscriber;

        LatencySubscriber(BaseSubscriber<ResourceResponse<Document>> baseSubscriber) {
            this.baseSubscriber = baseSubscriber;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            super.hookOnSubscribe(subscription);
        }

        @Override
        protected void hookOnNext(T value) {
        }

        @Override
        protected void hookOnComplete() {
            context.stop();
            baseSubscriber.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            context.stop();
            baseSubscriber.onError(throwable);
        }
    }

    AsyncWriteBenchmark(Configuration cfg) {
        super(cfg);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected void performWorkload(BaseSubscriber<ResourceResponse<Document>> baseSubscriber, long i) throws InterruptedException {

        String idString = uuid + i;
        Document newDoc = new Document();
        newDoc.id(idString);
        BridgeInternal.setProperty(newDoc, partitionKey, idString);
        BridgeInternal.setProperty(newDoc, "dataField1", dataFieldValue);
        BridgeInternal.setProperty(newDoc, "dataField2", dataFieldValue);
        BridgeInternal.setProperty(newDoc, "dataField3", dataFieldValue);
        BridgeInternal.setProperty(newDoc, "dataField4", dataFieldValue);
        BridgeInternal.setProperty(newDoc, "dataField5", dataFieldValue);
        Flux<ResourceResponse<Document>> obs = client.createDocument(getCollectionLink(), newDoc, null,
                false);

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.WriteThroughput) {
            obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<ResourceResponse<Document>> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
