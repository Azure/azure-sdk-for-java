// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

abstract class TableBatchBase {
    private String partitionKey;
    private TableAsyncClient client;

    TableBatchBase(String partitionKey, TableAsyncClient client) {
        this.partitionKey = partitionKey;
        this.client = client;
    }

    public void createEntity(TableEntity entity) {
    }

    public void upsertEntity(TableEntity entity) {
    }

    public void upsertEntity(TableEntity entity, UpdateMode updateMode) {
    }

    public void updateEntity(TableEntity entity) {
    }

    public void updateEntity(TableEntity entity, UpdateMode updateMode) {
    }

    public void updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
    }

    public void deleteEntity(String rowKey) {
    }

    public void deleteEntity(String rowKey, String eTag) {
    }

}
