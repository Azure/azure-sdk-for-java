// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.perf;

import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.perf.core.TableTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class CreateEntityWithAllTypes extends TableTestBase<PerfStressOptions> {
    private final TableEntity tableEntity;

    public CreateEntityWithAllTypes(PerfStressOptions options) {
        super(options);

        tableEntity = generateEntityWithAllTypes(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public Mono<Void> globalSetupAsync() {
        return tableAsyncClient.createTable()
            .then(super.globalSetupAsync());
    }

    @Override
    public void run() {
        tableClient.upsertEntity(tableEntity);
    }

    @Override
    public Mono<Void> runAsync() {
        return tableAsyncClient.upsertEntity(tableEntity)
            .then();
    }
}
