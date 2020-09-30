// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

import java.time.Duration;

/**
 * sync client for table operations
 */
@ServiceClient(
    builder = TableClientBuilder.class)
public class TableClient {
    final TableAsyncClient client;

    TableClient(TableAsyncClient client) {
        this.client = client;
    }

    /**
     * returns the table name associated with the client*
     *
     * @return table name
     */
    public String getTableName() {
        return this.client.getTableName();
    }

    /**
     * returns the account for this table
     *
     * @return a string of the account name
     */
    public String getAccountName() {
        return this.client.getAccountName();
    }

    /**
     * returns Url of this table
     *
     * @return Url
     */
    public String getTableUrl() {
        return this.client.getTableUrl();
    }

    /**
     * returns the version
     *
     * @return the version
     */
    public TablesServiceVersion getApiVersion() {
        return this.client.getApiVersion();
    }

    /**
     * creates new table with the name of this client
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void create() {
        client.create().block();
    }

    /**
     * creates new table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void create(Duration timeout) {
        client.create().block(timeout);
    }

    /**
     * creates a new table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createWithResponse(Duration timeout, Context context) {
        return client.createWithResponse(context).block(timeout);
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity tableEntity) {
        client.createEntity(tableEntity).block();
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     * @param timeout max time for query to execute before erroring out
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity tableEntity, Duration timeout) {
        createEntityWithResponse(tableEntity, timeout, null).getValue();
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include
     * rowKey and partitionKey
     *
     * @param tableEntity the entity to add
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createEntityWithResponse(TableEntity tableEntity, Duration timeout, Context context) {
        return client.createEntityWithResponse(tableEntity, context).block(timeout);
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param entity entity to upsert
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity) {
        client.upsertEntity(entity).block();
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param entity entity to upsert
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity, UpdateMode updateMode) {
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
    public void upsertEntity(TableEntity entity, UpdateMode updateMode, Duration timeout) {
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
    public Response<Void> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode, Duration timeout,
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
    public void updateEntity(TableEntity entity) {
        client.updateEntity(entity).block();
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or
     * fails if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity, UpdateMode updateMode) {
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
    public void updateEntity(TableEntity entity, boolean ifUnchanged, UpdateMode updateMode) {
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
    public void updateEntity(TableEntity entity, boolean ifUnchanged, UpdateMode updateMode, Duration timeout) {
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
    public Response<Void> updateEntityWithResponse(TableEntity entity, boolean ifUnchanged, UpdateMode updateMode,
                                                   Duration timeout, Context context) {
        return client.updateEntityWithResponse(entity, ifUnchanged, updateMode, timeout, context).block();
    }

    /**
     * deletes the table with the name of this client
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        client.delete().block();
    }

    /**
     * deletes the table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(Duration timeout) {
        client.delete().block(timeout);
    }

    /**
     * deletes the table with the name of this client
     *
     * @param timeout Duration to wait for operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        return client.deleteWithResponse(context).block(timeout);
    }

    /**
     * deletes the entity with the given partition key and row key
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey) {
        client.deleteEntity(partitionKey, rowKey).block();
    }

    /**
     * deletes the entity with the given partition key and row key
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @param eTag the eTag of the entity, the delete will only occur if this matches the entity in the service
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey, String eTag) {
        client.deleteEntity(partitionKey, rowKey, eTag).block();
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @param eTag the eTag of the entity, the delete will only occur if this matches the entity in the service
     * @param timeout max time for query to execute before erroring out
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey, String eTag, Duration timeout) {
        deleteEntityWithResponse(partitionKey, rowKey, eTag, timeout, null);
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @param eTag the eTag of the entity, the delete will only occur if this matches the entity in the service
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, Duration timeout,
                                                   Context context) {
        return client.deleteEntityWithResponse(partitionKey, rowKey, eTag, timeout, context).block();
    }

    /**
     * Queries and returns all entities in the given table
     *
     * @return a list of all the entities which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities() {
        return new PagedIterable<>(client.listEntities());
    }

    /**
     * Queries and returns entities in the given table using the odata QueryOptions
     *
     * @param options the odata query object
     * @return a list of all the entities which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedIterable<>(client.listEntities(options));
    }

    /**
     * Queries and returns all entities in the given table
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     * @return a list of all the entities which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedIterable<T> listEntities(Class<T> resultType) {
        return new PagedIterable<>(client.listEntities(resultType));
    }

    /**
     * Queries and returns entities in the given table using the odata QueryOptions
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param options the odata query object
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     * @return a list of all the entities which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedIterable<T> listEntities(ListEntitiesOptions options, Class<T> resultType) {
        return new PagedIterable<>(client.listEntities(options, resultType));
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @return the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey) {
        return client.getEntity(partitionKey, rowKey).block();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param select a select expression using OData notation. Limits the columns on each record to just those
     *               requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     * @return the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey, String select) {
        return client.getEntity(partitionKey, rowKey, select).block();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     *
     * @return the table entity subclass specified in resultType
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, Class<T> resultType) {
        return client.getEntity(partitionKey, rowKey, resultType).block();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param select a select expression using OData notation. Limits the columns on each record to just those
     *               requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     *
     * @return the table entity subclass specified in resultType
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, String select, Class<T> resultType) {
        return client.getEntity(partitionKey, rowKey, select, resultType).block();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param select a select expression using OData notation. Limits the columns on each record to just those
     *               requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     * @param timeout max time for query to execute before erroring out
     * @return the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey, String select, Duration timeout) {
        return getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, timeout, null).getValue();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param select a select expression using OData notation. Limits the columns on each record to just those
     *               requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     * @param timeout max time for query to execute before erroring out
     * @return the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, String select, Class<T> resultType,
                                               Duration timeout) {
        return getEntityWithResponse(partitionKey, rowKey, select, resultType, timeout, null).getValue();
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param <T> the type of the result value, which must be a subclass of TableEntity
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     * @param select a select expression using OData notation. Limits the columns on each record to just those
     *               requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     * @param resultType the type of the result value, which must be a subclass of TableEntity
     * @param timeout max time for query to execute before erroring out
     * @param context the context of the query
     * @return a mono of the response with the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> Response<T> getEntityWithResponse(String partitionKey, String rowKey, String select,
                                                                     Class<T> resultType, Duration timeout,
                                                                     Context context) {
        return client.getEntityWithResponse(partitionKey, rowKey, select, resultType, timeout, context).block();
    }

}
