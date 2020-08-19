// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerOperationBatchRequest {

    private final PartitionKeyRangeServerBatchRequest batchRequest;
    private final List<ItemBatchOperation<?>> pendingOperations;

    /**
     * Creates a new pair
     * @param batchRequest the {@link ServerBatchRequest batch request}
     * @param operations the {@link List list} of {@link ItemBatchOperation pendingOperations} for the batch request.
     */
    ServerOperationBatchRequest(
        final PartitionKeyRangeServerBatchRequest batchRequest,
        final List<ItemBatchOperation<?>> operations) {

        checkNotNull(batchRequest, "expected non-null batchRequest");
        checkNotNull(operations, "expected non-null pendingOperations");

        this.batchRequest = batchRequest;
        this.pendingOperations = operations;
    }

    /**
     * Gets the PartitionKeyRangeServerBatchRequest.
     * @return key for this pair
     */
    public PartitionKeyRangeServerBatchRequest getBatchRequest() {
        return this.batchRequest;
    }

    /**
     * Gets list of ItemBatchOperation.
     * @return value for this pair
     */
    public List<ItemBatchOperation<?>> getBatchPendingOperations() {
        return this.pendingOperations;
    }
}
