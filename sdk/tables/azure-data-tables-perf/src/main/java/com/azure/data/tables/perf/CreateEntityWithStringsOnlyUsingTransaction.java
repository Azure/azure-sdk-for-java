// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.perf;

import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.perf.core.TableTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class CreateEntityWithStringsOnlyUsingTransaction extends TableTestBase<PerfStressOptions> {
    private final List<List<TableTransactionAction>> allTransactionActions;

    public CreateEntityWithStringsOnlyUsingTransaction(PerfStressOptions options) {
        super(options);

        String partitionKey = UUID.randomUUID().toString();
        allTransactionActions = Flux.range(0, options.getCount())
            .map(i ->
                new TableTransactionAction(TableTransactionActionType.UPSERT_MERGE,
                    generateEntityWithAllTypes(partitionKey, Integer.toString(i))))
            .buffer(100)
            .collectList()
            .block();
    }

    public Mono<Void> globalSetupAsync() {
        return tableAsyncClient.createTable()
            .then(super.globalSetupAsync());
    }

    @Override
    public void run() {
        allTransactionActions.forEach(tableClient::submitTransaction);
    }

    @Override
    public Mono<Void> runAsync() {
        return Flux.fromIterable(allTransactionActions)
            .map(tableAsyncClient::submitTransaction)
            .then();
    }
}
