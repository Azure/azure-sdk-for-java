// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the partition scheme for an multiple-partitioned container in the Azure Cosmos DB database service.
 */
public enum PartitionKind {
    /**
     * The Partition of a item is calculated based on the hash value of the PartitionKey.
     */
    HASH("Hash");

    PartitionKind(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
