// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spring.benchmark;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.function.Consumer;

class AsyncWriteBenchmark extends AsyncBenchmark<CosmosItemResponse> {

    private final String uuid;
    private final String dataFieldValue;
    private ReactiveUserRepository reactiveUserRepository;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<CosmosItemResponse> baseSubscriber;
        ReactiveUserRepository reactiveUserRepository;

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

    AsyncWriteBenchmark(Configuration cfg, CosmosClientBuilder cosmosClientBuilder,
                        ReactiveUserRepository reactiveUserRepository) {
        super(cfg, cosmosClientBuilder, reactiveUserRepository);

        this.reactiveUserRepository = reactiveUserRepository;
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected void performWorkload(BaseSubscriber<CosmosItemResponse> baseSubscriber, long i) throws InterruptedException {
        String id = uuid + i;
        Mono<User> obs;
        if (configuration.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            User newUser = BenchmarkHelper.generateUser(id,
                dataFieldValue,
                partitionKey);
            obs = reactiveUserRepository.save(newUser);
        } else {
            // more optimized for write as partition key is already passed as config
            User newUser = BenchmarkHelper.generateUser(id,
                dataFieldValue,
                partitionKey);
            obs = reactiveUserRepository.save(newUser);
        }

        concurrencyControlSemaphore.acquire();

        if (configuration.getOperationType() == Configuration.Operation.WriteThroughput) {
            obs.subscribeOn(Schedulers.parallel()).subscribe((Consumer<? super User>) baseSubscriber);
        } else {
            LatencySubscriber<CosmosItemResponse> latencySubscriber = new LatencySubscriber<>(baseSubscriber);
            latencySubscriber.context = latency.time();
            obs.subscribeOn(Schedulers.parallel()).subscribe((Consumer<? super User>) latencySubscriber);
        }
    }
}
