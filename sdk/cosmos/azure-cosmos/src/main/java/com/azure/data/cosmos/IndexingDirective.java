// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Specifies whether or not the resource is to be indexed in the Azure Cosmos DB database service.
 */
public enum IndexingDirective {

    /**
     * Use any pre-defined/pre-configured defaults.
     */
    DEFAULT,

    /**
     * Index the resource.
     */
    INCLUDE,

    /**
     * Do not index the resource.
     */
    EXCLUDE;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());        
    }
}
