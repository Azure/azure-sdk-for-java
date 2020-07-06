// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;
import java.util.List;
import java.util.Map;

/**
 * sync client for table operations
 */
@ServiceClient(
    builder = TableClientBuilder.class)
public class TableClient {
    final String tableName;

    TableClient(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Queries and returns entities in the given table using the select and filter strings
     *
     * @param queryOptions the odata query object
     * @return a list of the tables that fit the query
     */
    public List<TableEntity> queryEntity(QueryOptions queryOptions) {
        return null;
    }

    /**
     * Queries and returns entities in the given table with the given rowKey and ParitionKey
     *
     * @param rowKey the given row key
     * @param partitionKey the given partition key
     * @return a list of the tables that fit the row and partition key
     */
    public List<TableEntity> queryEntitiesWithPartitionAndRowKey(String rowKey, String partitionKey) {
        return null;
    }


    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntityProperties a map of properties for the TableEntity
     * @return the created TableEntity
     */
    public TableEntity createEntity(Map<String, Object> tableEntityProperties) {
        return null;
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param tableEntity entity to upsert
     */
    public void upsertEntity(UpdateMode updateMode, TableEntity tableEntity) {
    }

    /**
     * based on Mode it either updates or fails if it does exists or replaces or fails if it does exists
     *
     * @param updateMode type of update
     * @param tableEntity entity to update
     */
    public void updateEntity(UpdateMode updateMode, TableEntity tableEntity) {
    }

    /**
     * deletes the given entity
     *
     * @param tableEntity entity to delete
     */
    public void deleteEntity(TableEntity tableEntity) {
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     */
    public void deleteEntity(String partitionKey, String rowKey) {
    }

    /**
     * returns the table name associated with the client
     *
     * @return table name
     */
    public String getTableName() {
        return this.tableName;
    }


}
