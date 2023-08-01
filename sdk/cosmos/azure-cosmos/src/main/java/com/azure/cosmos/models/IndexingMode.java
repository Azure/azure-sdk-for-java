// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the supported indexing modes in the Azure Cosmos DB database service.
 */
public enum IndexingMode {
    /**
     * Index is updated synchronously with a create or update operation.
     * <p>
     * With consistent indexing, query behavior is the same as the default consistency level for the container. The
     * index is always kept up to date with the data.
     */
    CONSISTENT("Consistent"),

    /**
     * Index is updated asynchronously with respect to a create or update operation.
     * <p>
     * With lazy indexing, queries are eventually consistent. The index is updated when the container is idle.
     */
    LAZY("Lazy"),

    /**
     * No index is provided.
     * <p>
     * Setting IndexingMode to "NONE" drops the index. Use this if you don't want to maintain the index for a item
     * container, to save the storage cost or improve the write throughput. Your queries will degenerate to scans of
     * the entire container.
     */
    NONE("None");

    IndexingMode(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
