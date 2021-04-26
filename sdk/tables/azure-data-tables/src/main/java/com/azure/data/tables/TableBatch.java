// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.models.BatchOperation;
import com.azure.data.tables.models.BatchOperationResponse;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceErrorException;
import com.azure.data.tables.models.UpdateMode;

import java.time.Duration;
import java.util.List;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * Provides a batch object for asynchronously executing a transaction containing one or more operations on entities
 * within a table in the Azure Tables service.
 *
 * The batch object represents a collection of one or more create, update, upsert, and/or delete operations on entities
 * that share the same partition key within the table. When the batch is executed, all of the operations will be
 * performed as part of a single transaction. As a result, either all operations in the batch will succeed, or if a
 * failure occurs, all operations in the batch will be rolled back. Each operation in a batch must operate on a distinct
 * row key. Attempting to add multiple operations to a batch that share the same row key will cause an exception to be
 * thrown.
 *
 * Instances of this object are obtained by calling the {@link TableClient#createBatch(String)} method on a
 * {@link TableClient} object.
 */
@Fluent
public final class TableBatch {
    private final TableAsyncBatch batch;

    TableBatch(TableAsyncBatch batch) {
        this.batch = batch;
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch createEntity(TableEntity entity) {
        batch.createEntity(entity);

        return this;
    }

    /**
     * Inserts an entity into the table if it does not exist, or merges the entity with the existing entity otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will be
     * inserted. Otherwise, the provided entity's properties will be merged into the existing entity.
     *
     * @param entity The entity to upsert.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch upsertEntity(TableEntity entity) {
        batch.upsertEntity(entity);

        return this;
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will be
     * inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch upsertEntity(TableEntity entity, UpdateMode updateMode) {
        batch.upsertEntity(entity, updateMode);

        return this;
    }

    /**
     * Updates an existing entity by merging the provided entity with the existing entity.
     *
     * @param entity The entity to update.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch updateEntity(TableEntity entity) {
        batch.updateEntity(entity);

        return this;
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
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch updateEntity(TableEntity entity, UpdateMode updateMode) {
        batch.updateEntity(entity, updateMode);

        return this;
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
     * service. If the values do not match, the update will not occur and an exception will be thrown.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        batch.updateEntity(entity, updateMode, ifUnchanged);

        return this;
    }

    /**
     * Deletes an entity from the table.
     *
     * @param rowKey The row key of the entity.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the provided row key is {@code null} or empty, or if another operation with
     * the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch deleteEntity(String rowKey) {
        batch.deleteEntity(rowKey);

        return this;
    }

    /**
     * Deletes an entity from the table.
     *
     * @param rowKey The row key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     * the delete will not occur and an exception will be thrown.
     *
     * @return The updated {@link TableBatch}.
     *
     * @throws IllegalArgumentException If the provided row key is {@code null} or empty, or if another operation with
     * the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableBatch deleteEntity(String rowKey, String eTag) {
        batch.deleteEntity(rowKey, eTag);

        return this;
    }

    /**
     * Gets an immutable list containing all operations added to this batch.
     *
     * @return An immutable list containing all operations added to this batch.
     */
    public List<BatchOperation> getOperations() {
        return batch.getOperations();
    }

    /**
     * Executes all operations within The batch inside a transaction. When the call completes, either all operations in
     * the batch will succeed, or if a failure occurs, all operations in the batch will be rolled back.
     *
     * @return A list of sub-responses for each operation in the batch.
     *
     * @throws IllegalStateException If no operations have been added to the batch.
     * @throws TableServiceErrorException if any operation within the batch fails. See the documentation for the client
     * methods in {@link TableClient} to understand the conditions that may cause a given operation to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<BatchOperationResponse> submitTransaction() {
        return batch.submitTransaction().block();
    }

    /**
     * Executes all operations within The batch inside a transaction. When the call completes, either all operations in
     * the batch will succeed, or if a failure occurs, all operations in the batch will be rolled back.
     *
     * @param timeout Duration to wait for the operation to complete.
     *
     * @return A list of sub-responses for each operation in the batch.
     *
     * @throws IllegalStateException If no operations have been added to the batch.
     * @throws TableServiceErrorException if any operation within the batch fails. See the documentation for the client
     * methods in {@link TableClient} to understand the conditions that may cause a given operation to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<BatchOperationResponse> submitTransaction(Duration timeout) {
        return blockWithOptionalTimeout(batch.submitTransaction(), timeout);
    }

    /**
     * Executes all operations within The batch inside a transaction. When the call completes, either all operations in
     * the batch will succeed, or if a failure occurs, all operations in the batch will be rolled back.
     *
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The HTTP response produced for the batch itself. The response's value will contain a list of
     * sub-responses for each operation in the batch.
     *
     * @throws IllegalStateException If no operations have been added to the batch.
     * @throws TableServiceErrorException if any operation within the batch fails. See the documentation for the client
     * methods in {@link TableClient} to understand the conditions that may cause a given operation to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<BatchOperationResponse>> submitTransactionWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(batch.submitTransactionWithResponse(context), timeout);
    }
}
