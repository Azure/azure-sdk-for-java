// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.Operation;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.Random;

public class AsyncEncryptionQueryBenchmark extends AsyncEncryptionBenchmark<FeedResponse<PojoizedJson>> {

    private int pageCount = 0;

    public AsyncEncryptionQueryBenchmark(TenantWorkloadConfig workloadCfg, Scheduler scheduler) throws IOException {
        super(workloadCfg, scheduler);
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
    @SuppressWarnings("unchecked")
    protected Mono<FeedResponse<PojoizedJson>> performWorkload(long i) {
        Random r = new Random();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (workloadConfig.getOperationType() == Operation.QueryCross) {

            int index = r.nextInt(this.workloadConfig.getNumberOfPreCreatedDocuments());
            String sqlQuery = "Select * from c where c.id = \"" + docsToRead.get(index).getId() + "\"";
            return cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage().last();
        } else if (workloadConfig.getOperationType() == Operation.QuerySingle) {

            int index = r.nextInt(this.workloadConfig.getNumberOfPreCreatedDocuments());
            String pk = (String) docsToRead.get(index).getProperty(partitionKey);
            options.setPartitionKey(new PartitionKey(pk));
            String sqlQuery = "Select * from c where c." + partitionKey + " = \"" + pk + "\"";
            return cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage().last();
        } else if (workloadConfig.getOperationType() == Operation.QueryParallel) {

            String sqlQuery = "Select * from c";
            return cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10).last();
        } else if (workloadConfig.getOperationType() == Operation.QueryOrderby) {

            String sqlQuery = "Select * from c order by c._ts";
            return cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage(10).last();
        } else if (workloadConfig.getOperationType() == Operation.QueryTopOrderby) {

            String sqlQuery = "Select top 1000 * from c order by c._ts";
            return cosmosEncryptionAsyncContainer.queryItems(sqlQuery, options, PojoizedJson.class).byPage().last();
        } else if (workloadConfig.getOperationType() == Operation.ReadAllItemsOfLogicalPartition) {
            throw new IllegalArgumentException("Unsupported Operation on encryption: " + workloadConfig.getOperationType());
        } else {
            throw new IllegalArgumentException("Unsupported Operation: " + workloadConfig.getOperationType());
        }
    }
}
