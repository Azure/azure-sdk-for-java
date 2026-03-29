// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

class AsyncReadBenchmark extends AsyncBenchmark<PojoizedJson> {

    private final CosmosItemRequestOptions readOptions;

    AsyncReadBenchmark(TenantWorkloadConfig cfg, Scheduler scheduler) {
        super(cfg, scheduler);
        this.readOptions = new CosmosItemRequestOptions();
        if (e2ePolicyConfig != null) {
            readOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicyConfig);
        }
    }

    @Override
    protected Mono<PojoizedJson> performWorkload(long i) {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);
        return cosmosAsyncContainer.readItem(doc.getId(),
            new PartitionKey(doc.getId()), readOptions, PojoizedJson.class)
            .map(CosmosItemResponse::getItem);
    }
}
