// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.perf;

import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.perf.core.TableTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ListEntitiesWithStringsOnlyTest extends TableTestBase<PerfStressOptions> {
    private final String partitionKey;

    public ListEntitiesWithStringsOnlyTest(PerfStressOptions options) {
        super(options);

        partitionKey = UUID.randomUUID().toString();
    }

    public Mono<Void> globalSetupAsync() {
        return tableAsyncClient.createTable()
            .then(super.globalSetupAsync())
            .then(Flux.range(0, options.getCount())
                .map(i ->
                    new TableTransactionAction(TableTransactionActionType.UPSERT_MERGE,
                        generateEntityWithStringsOnly(partitionKey, Integer.toString(i))))
                .buffer(100)
                .flatMap(tableAsyncClient::submitTransaction)
                .then());
    }

    @Override
    public void run() {
        tableClient.listEntities().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        return tableAsyncClient.listEntities()
            .then();
    }
}
