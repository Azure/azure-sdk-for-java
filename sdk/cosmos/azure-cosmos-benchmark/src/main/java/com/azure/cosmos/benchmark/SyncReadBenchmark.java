// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.CosmosItemResponse;

class SyncReadBenchmark extends SyncBenchmark<CosmosItemResponse> {

    SyncReadBenchmark(Configuration cfg) throws Exception {
        super(cfg);
    }

    @Override
    protected CosmosItemResponse performWorkload(long i) throws Exception {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);

        String partitionKeyValue = doc.getId();
        return cosmosContainer.getItem(doc.getId(), partitionKeyValue).read(new CosmosItemRequestOptions(partitionKeyValue));
    }
}
