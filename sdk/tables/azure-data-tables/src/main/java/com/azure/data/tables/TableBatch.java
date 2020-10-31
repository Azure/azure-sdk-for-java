// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.models.BatchOperationResponse;
import com.azure.data.tables.models.BatchOperation;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

import java.time.Duration;
import java.util.List;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

@Fluent
public final class TableBatch {
    private final TableAsyncBatch batch;

    TableBatch(TableAsyncBatch batch) {
        this.batch = batch;
    }

    public TableBatch createEntity(TableEntity entity) {
        batch.createEntity(entity);
        return this;
    }

    public TableBatch upsertEntity(TableEntity entity) {
        batch.upsertEntity(entity);
        return this;
    }

    public TableBatch upsertEntity(TableEntity entity, UpdateMode updateMode) {
        batch.upsertEntity(entity, updateMode);
        return this;
    }

    public TableBatch updateEntity(TableEntity entity) {
        batch.updateEntity(entity);
        return this;
    }

    public TableBatch updateEntity(TableEntity entity, UpdateMode updateMode) {
        batch.updateEntity(entity, updateMode);
        return this;
    }

    public TableBatch updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        batch.updateEntity(entity, updateMode, ifUnchanged);
        return this;
    }

    public TableBatch deleteEntity(String rowKey) {
        batch.deleteEntity(rowKey);
        return this;
    }

    public TableBatch deleteEntity(String rowKey, String eTag) {
        batch.deleteEntity(rowKey, eTag);
        return this;
    }

    public List<BatchOperation> getOperations() {
        return batch.getOperations();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<BatchOperationResponse> submitTransaction() {
        return batch.submitTransaction().block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<BatchOperationResponse> submitTransaction(Duration timeout) {
        return blockWithOptionalTimeout(batch.submitTransaction(), timeout);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<BatchOperationResponse>> submitTransactionWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(batch.submitTransactionWithResponse(context), timeout);
    }
}
