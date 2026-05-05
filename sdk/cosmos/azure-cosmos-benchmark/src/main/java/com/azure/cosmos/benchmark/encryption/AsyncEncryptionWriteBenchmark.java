// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.UUID;

public class AsyncEncryptionWriteBenchmark extends AsyncEncryptionBenchmark<CosmosItemResponse<PojoizedJson>> {

    private final String uuid;
    private final String dataFieldValue;

    public AsyncEncryptionWriteBenchmark(TenantWorkloadConfig workloadCfg, Scheduler scheduler) throws IOException {
        super(workloadCfg, scheduler);
        uuid = UUID.randomUUID().toString();
        dataFieldValue = RandomStringUtils.randomAlphabetic(workloadConfig.getDocumentDataFieldSize());
    }

    @Override
    protected Mono<CosmosItemResponse<PojoizedJson>> performWorkload(long i) {
        String id = uuid + i;
        PojoizedJson newDoc = BenchmarkHelper.generateDocument(id,
            dataFieldValue,
            partitionKey,
            workloadConfig.getDocumentDataFieldCount());
        for (int j = 1; j <= workloadConfig.getEncryptedStringFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_STRING_FIELD + j, uuid);
        }
        for (int j = 1; j <= workloadConfig.getEncryptedLongFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_LONG_FIELD + j, 1234l);
        }
        for (int j = 1; j <= workloadConfig.getEncryptedDoubleFieldCount(); j++) {
            newDoc.setProperty(ENCRYPTED_DOUBLE_FIELD + j, 1234.01d);
        }

        Mono<CosmosItemResponse<PojoizedJson>> obs;
        if (workloadConfig.isDisablePassingPartitionKeyAsOptionOnWrite()) {
            obs = cosmosEncryptionAsyncContainer.createItem(newDoc, new PartitionKey(id),
                new CosmosItemRequestOptions());
        } else {
            obs = cosmosEncryptionAsyncContainer.createItem(newDoc,
                new PartitionKey(id),
                null);
        }

        return obs;
    }
}
