// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
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

            PojoizedJson data = BenchmarkHelper.generateDocument(uuid + i,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount());
            obs = cosmosAsyncContainer.createItem(data).flux();

        } else if (i % 100 == 0) {

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            String sqlQuery = "Select top 100 * from c order by c._ts";
            obs = cosmosAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10);
        } else {

            int index = r.nextInt(1000);

            RequestOptions options = new RequestOptions();
            String partitionKeyValue = docsToRead.get(index).getId();

            options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

            obs = cosmosAsyncContainer.readItem(docsToRead.get(index).getId(),
                                              new PartitionKey(partitionKeyValue),
                                              PojoizedJson.class)
                    .flux();
        }

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(documentBaseSubscriber);
    }
}
