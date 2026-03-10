// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;

public class AsyncEncryptionQuerySinglePartitionMultiple extends AsyncEncryptionBenchmark<FeedResponse<PojoizedJson>> {

    private static final String SQL_QUERY = "Select * from c where c.pk = \"pk\"";
    private CosmosQueryRequestOptions options;
    private int pageCount = 0;

    public AsyncEncryptionQuerySinglePartitionMultiple(TenantWorkloadConfig workloadCfg, Scheduler scheduler) throws IOException {
        super(workloadCfg, scheduler);
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
        return cosmosEncryptionAsyncContainer.queryItems(SQL_QUERY, options, PojoizedJson.class)
            .byPage(10).last();
    }
}
