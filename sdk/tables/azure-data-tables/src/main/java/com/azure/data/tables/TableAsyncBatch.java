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
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.BatchImpl;
import com.azure.data.tables.implementation.TablesMultipartSerializer;
import com.azure.data.tables.implementation.models.BatchRequestBody;
import com.azure.data.tables.models.BatchOperation;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
    public synchronized Mono<Void> submitTransaction() {
        return submitTransactionWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public synchronized Mono<Response<Void>> submitTransactionWithResponse() {
        return withContext(this::submitTransactionWithResponse);
    }

    synchronized Mono<Response<Void>> submitTransactionWithResponse(Context context) {
        this.frozen = true;
        context = context == null ? Context.NONE : context;

        final BatchRequestBody body = new BatchRequestBody();

        Flux.fromIterable(operations)
            .flatMapSequential(op -> op.prepareRequest(operationClient))
            .doOnNext(body::addChangeOperation)
            .blockLast();

        try {
            return batchImpl.submitBatchWithRestResponseAsync(body, null, context)
                .map(response -> new SimpleResponse<>(response, null));
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
            throw new IllegalStateException("Operations can't be modified once a batch is submitted.");
        }

        if (!this.partitionKey.equals(partitionKey)) {
            throw new IllegalArgumentException("All operations in a batch must share the same partition key.");
        }

        if (rowKeys.contains(rowKey)) {
            throw new IllegalArgumentException("Every operation in a batch must use a different row key.");
        } else {
            rowKeys.add(rowKey);
        }
    }
}
