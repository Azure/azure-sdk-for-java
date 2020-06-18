// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;

import java.util.List;
import java.util.Map;

@ServiceClient(
    builder = TableClientBuilder.class)
public class TableClient {
    String tableName;

    TableClient(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Queries and returns entities in the given table using the select and filter strings
     * @param tableName name of table to query
     * @param selectString odata select string
     * @param filterString odata filter string
     * @return 
     */
    public List<TableEntity> queryEntity(String tableName, String selectString, String filterString) {
        return null;
    }

    public TableEntity insertEntity(String row, String partition, Map<String, Object> tableEntityProperties) {
        return new TableEntity();
    }

    /**
     *
     * @param tableEntity
     * @return
     */
    public TableEntity insertEntity(TableEntity tableEntity) {
        return tableEntity;
    }

    public void deleteEntity(TableEntity tableEntity) {
    }

    public void updateEntity(TableEntity te) {
    }

    public void updateAndReplaceEntity(TableEntity tableEntity) {
    }

    public void updateAndMergeEntity(TableEntity tableEntity) {
    }
}
