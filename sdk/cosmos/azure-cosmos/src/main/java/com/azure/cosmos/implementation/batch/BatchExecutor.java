// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.TransactionalBatch;
import com.azure.cosmos.TransactionalBatchRequestOptions;
import com.azure.cosmos.TransactionalBatchResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public final class BatchExecutor {

    private final CosmosAsyncContainer container;
    private final TransactionalBatchRequestOptions options;
    private final TransactionalBatch transactionalBatch;

    public BatchExecutor(
        final CosmosAsyncContainer container,
        final TransactionalBatch transactionalBatch,
        final TransactionalBatchRequestOptions options) {

        this.container = container;
        this.transactionalBatch = transactionalBatch;
        this.options = options;
    }

    /**
     * Create a batch request from list of operations and executes it.
     *
     * @return Response from the server.
     */
    public final Mono<TransactionalBatchResponse> executeAsync() {

        List<CosmosItemOperation> operations = this.transactionalBatch.getOperations();
        checkArgument(operations.size() > 0, "Number of operations should be more than 0.");

        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            this.transactionalBatch.getPartitionKeyValue(),
            operations);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .executeBatchRequest(BridgeInternal.getLink(container), request, BridgeInternal.toRequestOptions(options), false);
    }
}
