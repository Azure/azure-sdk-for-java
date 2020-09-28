// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.TransactionalBatch;
import com.azure.cosmos.TransactionalBatchRequestOptions;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.RequestOptions;
import reactor.core.publisher.Mono;

import java.util.List;

public final class BatchExecutor {

    private final CosmosAsyncContainer container;
    private final RequestOptions options;
    private final TransactionalBatch transactionalBatch;

    public BatchExecutor(
        final CosmosAsyncContainer container,
        final TransactionalBatch transactionalBatch,
        final TransactionalBatchRequestOptions options) {

        this.container = container;
        this.transactionalBatch = transactionalBatch;
        this.options = options.toRequestOptions();
    }

    /**
     * Create a batch request from list of operations and executes it.
     *
     * @return Response from the server.
     */
    public final Mono<TransactionalBatchResponse> executeAsync() {

        List<ItemBatchOperation<?>> operations = this.transactionalBatch.getOperations();

        BatchExecUtils.ensureValid(operations, this.options);

        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createAsync(
            this.transactionalBatch.getPartitionKey(),
            operations);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .executeBatchRequest(BridgeInternal.getLink(container), request, options, false);
    }
}
