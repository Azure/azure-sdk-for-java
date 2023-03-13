// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the partition scheme for a multiple-partitioned container in the Azure Cosmos DB database service.
 */
public enum PartitionKind {
    /**
     * The Partition of an item is calculated based on the hash value of the PartitionKey.
     */
    HASH("Hash"),

    /**
     * The Partition of an item is calculated based on a range.
     */
    RANGE("Range"),

    /**
     * The Partition of an item is calculated based on the hash value of multiple PartitionKeys.
     */
    MULTI_HASH("MultiHash");

    PartitionKind(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
