// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AsyncReadManyBenchmark extends AsyncBenchmark<FeedResponse<PojoizedJson>> {

    private final Random r;

    AsyncReadManyBenchmark(TenantWorkloadConfig cfg, Scheduler scheduler) {
        super(cfg, scheduler);
        r = new Random();
    }

    @Override
    protected Mono<FeedResponse<PojoizedJson>> performWorkload(long i) {
        int tupleSize = workloadConfig.getTupleSize();
        int randomIdx = r.nextInt(workloadConfig.getNumberOfPreCreatedDocuments());
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();

        for (int idx = randomIdx; idx < randomIdx + tupleSize; idx++) {
            int index = idx % workloadConfig.getNumberOfPreCreatedDocuments();
            PojoizedJson doc = docsToRead.get(index);
            String partitionKeyValue = (String) doc.getProperty(partitionKey);
            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);

            cosmosItemIdentities.add(new CosmosItemIdentity(partitionKey, doc.getId()));
        }

        return cosmosAsyncContainer.readMany(cosmosItemIdentities, PojoizedJson.class);
    }
}