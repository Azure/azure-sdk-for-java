// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

import java.time.Duration;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * Provides a synchronous service client for accessing a table in the Azure Tables service.
 *
 * The client encapsulates the URL for the table within the Tables service endpoint, the name of the table, and the
 * credentials for accessing the storage or CosmosDB table API account. It provides methods to create and delete the
 * table itself, as well as methods to create, upsert, update, delete, list, and get entities within the table. These
 * methods invoke REST API operations to make the requests and obtain the results that are returned.
 *
 * Instances of this client are obtained by calling the {@link TableClientBuilder#buildClient()} method on a
 * {@link TableClientBuilder} object.
 */
@ServiceClient(builder = TableClientBuilder.class)
public class TableClient {
    final TableAsyncClient client;

    TableClient(TableAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return this.client.getTableName();
    }

    /**
     * Gets the name of the account containing the table.
     *
     * @return The name of the account containing the table.
     */
    public String getAccountName() {
        return this.client.getAccountName();
    }

    /**
     * Gets the absolute URL for this table.
     *
     * @return The absolute URL for this table.
     */
    public String getTableUrl() {
        return this.client.getTableUrl();
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TablesServiceVersion getApiVersion() {
        return this.client.getApiVersion();
    }

    /**
     * Creates the table within the Tables service.
     *
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void create() {
        client.create().block();
    }

    /**
     * Creates the table within the Tables service.
     *
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void create(Duration timeout) {
        blockWithOptionalTimeout(client.create(), timeout);
    }

    /**
     * Creates the table within the Tables service.
     *
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.createWithResponse(context), timeout);
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     * @throws TableServiceErrorException if an entity with the same partition key and row key already exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity entity) {
        client.createEntity(entity).block();
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if an entity with the same partition key and row key already exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity entity, Duration timeout) {
        createEntityWithResponse(entity, timeout, null);
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if an entity with the same partition key and row key already exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createEntityWithResponse(TableEntity entity, Duration timeout, Context context) {
        return client.createEntityWithResponse(entity, timeout, context).block();
    }

    /**
     * Inserts an entity into the table if it does not exist, or merges the entity with the existing entity otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the provided entity's properties will be merged into the existing entity.
     *
     * @param entity The entity to upsert.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity) {
        client.upsertEntity(entity).block();
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity, UpdateMode updateMode) {
        client.upsertEntity(entity, updateMode).block();
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity, UpdateMode updateMode, Duration timeout) {
        upsertEntityWithResponse(entity, updateMode, timeout, null);
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode, Duration timeout,
                                                   Context context) {
        return client.upsertEntityWithResponse(entity, updateMode, timeout, context).block();
    }

    /**
     * Updates an existing entity by merging the provided entity with the existing entity.
     *
     * @param entity The entity to update.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity) {
        client.updateEntity(entity).block();
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode which type of mode to execute
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity, UpdateMode updateMode) {
        client.updateEntity(entity, updateMode).block();
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the eTag of the provided entity must match the eTag of the entity in the Table
     *                    service. If the values do not match, the update will not occur and an exception will be
     *                    thrown.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table,
     *                                    or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does
     *                                    not match that of the provided entity.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        client.updateEntity(entity, updateMode, ifUnchanged).block();
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the eTag of the provided entity must match the eTag of the entity in the Table
     *                    service. If the values do not match, the update will not occur and an exception will be
     *                    thrown.
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table,
     *                                    or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does
     *                                    not match that of the provided entity, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged, Duration timeout) {
        updateEntityWithResponse(entity, updateMode, ifUnchanged, timeout, null);
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the eTag of the provided entity must match the eTag of the entity in the Table
     *                    service. If the values do not match, the update will not occur and an exception will be
     *                    thrown.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table,
     *                                    or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does
     *                                    not match that of the provided entity, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateEntityWithResponse(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged,
                                                   Duration timeout, Context context) {
        return client.updateEntityWithResponse(entity, updateMode, ifUnchanged, timeout, context).block();
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @throws TableServiceErrorException if no table with this name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        client.delete().block();
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if no table with this name exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(Duration timeout) {
        blockWithOptionalTimeout(client.delete(), timeout);
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if no table with this name exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.deleteWithResponse(context), timeout);
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey) {
        client.deleteEntity(partitionKey, rowKey).block();
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     *             the delete will not occur and an exception will be thrown.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if {@code eTag} is not {@code null} and the existing entity's eTag
     *                                    does not match that of the provided entity.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey, String eTag) {
        client.deleteEntity(partitionKey, rowKey, eTag).block();
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     *             the delete will not occur and an exception will be thrown.
     * @param timeout Duration to wait for the operation to complete.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if {@code eTag} is not {@code null} and the existing entity's eTag
     *                                    does not match that of the provided entity, or if the provided timeout
     *                                    expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey, String eTag, Duration timeout) {
        deleteEntityWithResponse(partitionKey, rowKey, eTag, timeout, null);
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     *             the delete will not occur and an exception will be thrown.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if {@code eTag} is not {@code null} and the existing entity's eTag
     *                                    does not match that of the provided entity, or if the provided timeout
     *                                    expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, Duration timeout,
                                                   Context context) {
        return client.deleteEntityWithResponse(partitionKey, rowKey, eTag, timeout, context).block();
    }

    /**
     * Lists all entities within the table.
     *
     * @return A paged iterable containing all entities within the table.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities() {
        return new PagedIterable<>(client.listEntities());
    }

    /**
     * Lists entities using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only entities matching the filter will be returned. If the
     * `select` parameter is set, only the properties included in the select parameter will be returned for each entity.
     * If the `top` parameter is set, the number of returned entities will be limited to that value.
     *
     * @param options The `filter`, `select`, and `top` OData query options to apply to this operation.
     *
     * @return A paged iterable containing matching entities within the table.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedIterable<>(client.listEntities(options));
    }

    /**
     * Lists all entities within the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A paged iterable containing all entities within the table.
     * @throws IllegalArgumentException if an instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedIterable<T> listEntities(Class<T> resultType) {
        return new PagedIterable<>(client.listEntities(resultType));
    }

    /**
     * Lists entities using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only entities matching the filter will be returned. If the
     * `select` parameter is set, only the properties included in the select parameter will be returned for each entity.
     * If the `top` parameter is set, the number of returned entities will be limited to that value.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param options The `filter`, `select`, and `top` OData query options to apply to this operation.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A paged iterable containing matching entities within the table.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed, or if
     *                                  an instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedIterable<T> listEntities(ListEntitiesOptions options, Class<T> resultType) {
        return new PagedIterable<>(client.listEntities(options, resultType));
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey) {
        return client.getEntity(partitionKey, rowKey).block();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if the
     *                                  {@code select} OData query option is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey, String select) {
        return client.getEntity(partitionKey, rowKey, select).block();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if an
     *                                  instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, Class<T> resultType) {
        return client.getEntity(partitionKey, rowKey, resultType).block();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, if the
     *                                  {@code select} OData query option is malformed, or if an instance of the
     *                                  provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, String select, Class<T> resultType) {
        return client.getEntity(partitionKey, rowKey, select, resultType).block();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param timeout Duration to wait for the operation to complete.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if the
     *                                  {@code select} OData query option is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey, String select, Duration timeout) {
        return getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, timeout, null).getValue();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     * @param timeout Duration to wait for the operation to complete.
     *
     * @return The entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, if the
     *                                  {@code select} OData query option is malformed, or if an instance of the
     *                                  provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> T getEntity(String partitionKey, String rowKey, String select, Class<T> resultType,
                                               Duration timeout) {
        return getEntityWithResponse(partitionKey, rowKey, select, resultType, timeout, null).getValue();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if the
     *                                  {@code select} OData query option is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableEntity> getEntityWithResponse(String partitionKey, String rowKey, String select,
                                                       Duration timeout, Context context) {
        return client.getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, timeout, context).block();
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if the provided timeout expires.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, if the
     *                                  {@code select} OData query option is malformed, or if an instance of the
     *                                  provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> Response<T> getEntityWithResponse(String partitionKey, String rowKey, String select,
                                                                     Class<T> resultType, Duration timeout,
                                                                     Context context) {
        return client.getEntityWithResponse(partitionKey, rowKey, select, resultType, timeout, context).block();
    }
}
