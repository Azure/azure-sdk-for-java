// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.BatchImpl;
import com.azure.data.tables.implementation.TablesMultipartSerializer;
import com.azure.data.tables.implementation.models.BatchChangeSet;
import com.azure.data.tables.implementation.models.BatchOperationResponse;
import com.azure.data.tables.implementation.models.BatchRequestBody;
import com.azure.data.tables.implementation.models.BatchSubRequest;
import com.azure.data.tables.implementation.models.TableServiceError;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.BatchOperation;
import com.azure.data.tables.models.TableEntity;
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

@Fluent
public final class TableAsyncBatch {
    private final ClientLogger logger = new ClientLogger(TableAsyncBatch.class);
    private final String partitionKey;
    private final TableAsyncClient operationClient;
    private final BatchImpl batchImpl;
    private final HashSet<String> rowKeys = new HashSet<>();
    private final List<BatchOperation> operations = new ArrayList<>();
    private boolean frozen = false;

    TableAsyncBatch(String partitionKey, TableAsyncClient client) {
        this.partitionKey = partitionKey;
        this.batchImpl = new BatchImpl(client.getImplementation(), new TablesMultipartSerializer());
        this.operationClient = new TableClientBuilder()
            .tableName(client.getTableName())
            .endpoint(client.getImplementation().getUrl())
            .serviceVersion(client.getApiVersion())
            .pipeline(BuilderHelper.buildNullClientPipeline())
            .buildAsyncClient();
    }

    public TableAsyncBatch createEntity(TableEntity entity) {
        validate(entity);
        addOperation(new BatchOperation.CreateEntity(entity));
        return this;
    }

    public TableAsyncBatch upsertEntity(TableEntity entity) {
        return upsertEntity(entity, UpdateMode.MERGE);
    }

    public TableAsyncBatch upsertEntity(TableEntity entity, UpdateMode updateMode) {
        validate(entity);
        addOperation(new BatchOperation.UpsertEntity(entity, updateMode));
        return this;
    }

    public TableAsyncBatch updateEntity(TableEntity entity) {
        return updateEntity(entity, UpdateMode.MERGE);
    }

    public TableAsyncBatch updateEntity(TableEntity entity, UpdateMode updateMode) {
        return updateEntity(entity, updateMode, false);
    }

    public TableAsyncBatch updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        validate(entity);
        addOperation(new BatchOperation.UpdateEntity(entity, updateMode, ifUnchanged));
        return this;
    }

    public TableAsyncBatch deleteEntity(String rowKey) {
        return deleteEntity(rowKey, "*");
    }

    public TableAsyncBatch deleteEntity(String rowKey, String eTag) {
        validate(partitionKey, rowKey);
        addOperation(new BatchOperation.DeleteEntity(partitionKey, rowKey, eTag));
        return this;
    }

    public synchronized List<BatchOperation> getOperations() {
        return Collections.unmodifiableList(this.operations);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public synchronized Mono<List<BatchOperationResponse>> submitTransaction() {
        return submitTransactionWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public synchronized Mono<Response<List<BatchOperationResponse>>> submitTransactionWithResponse() {
        return withContext(this::submitTransactionWithResponse);
    }

    synchronized Mono<Response<List<BatchOperationResponse>>> submitTransactionWithResponse(Context context) {
        this.frozen = true;
        context = context == null ? Context.NONE : context;

        final BatchRequestBody body = new BatchRequestBody();
        Flux.fromIterable(operations)
            .flatMapSequential(op -> op.prepareRequest(operationClient))
            .zipWith(Flux.fromIterable(operations))
            .doOnNext(pair -> body.addChangeOperation(new BatchSubRequest(pair.getT2(), pair.getT1())))
            .blockLast();

        try {
            return batchImpl.submitBatchWithRestResponseAsync(body, null, context)
                .map(response -> {
                    TableServiceError error = null;
                    BatchChangeSet changes = null;
                    BatchOperation failedOperation = null;

                    if (body.getContents().get(0) instanceof BatchChangeSet) {
                        changes = (BatchChangeSet) body.getContents().get(0);
                    }

                    for (int i = 0; i < response.getValue().length; i++) {
                        BatchOperationResponse subResponse = response.getValue()[i];

                        // Attempt to attach a sub-request to each batch sub-response
                        if (changes != null && changes.getContents().get(i) != null) {
                            subResponse.setRequest(changes.getContents().get(i).getHttpRequest());
                        }

                        // If one sub-response was an error, we need to throw even though the service responded with 202
                        if (subResponse.getStatusCode() >= 400 && error == null
                            && subResponse.getValue() instanceof TableServiceError) {
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
                                    logger.logThrowableAsWarning(new IllegalArgumentException(
                                        "Unable to parse failed operation from batch error message.", e));
                                }
                            }
                        }
                    }

                    if (error != null) {
                        String message = "An operation within the batch failed, the transaction has been rolled back.";
                        if (failedOperation != null) {
                            message += " The failed operation was: " + failedOperation.toString();
                        }
                        throw logger.logExceptionAsError(new TableServiceErrorException(message, null, error));
                    } else {
                        return new SimpleResponse<>(response, Arrays.asList(response.getValue()));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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

        if (rowKeys.contains(rowKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Every operation in a batch must use a different row key."));
        } else {
            rowKeys.add(rowKey);
        }
    }
}
