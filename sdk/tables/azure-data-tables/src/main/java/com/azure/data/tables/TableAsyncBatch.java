// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.implementation.BatchOperation;
import com.azure.data.tables.models.TableBatchResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Fluent
public final class TableAsyncBatch extends TableBatchBase<TableAsyncBatch> {

    TableAsyncBatch(String partitionKey, TableAsyncClient client) {
        super(partitionKey, client);
    }

    @Override
    public synchronized Flux<TableBatchResult> submitTransaction() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }

    @Override
    public synchronized Mono<Response<Flux<Response<TableBatchResult>>>> submitTransactionWithResponse() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }
}
