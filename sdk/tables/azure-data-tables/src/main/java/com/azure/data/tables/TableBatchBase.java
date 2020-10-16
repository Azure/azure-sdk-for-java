// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.implementation.BatchOperation;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

abstract class TableBatchBase {
    private final String partitionKey;
    private final TableAsyncClient client;
    private final HashSet<String> rowKeys = new HashSet<>();

    private List<BatchOperation> operations = new ArrayList<>();
    private boolean frozen = false;

    TableBatchBase(String partitionKey, TableAsyncClient client) {
        this.partitionKey = partitionKey;
        this.client = client;
    }

    public void createEntity(TableEntity entity) {
        validate(entity);
        operations.add(new BatchOperation.CreateEntity(entity));
    }

    public void upsertEntity(TableEntity entity) {
        validate(entity);
        operations.add(new BatchOperation.UpsertEntity(entity, UpdateMode.MERGE));
    }

    public void upsertEntity(TableEntity entity, UpdateMode updateMode) {
        validate(entity);
        operations.add(new BatchOperation.UpsertEntity(entity, updateMode));
    }

    public void updateEntity(TableEntity entity) {
        validate(entity);
        operations.add(new BatchOperation.UpdateEntity(entity, UpdateMode.MERGE, false));
    }

    public void updateEntity(TableEntity entity, UpdateMode updateMode) {
        validate(entity);
        operations.add(new BatchOperation.UpdateEntity(entity, updateMode, false));
    }

    public void updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        validate(entity);
        operations.add(new BatchOperation.UpdateEntity(entity, updateMode, ifUnchanged));
    }

    public void deleteEntity(String rowKey) {
        validate(partitionKey, rowKey);
        operations.add(new BatchOperation.DeleteEntity(partitionKey, rowKey, "*"));
    }

    public void deleteEntity(String rowKey, String eTag) {
        validate(partitionKey, rowKey);
        operations.add(new BatchOperation.DeleteEntity(partitionKey, rowKey, eTag));
    }

    protected List<BatchOperation> getOperations() {
        return operations;
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
