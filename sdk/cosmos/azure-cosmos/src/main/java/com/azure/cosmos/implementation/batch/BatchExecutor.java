// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.RequestOptions;
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
    private final RequestOptions options;
    private final CosmosBatch cosmosBatch;
    private final CosmosItemSerializer effectiveItemSerializer;


    public BatchExecutor(
        final CosmosAsyncContainer container,
        final CosmosBatch cosmosBatch,
        final RequestOptions options) {

        this.container = container;
        this.cosmosBatch = cosmosBatch;
        this.options = options;
        AsyncDocumentClient docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.effectiveItemSerializer = docClientWrapper.getEffectiveItemSerializer(
            this.options != null ? this.options.getEffectiveItemSerializer() : null
        );
    }

    /**
     * Create a batch request from list of operations and executes it.
     *
     * @return Response from the server.
     */
    public Mono<CosmosBatchResponse> executeAsync() {

        List<CosmosItemOperation> operations = this.cosmosBatch.getOperations();
        checkArgument(operations.size() > 0, "Number of operations should be more than 0.");

        final SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            this.cosmosBatch.getPartitionKeyValue(),
            operations,
            this.effectiveItemSerializer);
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .executeBatchRequest(BridgeInternal.getLink(container), request, options, false);
    }
}
