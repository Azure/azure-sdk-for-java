// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.apache.commons.lang3.RandomStringUtils;


import java.util.UUID;

class SyncWriteBenchmark extends SyncBenchmark<CosmosItemResponse> {

    private final String dataFieldValue;
    private final String uuid;

    SyncWriteBenchmark(TenantWorkloadConfig workloadCfg) throws Exception {
        super(workloadCfg);

        uuid = UUID.randomUUID().toString();
        dataFieldValue =
            RandomStringUtils.randomAlphabetic(workloadConfig.getDocumentDataFieldSize());
    }

    @Override
    protected CosmosItemResponse performWorkload(long i) throws Exception {
        String id = uuid + i;
        CosmosItemResponse<PojoizedJson> response;
        if (workloadConfig.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            return cosmosContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                workloadConfig.getDocumentDataFieldCount()));
        }

        // more optimized for write as partition key is already passed as config
        return cosmosContainer.createItem(BenchmarkHelper.generateDocument(id,
            dataFieldValue,
            partitionKey,
            workloadConfig.getDocumentDataFieldCount()),
            new PartitionKey(id),
            null);
    }
}
