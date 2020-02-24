// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

class AsyncReadBenchmark extends AsyncBenchmark<ResourceResponse<Document>> {

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

    AsyncReadBenchmark(Configuration cfg) {
        super(cfg);
    }

    @Override
    protected void performWorkload(BaseSubscriber<ResourceResponse<Document>> baseSubscriber, long i) throws InterruptedException {
        int index = (int) (i % docsToRead.size());
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(docsToRead.get(index).id()));

        Flux<ResourceResponse<Document>> obs = client.readDocument(getDocumentLink(docsToRead.get(index)), options);

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.ReadThroughput) {
            obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<ResourceResponse<Document>> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
