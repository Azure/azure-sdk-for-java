// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Random;

class AsyncQueryBenchmark extends AsyncBenchmark<FeedResponse<Document>> {

    private int pageCount = 0;

    AsyncQueryBenchmark(Configuration cfg) {
        super(cfg);
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
    protected void performWorkload(BaseSubscriber<FeedResponse<Document>> baseSubscriber, long i) throws InterruptedException {

        Flux<FeedResponse<Document>> obs;
        Random r = new Random();
        FeedOptions options = new FeedOptions();

        if (configuration.getOperationType() == Configuration.Operation.QueryCross) {

            int index = r.nextInt(1000);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c where c._rid = \"" + docsToRead.get(index).resourceId() + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QuerySingle) {

            int index = r.nextInt(1000);
            String pk = docsToRead.get(index).getString("pk");
            options.partitionKey(new PartitionKey(pk));
            String sqlQuery = "Select * from c where c.pk = \"" + pk + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryParallel) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryOrderby) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryAggregate) {

            options.maxItemCount(10);
            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select value max(c._ts) from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryAggregateTopOrderby) {

            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1 value count(c) from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryTopOrderby) {

            options.enableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1000 * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else {
            throw new IllegalArgumentException("Unsupported Operation: " + configuration.getOperationType());
        }
        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
    }
}
