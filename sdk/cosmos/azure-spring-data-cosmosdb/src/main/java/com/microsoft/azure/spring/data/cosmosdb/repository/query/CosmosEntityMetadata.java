// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.data.repository.core.EntityMetadata;

/**
 * Metadata class to describe cosmos entity includes domain type and container information
 */
public interface CosmosEntityMetadata<T> extends EntityMetadata<T> {

    /**
     * Get collection name from the given entity
     * @return String
     * @deprecated use {@link #getContainerName()} instead
     */
    @Deprecated
    String getCollectionName();

    /**
     * Get container name from the given entity
     * @return String
     */
    String getContainerName();
}
