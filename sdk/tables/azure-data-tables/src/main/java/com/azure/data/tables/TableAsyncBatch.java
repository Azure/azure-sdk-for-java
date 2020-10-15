// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.Response;
import com.azure.data.tables.models.TableBatchResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class TableAsyncBatch extends TableBatchBase {

    TableAsyncBatch(String partitionKey, TableAsyncClient client) {
        super(partitionKey, client);
    }

    public Flux<TableBatchResult> submitTransaction() {
        return null;
    }

    public Mono<Response<Flux<Response<TableBatchResult>>>> submitTransactionWithResponse() {
        return null;
    }
}
