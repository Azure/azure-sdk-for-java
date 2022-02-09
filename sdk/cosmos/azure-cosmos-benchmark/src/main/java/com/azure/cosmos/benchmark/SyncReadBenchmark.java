// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

class SyncReadBenchmark extends SyncBenchmark<CosmosItemResponse> {

    SyncReadBenchmark(Configuration cfg) throws Exception {
        super(cfg);
    }

    @Override
    protected CosmosItemResponse performWorkload(long i) throws Exception {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);

        String partitionKeyValue = doc.getId();
        return cosmosContainer.readItem(doc.getId(), new PartitionKey(partitionKeyValue),
                                        new CosmosItemRequestOptions(), InternalObjectNode.class);
    }
}
