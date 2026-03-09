// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Random;
import java.util.UUID;


class AsyncMixedBenchmark extends AsyncBenchmark<Object> {

    private final String uuid;
    private final String dataFieldValue;
    private final Random r;

    AsyncMixedBenchmark(TenantWorkloadConfig cfg, Scheduler scheduler) {
        super(cfg, scheduler);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(workloadConfig.getDocumentDataFieldSize());
        r = new Random();
    }

    @Override
    protected Mono<Object> performWorkload(long i) {
        if (i % 10 == 0 && i % 100 != 0) {

            PojoizedJson data = BenchmarkHelper.generateDocument(uuid + i,
                dataFieldValue,
                partitionKey,
                workloadConfig.getDocumentDataFieldCount());
            return cosmosAsyncContainer.createItem(data).map(resp -> resp);

        } else if (i % 100 == 0) {

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            String sqlQuery = "Select top 100 * from c order by c._ts";
            return cosmosAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10).last().map(resp -> resp);
        } else {

            int index = r.nextInt(1000);
            String partitionKeyValue = docsToRead.get(index).getId();

            return cosmosAsyncContainer.readItem(docsToRead.get(index).getId(),
                                              new PartitionKey(partitionKeyValue),
                                              PojoizedJson.class)
                    .map(resp -> resp);
        }
    }
}
