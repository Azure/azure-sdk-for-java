// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;


class SyncReadBenchmark extends SyncBenchmark<CosmosItemResponse> {

    SyncReadBenchmark(TenantWorkloadConfig workloadCfg) throws Exception {
        super(workloadCfg);
    }

    @Override
    protected CosmosItemResponse performWorkload(long i) throws Exception {
        int index = (int) (i % docsToRead.size());
        PojoizedJson doc = docsToRead.get(index);

        String partitionKeyValue = doc.getId();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setExcludedRegions(workloadConfig.getExcludedRegionsList());
        return cosmosContainer.readItem(doc.getId(), new PartitionKey(partitionKeyValue),
                                        options, InternalObjectNode.class);
    }
}
