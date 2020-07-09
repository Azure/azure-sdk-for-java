// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedFlux;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * class for the table async client
 */
@ServiceClient(
    builder = TableClientBuilder.class,
    isAsync = true)
public class TableAsyncClient {
    private final String tableName;

    TableAsyncClient(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Queries and returns entities in the given table using the select and filter strings
     *
     * @param queryOptions the odata query object
     * @return a paged flux of all the entity which fit this criteria
     */
    public PagedFlux<TableEntity> queryEntities(QueryOptions queryOptions) {
        return null;
    }

    /**
     * returns the entity with the given rowKey and ParitionKey
     *
     * @param rowKey the given row key
     * @param partitionKey the given partition key
     * @return an entity that fits the criteria
     */
    public Mono<TableEntity> get(String rowKey, String partitionKey) {
        return null;
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntityProperties a map of properties for the TableEntity
     * @return the created TableEntity
     */
    public Mono<TableEntity> createEntity(Map<String, Object> tableEntityProperties) {
        return Mono.empty();
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param tableEntity entity to upsert
     * @return void
     */
    public Mono<Void> upsertEntity(UpdateMode updateMode, TableEntity tableEntity) {
        return Mono.empty();
    }

    /**
     * based on Mode it either updates or fails if it does exists or replaces or fails if it does exists
     *
     * @param updateMode type of update
     * @param tableEntity entity to update
     * @return void
     */
    public Mono<Void> updateEntity(UpdateMode updateMode, TableEntity tableEntity) {
        return Mono.empty();
    }

    /**
     * deletes the given entity
     *
     * @param tableEntity entity to delete
     * @return void
     */
    public Mono<Void> deleteEntity(TableEntity tableEntity) {
        return Mono.empty();
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @return void
     */
    public Mono<Void> deleteEntity(String partitionKey, String rowKey) {
        return Mono.empty();
    }

    /**
     * returns the table name associated with the client
     *
     * @return table name
     */
    public Mono<String> getTableName() {
        return Mono.empty();
    }
}
