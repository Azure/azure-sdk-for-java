// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class BatchExecutor {

    private final CosmosAsyncContainer container;
    private final List<ItemBatchOperation<?>> operations;
    private final RequestOptions options;
    private final PartitionKey partitionKey;

    public BatchExecutor(
        final CosmosAsyncContainer container,
        final PartitionKey partitionKey,
        final List<ItemBatchOperation<?>> operations,
        final RequestOptions options) {

        this.container = container;
        this.operations = operations;
        this.partitionKey = partitionKey;
        this.options = options;
    }

    public Mono<TransactionalBatchResponse> executeAsync() {

        BatchExecUtils.ensureValid(this.operations, this.options);
        final ArrayList<ItemBatchOperation<?>> operations = new ArrayList<>(this.operations);

        SinglePartitionKeyServerBatchRequest request = SinglePartitionKeyServerBatchRequest.createAsync(this.partitionKey, operations);

        return executeBatchRequestAsync(request);
    }

    /**
     * Makes a single batch request to the server.
     *
     * @param request A server request with a set of operations on items.
     *
     * @return Response from the server.
     */
    private Mono<TransactionalBatchResponse> executeBatchRequestAsync(final SinglePartitionKeyServerBatchRequest request) {

        checkNotNull(request, "expected non-null request");
        request.setAtomicBatch(true);
        request.setShouldContinueOnError(false);

        return CosmosBridgeInternal.getAsyncDocumentClient(container)
            .executeBatchRequest(BridgeInternal.getLink(container), request, options, false);
    }
}
