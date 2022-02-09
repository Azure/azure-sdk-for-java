// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Timer;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.UUID;

public class AsyncEncryptionWriteBenchmark extends AsyncEncryptionBenchmark<CosmosItemResponse> {

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

    public AsyncEncryptionWriteBenchmark(Configuration cfg) throws IOException, MicrosoftDataEncryptionException {
        super(cfg);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected void performWorkload(BaseSubscriber<CosmosItemResponse> baseSubscriber, long i) throws InterruptedException {
        String id = uuid + i;
        Mono<CosmosItemResponse<PojoizedJson>> obs;
        PojoizedJson newDoc = BenchmarkHelper.generateDocument(id,
            dataFieldValue,
            partitionKey,
            configuration.getDocumentDataFieldCount());
        for (int j = 1; j <= configuration.getEncryptedStringFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_STRING_FIELD + j, uuid);
        }
        for (int j = 1; j <= configuration.getEncryptedLongFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_LONG_FIELD + j, 1234l);
        }
        for (int j = 1; j <= configuration.getEncryptedDoubleFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_DOUBLE_FIELD + j, 1234.01d);
        }
        if (configuration.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            obs = cosmosEncryptionAsyncContainer.createItem(newDoc, new PartitionKey(id),
                new CosmosItemRequestOptions());
        } else {
            // more optimized for write as partition key is already passed as config
            obs = cosmosEncryptionAsyncContainer.createItem(newDoc,
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
