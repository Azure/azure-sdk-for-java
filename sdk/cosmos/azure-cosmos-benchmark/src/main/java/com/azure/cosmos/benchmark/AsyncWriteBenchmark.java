// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.PartitionKey;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

class AsyncWriteBenchmark extends AsyncBenchmark<CosmosAsyncItemResponse> {

    private final String uuid;
    private final String dataFieldValue;

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

    AsyncWriteBenchmark(Configuration cfg) {
        super(cfg);

        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected void performWorkload(BaseSubscriber<CosmosAsyncItemResponse> baseSubscriber, long i) throws InterruptedException {
        String partitionKey = uuid + i;
        Mono<CosmosAsyncItemResponse<PojoizedJson>> obs;
        if (configuration.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            obs = cosmosAsyncContainer.createItem(generateDocument(partitionKey, dataFieldValue));
        } else {
            // more optimized for write as partition ke is already passed as config
            obs = cosmosAsyncContainer.createItem(generateDocument(partitionKey, dataFieldValue),
                                                  new PartitionKey(partitionKey),
                                                  new CosmosItemRequestOptions());
        }

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.WriteThroughput) {
            obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } else {
            LatencySubscriber<CosmosAsyncItemResponse> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
        }
    }
}
