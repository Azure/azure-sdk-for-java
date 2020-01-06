// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.implementation.RequestOptions;
import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Random;
import java.util.UUID;

class AsyncMixedBenchmark extends AsyncBenchmark<Object> {

    private final String uuid;
    private final String dataFieldValue;
    private final Random r;

    AsyncMixedBenchmark(Configuration cfg) {
        super(cfg);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
        r = new Random();
    }

    @Override
    protected void performWorkload(BaseSubscriber<Object> documentBaseSubscriber, long i) throws InterruptedException {
        Flux<? extends Object> obs;
        if (i % 10 == 0 && i % 100 != 0) {

            PojoizedJson data = generateDocument(uuid + i, dataFieldValue);
            obs = cosmosAsyncContainer.createItem(data).flux();

        } else if (i % 100 == 0) {

            FeedOptions options = new FeedOptions();
            options.maxItemCount(10);

            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = cosmosAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class);
        } else {

            int index = r.nextInt(1000);

            RequestOptions options = new RequestOptions();
            String partitionKeyValue = docsToRead.get(index).getId();

            options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

            obs = cosmosAsyncContainer.getItem(docsToRead.get(index).getId(), partitionKeyValue).read().flux();
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(documentBaseSubscriber);
    }
}
