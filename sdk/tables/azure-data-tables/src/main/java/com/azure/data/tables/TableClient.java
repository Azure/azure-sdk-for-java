// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.models.Entity;
import com.azure.data.tables.models.QueryParams;
import com.azure.data.tables.models.Table;
import com.azure.data.tables.models.UpdateMode;
import java.time.Duration;

/**
 * sync client for table operations
 */
@ServiceClient(
    builder = TableClientBuilder.class)
public class TableClient {
    final String tableName;
    final TableAsyncClient client;

    TableClient(String tableName, TableAsyncClient client) {
        this.tableName = tableName;
        this.client = client;
    }

    /**
     * returns the table name associated with the client*
     *
     * @return table name
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * returns the account for this table
     *
     * @return a string of the account name
     */
    public String getAccountName() {
        return null;
    }

    /**
     * returns Url of this service
     *
     * @return Url
     */
    public String getTableUrl() {
        return null;
    }

    /**
     * returns the version
     *
     * @return the version
     */
    public TablesServiceVersion getApiVersion() {
        return null;
    }

    /**
     * creates new table with the name of this client
     *
     * @return a table
     */
    public Table create() {
        return client.create().block();
    }

    /**
     * creates new table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     * @return a table
     */
    public Table create(Duration timeout) {
        return client.create().block(timeout);
    }

    /**
     * creates a new table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return HTTP response containing the created table.
     */
    public Response<Table> createWithResponse(Duration timeout, Context context) {
        return client.createWithResponse(context).block(timeout);
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     * @return the created TableEntity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Entity createEntity(Entity tableEntity) {
        return client.createEntity(tableEntity).block();
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     * @param timeout max time for query to execute before erroring out
     * @return the created TableEntity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Entity createEntity(Entity tableEntity, Duration timeout) {
        return createEntityWithResponse(tableEntity, timeout, null).getValue();
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return the created TableEntity in a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Entity> createEntityWithResponse(Entity tableEntity, Duration timeout, Context context) {
        return client.createEntityWithResponse(tableEntity, context).block(timeout);
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param entity entity to upsert
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(Entity entity) {
        client.upsertEntity(entity).block();
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param entity entity to upsert
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(Entity entity, UpdateMode updateMode) {
        client.upsertEntity(entity, updateMode).block();
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param entity entity to upsert
     * @param timeout max time for query to execute before erroring out
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(Entity entity, UpdateMode updateMode, Duration timeout) {
        upsertEntityWithResponse(entity, updateMode, timeout, null).getValue();
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param entity entity to upsert
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> upsertEntityWithResponse(Entity entity, UpdateMode updateMode, Duration timeout,
        Context context) {
        return client.upsertEntityWithResponse(entity, updateMode, timeout, context).block();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param entity the entity to update
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(Entity entity) {
        client.upsertEntity(entity).block();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(Entity entity, UpdateMode updateMode) {
        client.updateEntity(entity, updateMode).block();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(Entity entity, boolean ifUnchanged, UpdateMode updateMode) {
        client.updateEntity(entity, ifUnchanged, updateMode).block();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     * @param timeout max time for query to execute before erroring out
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(Entity entity, boolean ifUnchanged, UpdateMode updateMode, Duration timeout) {
        updateEntityWithResponse(entity, ifUnchanged, updateMode, timeout, null).getValue();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateEntityWithResponse(Entity entity, boolean ifUnchanged, UpdateMode updateMode,
        Duration timeout, Context context) {
        return client.updateEntityWithResponse(entity, ifUnchanged, updateMode, timeout, context).block();
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(Entity entity) {
        client.deleteEntity(entity).block();
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(Entity entity, boolean ifUnchanged) {
        client.deleteEntity(entity, ifUnchanged).block();
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     * @param timeout max time for query to execute before erroring out
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(Entity entity, boolean ifUnchanged, Duration timeout) {
        deleteEntityWithResponse(entity, ifUnchanged, timeout, null);
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEntityWithResponse(Entity entity, boolean ifUnchanged, Duration timeout,
        Context context) {
        return client.deleteEntityWithResponse(entity, ifUnchanged, timeout, context).block();
    }

    /**
     * Queries and returns entities in the given table using the odata QueryOptions
     *
     * @return a list of the tables that fit the query
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> listEntities() {
        return new PagedIterable<>(client.listEntities());
    }

    /**
     * Queries and returns entities in the given table using the odata QueryOptions
     *
     * @param queryOptions the odata query object
     * @return a list of the tables that fit the query
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> listEntities(QueryParams queryOptions) {
        return new PagedIterable<>(client.listEntities(queryOptions));
    }

    /**
     * Queries and returns entities in the given table using the odata QueryOptions
     *
     * @param queryOptions the odata query object
     * @param timeout max time for query to execute before erroring out
     * @return a list of the tables that fit the query
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Entity> listEntities(QueryParams queryOptions, Duration timeout) {
        return null;
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @return the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Entity getEntity(String partitionKey, String rowKey) {
        return client.getEntity(partitionKey, rowKey).block();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param context the context of the query
     * @return a mono of the response with the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Entity> getEntityWithResponse(String partitionKey, String rowKey, Context context) {
        return client.getEntityWithResponse(partitionKey, rowKey, new QueryOptions(), context).block();
    }

}
