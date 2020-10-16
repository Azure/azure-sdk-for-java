// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.implementation.BatchOperation;
import com.azure.data.tables.models.TableBatchResult;

import java.util.List;

@Fluent
public final class TableBatch extends TableBatchBase<TableBatch> {

    TableBatch(String partitionKey, TableAsyncClient client) {
        super(partitionKey, client);
    }

    @Override
    public synchronized List<TableBatchResult> submitTransaction() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }

    @Override
    public synchronized Response<List<Response<TableBatchResult>>> submitTransactionWithResponse() {
        freeze();

        for (BatchOperation operation : getOperations()) {
            // do something
        }
        return null;
    }

}
