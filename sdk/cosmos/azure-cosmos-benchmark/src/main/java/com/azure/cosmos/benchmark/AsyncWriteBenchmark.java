// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.UUID;

class AsyncWriteBenchmark extends AsyncBenchmark<CosmosItemResponse> {

    private final String uuid;
    private final String dataFieldValue;

    AsyncWriteBenchmark(TenantWorkloadConfig cfg, Scheduler scheduler) {
        super(cfg, scheduler);

        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(workloadConfig.getDocumentDataFieldSize());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Mono<CosmosItemResponse> performWorkload(long i) {
        String id = uuid + i;
        Mono<? extends CosmosItemResponse<?>> result;
        if (workloadConfig.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            result = cosmosAsyncContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                workloadConfig.getDocumentDataFieldCount()));
        } else {
            result = cosmosAsyncContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                workloadConfig.getDocumentDataFieldCount()),
                new PartitionKey(id),
                null);
        }
        // Raw type cast is required because CosmosItemResponse uses wildcard generics
        // that cannot be expressed in the class type parameter without propagating
        // the wildcard throughout the benchmark hierarchy.
        return (Mono) result;
    }
}
