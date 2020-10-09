// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ItemBatchRequestOptions;
import com.azure.cosmos.CosmosItemOperationType;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents an operation on an item which will be executed as part of a batch request on a container. This will be
 * serialized and sent in the request.
 *
 * @param <T> The type of item.
 */
public final class ItemBatchOperation<T> {

    private T item;

    private final String id;
    private final int operationIndex;
    private final PartitionKey partitionKey;
    private final CosmosItemOperationType operationType;
    private final ItemBatchRequestOptions requestOptions;

    ItemBatchOperation(
        final CosmosItemOperationType operationType,
        final int operationIndex,
        final PartitionKey partitionKey,
        final String id,
        final T item,
        final ItemBatchRequestOptions requestOptions) {

        checkNotNull(operationType, "expected non-null operationType");
        checkArgument(operationIndex >= 0, "expected operationIndex >= 0, not %s", operationIndex);

        this.operationType = operationType;
        this.operationIndex = operationIndex;
        this.partitionKey = partitionKey;
        this.id = id;
        this.item = item;
        this.requestOptions = requestOptions;
    }

    public T getItem() {
        return this.item;
    }

    public String getId() {
        return this.id;
    }

    public int getOperationIndex() {
        return operationIndex;
    }

    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    public CosmosItemOperationType getOperationType() {
        return this.operationType;
    }

    public ItemBatchRequestOptions getItemBatchRequestOptions() {
        return this.requestOptions;
    }
}
