// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class AsyncReadBenchmark extends AsyncBenchmark<CosmosAsyncItemResponse> {
    private final CosmosAsyncContainer cosmosAsyncContainer;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<CosmosAsyncItemResponse> baseSubscriber;

        LatencySubscriber(BaseSubscriber<CosmosAsyncItemResponse> baseSubscriber) {
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
        cosmosAsyncContainer = v4Client.getDatabase(cfg.getDatabaseId()).getContainer(cfg.getCollectionId()).read().block().getContainer();
    }

    @Override
    protected void performWorkload(BaseSubscriber<CosmosAsyncItemResponse> baseSubscriber, long i) throws InterruptedException {
        int index = (int) (i % docsToRead.size());
        Document doc = docsToRead.get(index);

        String partitionKeyValue = doc.getId();
        Mono<CosmosAsyncItemResponse> result = cosmosAsyncContainer.getItem(doc.getId(), partitionKeyValue).read();

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.ReadThroughput) {
            result.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<CosmosAsyncItemResponse> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            result.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
