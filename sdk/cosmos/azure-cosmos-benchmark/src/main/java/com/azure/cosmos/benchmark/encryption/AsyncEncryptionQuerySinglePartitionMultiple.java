// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

public class AsyncEncryptionQuerySinglePartitionMultiple extends AsyncEncryptionBenchmark<FeedResponse<PojoizedJson>> {

    private static final String SQL_QUERY = "Select * from c where c.pk = \"pk\"";
    private CosmosQueryRequestOptions options;
    private int pageCount = 0;

    public AsyncEncryptionQuerySinglePartitionMultiple(Configuration cfg) throws IOException, MicrosoftDataEncryptionException {
        super(cfg);
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
    protected void performWorkload(BaseSubscriber<FeedResponse<PojoizedJson>> baseSubscriber, long i) throws InterruptedException {
        CosmosPagedFlux<PojoizedJson> obs = cosmosEncryptionAsyncContainer.queryItems(SQL_QUERY, options, PojoizedJson.class);

        concurrencyControlSemaphore.acquire();

        obs.byPage(10).subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
    }
}
