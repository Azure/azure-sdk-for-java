// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Specifies the supported indexing modes in the Azure Cosmos DB database service.
 */
public enum IndexingMode {
    /**
     * Index is updated synchronously with a create or update operation.
     * <p>
     * With consistent indexing, query behavior is the same as the default consistency level for the collection. The
     * index is always kept up to date with the data.
     */
    CONSISTENT,

    /**
     * Index is updated asynchronously with respect to a create or update operation.
     * <p>
     * With lazy indexing, queries are eventually consistent. The index is updated when the collection is idle.
     */
    LAZY,

    /**
     * No index is provided.
     * <p>
     * Setting IndexingMode to "NONE" drops the index. Use this if you don't want to maintain the index for a document
     * collection, to save the storage cost or improve the write throughput. Your queries will degenerate to scans of
     * the entire collection.
     */
    NONE;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());        
    }
}
