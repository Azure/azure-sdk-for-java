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

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.ResourceResponse;
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
