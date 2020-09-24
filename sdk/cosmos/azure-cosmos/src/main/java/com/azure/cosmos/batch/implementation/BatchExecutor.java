// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.batch.TransactionalBatchResponse;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

final class BatchExecutor {

    private final CosmosAsyncContainer container;
    private final List<ItemBatchOperation<?>> operations;
    private final RequestOptions options;
    private final PartitionKey partitionKey;

    BatchExecutor(
        final CosmosAsyncContainer container,
        final PartitionKey partitionKey,
        final List<ItemBatchOperation<?>> operations,
        final RequestOptions options) {

        this.container = container;
        this.operations = operations;
        this.partitionKey = partitionKey;
        this.options = options;
    }


    /**
     * Create a batch request from list of operations and executes it.
     *
     * @return Response from the server.
     */
    final Mono<TransactionalBatchResponse> executeAsync() {

        BatchExecUtils.ensureValid(this.operations, this.options);
        final ArrayList<ItemBatchOperation<?>> operations = new ArrayList<>(this.operations);

        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createAsync(this.partitionKey, operations);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .executeBatchRequest(BridgeInternal.getLink(container), request, options, false);
    }
}
