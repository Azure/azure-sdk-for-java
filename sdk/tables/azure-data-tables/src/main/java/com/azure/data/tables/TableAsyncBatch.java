// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.BatchImpl;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.TablesMultipartSerializer;
import com.azure.data.tables.implementation.models.BatchChangeSet;
import com.azure.data.tables.implementation.models.BatchOperation;
import com.azure.data.tables.implementation.models.BatchSubmitBatchResponse;
import com.azure.data.tables.models.BatchOperationResponse;
import com.azure.data.tables.implementation.models.BatchRequestBody;
import com.azure.data.tables.implementation.models.BatchSubRequest;
import com.azure.data.tables.implementation.models.TableServiceError;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceErrorException;
import com.azure.data.tables.models.UpdateMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableUtils.toTableServiceError;

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
 * Instances of this object are obtained by calling the {@link TableAsyncClient#createBatch(String)} method on a {@link
 * TableAsyncClient} object.
 */
@Fluent
public final class TableAsyncBatch {
    private static final TablesMultipartSerializer BATCH_SERIALIZER = new TablesMultipartSerializer();

    private final ClientLogger logger = new ClientLogger(TableAsyncBatch.class);
    private final String partitionKey;
    private final TableAsyncClient operationClient;
    private final BatchImpl batchImpl;
    private final HashSet<String> rowKeys = new HashSet<>();
    private final List<BatchOperation> operations = new ArrayList<>();
    private boolean frozen = false;

    TableAsyncBatch(String partitionKey, TableAsyncClient client) {
        this.partitionKey = partitionKey;
        this.batchImpl = new BatchImpl(client.getImplementation(), BATCH_SERIALIZER);
        this.operationClient = new TableClientBuilder()
            .tableName(client.getTableName())
            .endpoint(client.getImplementation().getUrl())
            .serviceVersion(client.getApiVersion())
            .pipeline(BuilderHelper.buildNullClientPipeline())
            .buildAsyncClient();
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     *
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch createEntity(TableEntity entity) {
        validate(entity);
        addOperation(new BatchOperation.CreateEntity(entity));

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
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch upsertEntity(TableEntity entity) {
        return upsertEntity(entity, UpdateMode.MERGE);
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
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch upsertEntity(TableEntity entity, UpdateMode updateMode) {
        validate(entity);
        addOperation(new BatchOperation.UpsertEntity(entity, updateMode));

        return this;
    }

    /**
     * Updates an existing entity by merging the provided entity with the existing entity.
     *
     * @param entity The entity to update.
     *
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch updateEntity(TableEntity entity) {
        return updateEntity(entity, UpdateMode.MERGE);
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
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch updateEntity(TableEntity entity, UpdateMode updateMode) {
        return updateEntity(entity, updateMode, false);
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
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the entity's partition key does not match the partition key provided when
     * creating this {@link TableAsyncBatch} object, if the entity's row key is {@code null} or empty, or if another
     * operation with the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        validate(entity);
        addOperation(new BatchOperation.UpdateEntity(entity, updateMode, ifUnchanged));

        return this;
    }

    /**
     * Deletes an entity from the table.
     *
     * @param rowKey The row key of the entity.
     *
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the provided row key is {@code null} or empty, or if another operation with
     * the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch deleteEntity(String rowKey) {
        return deleteEntity(rowKey, "*");
    }

    /**
     * Deletes an entity from the table.
     *
     * @param rowKey The row key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     * the delete will not occur and an exception will be thrown.
     *
     * @return The updated {@link TableAsyncBatch}.
     *
     * @throws IllegalArgumentException If the provided row key is {@code null} or empty, or if another operation with
     * the same row key has already been added to the batch.
     * @throws IllegalStateException If this method is called after the batch has been submitted.
     */
    public TableAsyncBatch deleteEntity(String rowKey, String eTag) {
        validate(partitionKey, rowKey);
        addOperation(new BatchOperation.DeleteEntity(partitionKey, rowKey, eTag));

        return this;
    }

    /**
     * Gets an immutable list containing all operations added to this batch.
     *
     * @return An immutable list containing all operations added to this batch.
     */
    public synchronized List<BatchOperation> getOperations() {
        return Collections.unmodifiableList(this.operations);
    }

    /**
     * Executes all operations within The batch inside a transaction. When the call completes, either all operations in
     * the batch will succeed, or if a failure occurs, all operations in the batch will be rolled back.
     *
     * @return A reactive result containing a list of sub-responses for each operation in the batch.
     *
     * @throws IllegalStateException If no operations have been added to the batch.
     * @throws TableServiceErrorException if any operation within the batch fails. See the documentation for the client
     * methods in {@link TableAsyncClient} to understand the conditions that may cause a given operation to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public synchronized Mono<List<BatchOperationResponse>> submitTransaction() {
        return submitTransactionWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Executes all operations within The batch inside a transaction. When the call completes, either all operations in
     * the batch will succeed, or if a failure occurs, all operations in the batch will be rolled back.
     *
     * @return A reactive result containing the HTTP response produced for the batch itself. The response's value will
     * contain a list of sub-responses for each operation in the batch.
     *
     * @throws IllegalStateException If no operations have been added to the batch.
     * @throws TableServiceErrorException if any operation within the batch fails. See the documentation for the client
     * methods in {@link TableAsyncClient} to understand the conditions that may cause a given operation to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public synchronized Mono<Response<List<BatchOperationResponse>>> submitTransactionWithResponse() {
        return withContext(this::submitTransactionWithResponse);
    }

    synchronized Mono<Response<List<BatchOperationResponse>>> submitTransactionWithResponse(Context context) {
        this.frozen = true;
        Context finalContext = context == null ? Context.NONE : context;

        if (operations.size() == 0) {
            throw logger.logExceptionAsError(new IllegalStateException("A batch must contain at least one operation."));
        }

        return Flux.fromIterable(operations)
            .flatMapSequential(op -> op.prepareRequest(operationClient).zipWith(Mono.just(op)))
            .collect(BatchRequestBody::new, (body, pair) ->
                body.addChangeOperation(new BatchSubRequest(pair.getT2(), pair.getT1())))
            .flatMap(body ->
                batchImpl.submitBatchWithRestResponseAsync(body, null, finalContext).zipWith(Mono.just(body)))
            .flatMap(pair -> parseResponse(pair.getT2(), pair.getT1()));
    }

    private Mono<Response<List<BatchOperationResponse>>> parseResponse(BatchRequestBody requestBody,
                                                                       BatchSubmitBatchResponse response) {
        TableServiceError error = null;
        String errorMessage = null;
        BatchChangeSet changes = null;
        BatchOperation failedOperation = null;

        if (requestBody.getContents().get(0) instanceof BatchChangeSet) {
            changes = (BatchChangeSet) requestBody.getContents().get(0);
        }

        for (int i = 0; i < response.getValue().length; i++) {
            BatchOperationResponse subResponse = response.getValue()[i];

            // Attempt to attach a sub-request to each batch sub-response
            if (changes != null && changes.getContents().get(i) != null) {
                ModelHelper.updateBatchOperationResponse(subResponse,
                    changes.getContents().get(i).getHttpRequest());
            }

            // If one sub-response was an error, we need to throw even though the service responded with 202
            if (subResponse.getStatusCode() >= 400 && error == null && errorMessage == null) {
                if (subResponse.getValue() instanceof TableServiceError) {
                    error = (TableServiceError) subResponse.getValue();

                    // Make a best effort to locate the failed operation and include it in the message
                    if (changes != null && error.getOdataError() != null
                        && error.getOdataError().getMessage() != null
                        && error.getOdataError().getMessage().getValue() != null) {

                        String message = error.getOdataError().getMessage().getValue();

                        try {
                            int failedIndex = Integer.parseInt(message.substring(0, message.indexOf(":")));
                            failedOperation = changes.getContents().get(failedIndex).getOperation();
                        } catch (NumberFormatException e) {
                            // Unable to parse failed operation from batch error message - this just means
                            // the service did not indicate which request was the one that failed. Since
                            // this is optional, just swallow the exception.
                        }
                    }
                } else if (subResponse.getValue() instanceof String) {
                    errorMessage = "The service returned the following data for the failed operation: "
                        + subResponse.getValue();
                } else {
                    errorMessage =
                        "The service returned the following status code for the failed operation: "
                            + subResponse.getStatusCode();
                }
            }
        }

        if (error != null || errorMessage != null) {
            String message = "An operation within the batch failed, the transaction has been rolled back.";

            if (failedOperation != null) {
                message += " The failed operation was: " + failedOperation.toString();
            } else if (errorMessage != null) {
                message += " " + errorMessage;
            }

            return monoError(logger, new TableServiceErrorException(message, null, toTableServiceError(error)));
        } else {
            return Mono.just(new SimpleResponse<>(response, Arrays.asList(response.getValue())));
        }
    }

    private synchronized void addOperation(BatchOperation operation) {
        operations.add(operation);
    }

    private synchronized void validate(TableEntity entity) {
        validate(entity.getPartitionKey(), entity.getRowKey());
    }

    private synchronized void validate(String partitionKey, String rowKey) {
        if (this.frozen) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Operations can't be modified once a batch is submitted."));
        }

        if (!this.partitionKey.equals(partitionKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("All operations in a batch must share the same partition key."));
        }

        if (CoreUtils.isNullOrEmpty(rowKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The row key must not be null or empty."));
        }

        if (rowKeys.contains(rowKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Every operation in a batch must use a different row key."));
        } else {
            rowKeys.add(rowKey);
        }
    }
}
