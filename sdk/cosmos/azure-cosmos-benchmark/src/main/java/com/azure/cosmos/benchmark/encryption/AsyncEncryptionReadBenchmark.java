// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;

public class AsyncEncryptionReadBenchmark extends AsyncEncryptionBenchmark<PojoizedJson> {

    public AsyncEncryptionReadBenchmark(TenantWorkloadConfig workloadCfg, Scheduler scheduler) throws IOException {
        super(workloadCfg, scheduler);
    }

    @Override
    protected Mono<PojoizedJson> performWorkload(long i) {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);
        String partitionKeyValue = doc.getId();

        return cosmosEncryptionAsyncContainer.readItem(doc.getId(),
            new PartitionKey(partitionKeyValue),
            new CosmosItemRequestOptions(),
            PojoizedJson.class).map(CosmosItemResponse::getItem);
    }
}
