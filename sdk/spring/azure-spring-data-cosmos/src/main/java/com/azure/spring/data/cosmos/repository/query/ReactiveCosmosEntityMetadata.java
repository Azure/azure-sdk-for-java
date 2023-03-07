// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.data.repository.core.EntityMetadata;

/**
 * Metadata class to describe reactive cosmos entity includes domain type and container information
 */
public interface ReactiveCosmosEntityMetadata<T> extends EntityMetadata<T> {

    /**
     * Get container name from the given entity
     * @return String
     */
    String getContainerName();
}
