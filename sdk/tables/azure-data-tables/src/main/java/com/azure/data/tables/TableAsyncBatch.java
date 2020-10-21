// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.models.BatchOperation;
import reactor.core.publisher.Mono;

import java.util.List;

@Fluent
public final class TableAsyncBatch extends TableBatchBase<TableAsyncBatch> {

    TableAsyncBatch(String partitionKey, TableAsyncClient client) {
        super(partitionKey, client);
    }

    @Override
    public synchronized Mono<Void> submitTransaction() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }

    @Override
    public synchronized Mono<Response<List<Response<Void>>>> submitTransactionWithResponse() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }
}
