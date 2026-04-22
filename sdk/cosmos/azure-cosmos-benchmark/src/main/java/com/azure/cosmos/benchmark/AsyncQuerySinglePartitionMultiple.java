// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;


class AsyncQuerySinglePartitionMultiple extends AsyncBenchmark<FeedResponse<PojoizedJson>> {

    private static final String SQL_QUERY = "Select * from c where c.pk = \"pk\"";
    private CosmosQueryRequestOptions options;
    private int pageCount = 0;

    AsyncQuerySinglePartitionMultiple(TenantWorkloadConfig cfg, Scheduler scheduler) {
        super(cfg, scheduler);
        options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey("pk"));
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
    protected Mono<FeedResponse<PojoizedJson>> performWorkload(long i) {
        return cosmosAsyncContainer.queryItems(SQL_QUERY, options, PojoizedJson.class)
            .byPage(10)
            .last();
    }
}
