// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public final class BatchExecutor {

    private final CosmosAsyncContainer container;
    private final CosmosBatchRequestOptions options;
    private final CosmosBatch cosmosBatch;

    public BatchExecutor(
        final CosmosAsyncContainer container,
        final CosmosBatch cosmosBatch,
        final CosmosBatchRequestOptions options) {

        this.container = container;
        this.cosmosBatch = cosmosBatch;
        this.options = options;
    }

    /**
     * Create a batch request from list of operations and executes it.
     *
     * @return Response from the server.
     */
    public final Mono<CosmosBatchResponse> executeAsync() {

        List<CosmosItemOperation> operations = this.cosmosBatch.getOperations();
        checkArgument(operations.size() > 0, "Number of operations should be more than 0.");

        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            this.cosmosBatch.getPartitionKey(),
            operations);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .executeBatchRequest(BridgeInternal.getLink(container), request, ModelBridgeInternal.toRequestOptions(options), false);
    }
}
