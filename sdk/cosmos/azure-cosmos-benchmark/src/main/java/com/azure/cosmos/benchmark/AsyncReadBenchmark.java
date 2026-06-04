// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import reactor.core.publisher.Mono;

class AsyncReadBenchmark extends AsyncBenchmark<PojoizedJson> {

    AsyncReadBenchmark(TenantWorkloadConfig cfg) {
        super(cfg);
    }

    @Override
    protected Mono<PojoizedJson> performWorkload(long i) {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setExcludedRegions(workloadConfig.getExcludedRegionsList());
        return cosmosAsyncContainer.readItem(doc.getId(),
            new PartitionKey(doc.getId()), options, PojoizedJson.class)
            .map(CosmosItemResponse::getItem);
    }
}
