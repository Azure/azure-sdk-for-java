// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Timer;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Random;

public class AsyncEncryptionQueryBenchmark extends AsyncEncryptionBenchmark<FeedResponse<PojoizedJson>> {

    private int pageCount = 0;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<T> baseSubscriber;

        LatencySubscriber(BaseSubscriber<T> baseSubscriber) {
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

    public AsyncEncryptionQueryBenchmark(Configuration cfg) throws IOException, MicrosoftDataEncryptionException {
        super(cfg);
    }

    @Override
    protected void onSuccess() {
        pageCount++;
        if (pageCount % 10000 == 0) {
            if (pageCount == 0) {
                return;
            }
            logger.info("total pages so far: {}", pageCount);
        }
    }

    @Override
    protected void performWorkload(BaseSubscriber<FeedResponse<PojoizedJson>> baseSubscriber, long i) throws InterruptedException {
        Flux<FeedResponse<PojoizedJson>> obs;
        Random r = new Random();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (configuration.getOperationType() == Configuration.Operation.QueryCross) {

            int index = r.nextInt(this.configuration.getNumberOfPreCreatedDocuments());
            String sqlQuery = "Select * from c where c.id = \"" + docsToRead.get(index).getId() + "\"";
            obs = cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage();
        } else if (configuration.getOperationType() == Configuration.Operation.QuerySingle) {

            int index = r.nextInt(this.configuration.getNumberOfPreCreatedDocuments());
            String pk = (String) docsToRead.get(index).getProperty(partitionKey);
            options.setPartitionKey(new PartitionKey(pk));
            String sqlQuery = "Select * from c where c." + partitionKey + " = \"" + pk + "\"";
            obs = cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage();
        } else if (configuration.getOperationType() == Configuration.Operation.QueryParallel) {

            String sqlQuery = "Select * from c";
            obs = cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryOrderby) {

            String sqlQuery = "Select * from c order by c._ts";
            obs = cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryTopOrderby) {

            String sqlQuery = "Select top 1000 * from c order by c._ts";
            obs = cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage();
        } else if (configuration.getOperationType() == Configuration.Operation.ReadAllItemsOfLogicalPartition) {
            throw new IllegalArgumentException("Unsupported Operation on encryption: " + configuration.getOperationType());
        } else {
            throw new IllegalArgumentException("Unsupported Operation: " + configuration.getOperationType());
        }

        concurrencyControlSemaphore.acquire();
        LatencySubscriber<FeedResponse> latencySubscriber = new LatencySubscriber(baseSubscriber);
        latencySubscriber.context = latency.time();
        obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
    }
}
