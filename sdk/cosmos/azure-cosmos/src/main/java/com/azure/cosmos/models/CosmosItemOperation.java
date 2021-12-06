// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Encapsulates Cosmos Item Operation
 */
public interface CosmosItemOperation {

    /**
     * @return the id.
     */
    String getId();

    /**
     * @return the partition key value.
     */
    PartitionKey getPartitionKeyValue();

    /**
     * @return the operation type.
     */
    CosmosItemOperationType getOperationType();

    /**
     * @param <T> type of the item.
     * @return the item.
     */
    <T> T getItem();

    /**
     * @param <T> type of the context.
     * @return the context.
     */
    <T> T getContext();
}
