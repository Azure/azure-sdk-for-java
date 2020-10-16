// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.BatchOperation;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Fluent
abstract class TableBatchBase<T extends TableBatchBase<?>> {
    private final String partitionKey;
    private final TableAsyncClient client;
    private final HashSet<String> rowKeys = new HashSet<>();

    private List<BatchOperation> operations = new ArrayList<>();
    private boolean frozen = false;

    TableBatchBase(String partitionKey, TableAsyncClient client) {
        this.partitionKey = partitionKey;
        this.client = client;
    }

    public abstract Object submitTransaction();
    public abstract Object submitTransactionWithResponse();

    @SuppressWarnings("unchecked")
    public T createEntity(TableEntity entity) {
        validate(entity);
        operations.add(new BatchOperation.CreateEntity(entity));
        return (T)this;
    }

    public T upsertEntity(TableEntity entity) {
        return upsertEntity(entity, UpdateMode.MERGE);
    }

    @SuppressWarnings("unchecked")
    public T upsertEntity(TableEntity entity, UpdateMode updateMode) {
        validate(entity);
        operations.add(new BatchOperation.UpsertEntity(entity, updateMode));
        return (T)this;
    }

    public T updateEntity(TableEntity entity) {
        return updateEntity(entity, UpdateMode.MERGE);
    }

    public T updateEntity(TableEntity entity, UpdateMode updateMode) {
        return updateEntity(entity, updateMode, false);
    }

    @SuppressWarnings("unchecked")
    public T updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        validate(entity);
        operations.add(new BatchOperation.UpdateEntity(entity, updateMode, ifUnchanged));
        return (T)this;
    }

    protected T deleteEntity(String rowKey) {
        return deleteEntity(rowKey, "*");
    }

    @SuppressWarnings("unchecked")
    protected T deleteEntity(String rowKey, String eTag) {
        validate(partitionKey, rowKey);
        operations.add(new BatchOperation.DeleteEntity(partitionKey, rowKey, eTag));
        return (T)this;
    }

    protected List<BatchOperation> getOperations() {
        return operations;
    }

    protected TableAsyncClient getClient() {
        return client;
    }

    protected void freeze() {
        this.frozen = true;
        this.operations = Collections.unmodifiableList(this.operations);
    }

    private void validate(TableEntity entity) {
        validate(entity.getPartitionKey(), entity.getRowKey());
    }

    private void validate(String partitionKey, String rowKey) {
        if (this.frozen) {
            throw new IllegalStateException("Operations can't be modified once a batch is submitted.");
        }

        if (!this.partitionKey.equals(partitionKey)) {
            throw new IllegalArgumentException("All operations in a batch must share the same partition key.");
        }

        if (rowKeys.contains(rowKey)) {
            throw new IllegalArgumentException("Every operation in a batch must use a different row key.");
        } else {
            rowKeys.add(rowKey);
        }
    }
}
