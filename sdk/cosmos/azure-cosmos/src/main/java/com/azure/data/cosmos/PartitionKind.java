// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Specifies the partition scheme for an multiple-partitioned collection in the Azure Cosmos DB database service.
 */
public enum PartitionKind {
    /**
     * The Partition of a document is calculated based on the hash value of the PartitionKey.
     */
    HASH;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());        
    }    
}
