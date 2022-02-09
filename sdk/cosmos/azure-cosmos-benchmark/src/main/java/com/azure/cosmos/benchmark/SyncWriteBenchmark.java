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

    SyncWriteBenchmark(Configuration cfg) throws Exception {
        super(cfg);

        uuid = UUID.randomUUID().toString();
        dataFieldValue =
            RandomStringUtils.randomAlphabetic(configuration.getDocumentDataFieldSize());
    }

    @Override
    protected CosmosItemResponse performWorkload(long i) throws Exception {
        String id = uuid + i;
        CosmosItemResponse<PojoizedJson> response;
        if (configuration.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            // require parsing partition key from the doc
            return cosmosContainer.createItem(BenchmarkHelper.generateDocument(id,
                dataFieldValue,
                partitionKey,
                configuration.getDocumentDataFieldCount()));
        }

        // more optimized for write as partition key is already passed as config
        return cosmosContainer.createItem(BenchmarkHelper.generateDocument(id,
            dataFieldValue,
            partitionKey,
            configuration.getDocumentDataFieldCount()),
            new PartitionKey(id),
            null);
    }
}
