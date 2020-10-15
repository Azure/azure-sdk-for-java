// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.Response;
import com.azure.data.tables.models.TableBatchResult;

import java.util.List;

public final class TableBatch extends TableBatchBase {

    TableBatch(String partitionKey, TableAsyncClient client) {
        super(partitionKey, client);
    }

    public List<TableBatchResult> submitTransaction() {
        return null;
    }

    public Response<List<Response<TableBatchResult>>> submitTransactionWithResponse() {
        return null;
    }

}
