// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemOperation;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class ServerOperationBatchRequest {

    private final PartitionKeyRangeServerBatchRequest batchRequest;
    private final List<CosmosItemOperation> pendingOperations;

    /**
     * Creates a new pair of batch request and pending operations.
     *
     * @param batchRequest the {@link ServerBatchRequest batch request}
     * @param operations the list of {@link CosmosItemOperation pendingOperations} for the batch request.
     */
    ServerOperationBatchRequest(
        final PartitionKeyRangeServerBatchRequest batchRequest,
        final List<CosmosItemOperation> operations) {

        checkNotNull(batchRequest, "expected non-null batchRequest");
        checkNotNull(operations, "expected non-null pendingOperations");

        this.batchRequest = batchRequest;
        this.pendingOperations = operations;
    }

    /**
     * Gets the PartitionKeyRangeServerBatchRequest.
     *
     * @return PartitionKeyRangeServerBatchRequest
     */
    PartitionKeyRangeServerBatchRequest getBatchRequest() {
        return this.batchRequest;
    }

    /**
     * Gets list of CosmosItemOperation.
     *
     * @return list of CosmosItemOperation.
     */
    List<CosmosItemOperation> getBatchPendingOperations() {
        return this.pendingOperations;
    }
}
