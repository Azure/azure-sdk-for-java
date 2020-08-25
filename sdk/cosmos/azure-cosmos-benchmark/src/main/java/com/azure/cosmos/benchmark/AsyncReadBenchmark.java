// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

class AsyncReadBenchmark extends AsyncBenchmark<PojoizedJson> {

    static class LatencySubscriber<T> extends BaseSubscriber<T> {

        volatile Timer.Context context;
        BaseSubscriber<PojoizedJson> baseSubscriber;

        LatencySubscriber(BaseSubscriber<PojoizedJson> baseSubscriber) {
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
    protected void performWorkload(BaseSubscriber<PojoizedJson> baseSubscriber, long i) throws InterruptedException {
        Mono sparsitySleepMono = sparsityMono();

        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);

        String partitionKeyValue = doc.getId();

        Mono<PojoizedJson> result = cosmosAsyncContainer.readItem(doc.getId(),
            new PartitionKey(partitionKeyValue),
            PojoizedJson.class).map(CosmosItemResponse::getItem);

        if (sparsitySleepMono != null) {
            result = Mono.from(Flux.concat(sparsitySleepMono, result));
        }

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.ReadThroughput) {
            result.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<PojoizedJson> latencySubscriber = new LatencySubscriber<>(baseSubscriber);

            if (sparsitySleepMono != null) {
                sparsitySleepMono.doFinally(signalType -> {
                    latencySubscriber.context = latency.time();
                });
            } else {
                latencySubscriber.context = latency.time();
            }

            result.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
