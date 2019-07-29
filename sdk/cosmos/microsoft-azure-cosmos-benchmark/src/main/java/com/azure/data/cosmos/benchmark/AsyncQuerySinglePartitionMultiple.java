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

class AsyncQuerySinglePartitionMultiple extends AsyncBenchmark<FeedResponse<Document>> {

    private static final String SQL_QUERY = "Select * from c where c.pk = \"pk\"";
    private FeedOptions options;
    private int pageCount = 0;

    AsyncQuerySinglePartitionMultiple(Configuration cfg) {
        super(cfg);
        options = new FeedOptions();
        options.partitionKey(new PartitionKey("pk"));
        options.maxItemCount(10);
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
        Flux<FeedResponse<Document>> obs = client.queryDocuments(getCollectionLink(), SQL_QUERY, options);

        concurrencyControlSemaphore.acquire();

        obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
    }
}
