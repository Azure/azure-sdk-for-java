// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class SinglePartitionKeyServerBatchRequest extends ServerBatchRequest {

    private final PartitionKey partitionKey;

    /**
     * Initializes a new instance of the {@link SinglePartitionKeyServerBatchRequest} class. Single partition key server
     * request.
     *
     * @param partitionKey Partition key that applies to all operations in this request.
     */
    private SinglePartitionKeyServerBatchRequest(final PartitionKey partitionKey) {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.partitionKey = partitionKey;
    }

    /**
     * Creates an instance of {@link SinglePartitionKeyServerBatchRequest}. The body of the request is populated with
     * operations till it reaches the provided maxBodyLength.
     *
     * @param partitionKey Partition key of the request.
     * @param operations Operations to be added into this batch request.
     *
     * @return A newly created instance of {@link SinglePartitionKeyServerBatchRequest}.
     */
    static SinglePartitionKeyServerBatchRequest createBatchRequest(
        final PartitionKey partitionKey,
        final List<CosmosItemOperation> operations) {

        checkNotNull(partitionKey, "expected non-null partitionKey");
        checkNotNull(operations, "expected non-null operations");

        final SinglePartitionKeyServerBatchRequest request = new SinglePartitionKeyServerBatchRequest(partitionKey);
        request.createBodyOfBatchRequest(operations);

        return request;
    }

    /**
     * Returns the {@link PartitionKey partition key} that applies to all operations in this {@link
     * SinglePartitionKeyServerBatchRequest batch request}.
     *
     * @return the {@link PartitionKey partition key} that applies to all operations in this {@link
     * SinglePartitionKeyServerBatchRequest batch request}.
     */
    public PartitionKey getPartitionKeyValue() {
        return this.partitionKey;
    }
}
