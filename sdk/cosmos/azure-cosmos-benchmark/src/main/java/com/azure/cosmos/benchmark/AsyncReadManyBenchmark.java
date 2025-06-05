// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AsyncReadManyBenchmark extends AsyncBenchmark<FeedResponse<PojoizedJson>> {

    private final Random r;

    static class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<T> baseSubscriber;

        LatencySubscriber(BaseSubscriber<T> baseSubscriber) { this.baseSubscriber = baseSubscriber; }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            super.hookOnSubscribe(subscription);
        }

        @Override
        protected void hookOnNext(T value) {}

        @Override
        protected void hookOnComplete() {
            context.stop();
            baseSubscriber.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            context.stop();
            super.hookOnError(throwable);
        }
    }

    AsyncReadManyBenchmark(Configuration cfg) {
        super(cfg);
        r = new Random();
    }

    @Override
    protected void performWorkload(BaseSubscriber<FeedResponse<PojoizedJson>> baseSubscriber, long i) throws Exception {
        int tupleSize = configuration.getTupleSize();
        int randomIdx = r.nextInt(configuration.getNumberOfPreCreatedDocuments());
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();

        for (int idx = randomIdx; idx < randomIdx + tupleSize; idx++) {
            int index = idx % configuration.getNumberOfPreCreatedDocuments();
            PojoizedJson doc = docsToRead.get(index);
            String partitionKeyValue = (String) doc.getProperty(partitionKey);
            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);

            cosmosItemIdentities.add(new CosmosItemIdentity(partitionKey, doc.getId()));
        }

        Mono<FeedResponse<PojoizedJson>> obs = cosmosAsyncContainer.readMany(cosmosItemIdentities, PojoizedJson.class);

        concurrencyControlSemaphore.acquire();

        switch (configuration.getOperationType()) {
            case ReadManyLatency:
                readManyLatency(obs, baseSubscriber);
                break;
            case ReadManyThroughput:
                readManyThroughput(obs, baseSubscriber);
                break;
            default:
                throw new IllegalArgumentException("invalid workload type " + configuration.getOperationType());
        }
    }

    private void readManyLatency(Mono<FeedResponse<PojoizedJson>> obs, BaseSubscriber<FeedResponse<PojoizedJson>> baseSubscriber) {
        LatencySubscriber<FeedResponse<PojoizedJson>> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
        latencySubscriber.context = latency.time();

        obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
    }

    private void readManyThroughput(Mono<FeedResponse<PojoizedJson>> obs, BaseSubscriber<FeedResponse<PojoizedJson>> baseSubscriber) {
        obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
    }
}