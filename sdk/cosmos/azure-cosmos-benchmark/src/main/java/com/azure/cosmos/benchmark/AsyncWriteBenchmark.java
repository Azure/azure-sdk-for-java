// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

class AsyncWriteBenchmark extends AsyncBenchmark<CosmosItemResponse> {

    private final String uuid;
    private final String dataFieldValue;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<CosmosItemResponse> baseSubscriber;

        LatencySubscriber(BaseSubscriber<CosmosItemResponse> baseSubscriber) {
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
    protected void performWorkload(BaseSubscriber<CosmosItemResponse> baseSubscriber, long i) throws InterruptedException {
        String id = uuid + i;
        Mono<CosmosItemResponse<PojoizedJson>> obs;
        if (configuration.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            obs = cosmosAsyncContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount()));
        } else {
            // more optimized for write as partition key is already passed as config
            obs = cosmosAsyncContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount()),
                new PartitionKey(id),
                null);
        }

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.WriteThroughput) {
            obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<CosmosItemResponse> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
