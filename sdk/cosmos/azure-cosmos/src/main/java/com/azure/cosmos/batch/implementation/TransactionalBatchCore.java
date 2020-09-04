// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.batch.TransactionalBatch;
import com.azure.cosmos.batch.TransactionalBatchItemRequestOptions;
import com.azure.cosmos.batch.TransactionalBatchRequestOptions;
import com.azure.cosmos.batch.TransactionalBatchResponse;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class TransactionalBatchCore implements TransactionalBatch {

    private final CosmosAsyncContainer container;
    private final ArrayList<ItemBatchOperation<?>> operations;
    private final PartitionKey partitionKey;
    private final String batchSpanName;

    /**
     * Initializes a new instance of the {@link TransactionalBatchCore} class.
     *
     * @param container a container of items on which the batch operations are to be performed.
     * @param partitionKey the partition key for all items on which batch operations are to be performed.
     */
    public TransactionalBatchCore(
        final CosmosAsyncContainer container,
        final PartitionKey partitionKey) {

        checkNotNull(container, "expected non-null container");
        checkNotNull(partitionKey, "expected non-null partitionKey");

        this.container = container;
        this.operations = new ArrayList<>();
        this.partitionKey = partitionKey;
        this.batchSpanName = "transactionalBatch." + container.getId();
    }

    @Override
    public <TItem> TransactionalBatch createItem(
        final TItem item,
        final TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");

        this.operations.add(
            new ItemBatchOperation.Builder<TItem>(
                OperationType.Create,
                this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .resource(item)
            .build());

        return this;
    }

    @Override
    public TransactionalBatch deleteItem(final String id, final TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");

        this.operations.add(new ItemBatchOperation.Builder<Void>(OperationType.Delete, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .id(id)
            .build());

        return this;
    }

    @Override
    public TransactionalBatch readItem(final String id, final TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");

        this.operations.add(new ItemBatchOperation.Builder<Void>(OperationType.Read, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .id(id)
            .build());

        return this;
    }

    @Override
    public <TItem> TransactionalBatch replaceItem(
        final String id,
        final TItem item,
        TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(id, "expected non-null id");
        checkNotNull(item, "expected non-null item");

        this.operations.add(new ItemBatchOperation.Builder<TItem>(OperationType.Replace, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .resource(item)
            .id(id)
            .build());

        return this;
    }

    @Override
    public <TItem> TransactionalBatch upsertItem(
        final TItem item,
        final TransactionalBatchItemRequestOptions requestOptions) {

        checkNotNull(item, "expected non-null item");

        this.operations.add(new ItemBatchOperation.Builder<TItem>(OperationType.Upsert, this.operations.size())
            .requestOptions(requestOptions.toRequestOptions())
            .resource(item)
            .build());

        return this;
    }

    @Override
    public Mono<TransactionalBatchResponse> executeAsync() {
        return this.executeAsync(new TransactionalBatchRequestOptions());
    }

    /**
     * Executes the batch at the Azure Cosmos service as an asynchronous operation.
     *
     * @param requestOptions Options that apply to the batch.
     *
     * @return An {@link Mono} that will contain the results of each operation or an error.
     *
     */
    @Override
    public Mono<TransactionalBatchResponse> executeAsync(TransactionalBatchRequestOptions requestOptions) {

        BatchExecutor executor = new BatchExecutor(
            this.container,
            this.partitionKey,
            new ArrayList<>(this.operations),
            requestOptions.toRequestOptions());

        this.operations.clear();
        Mono<TransactionalBatchResponse> responseMono = executor.executeAsync();

        return withContext(context -> CosmosBridgeInternal.getTracerProvider(container).
            traceEnabledBatchResponsePublisher(responseMono,
                context,
                this.batchSpanName,
                container.getDatabase().getId(),
                CosmosBridgeInternal.getServiceEndpoint(container)));
    }
}
