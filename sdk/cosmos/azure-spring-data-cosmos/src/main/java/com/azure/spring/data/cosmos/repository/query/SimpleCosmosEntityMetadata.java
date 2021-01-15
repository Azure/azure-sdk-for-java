// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.util.Assert;

/**
 * Metadata class to describe simple cosmos entity includes domain type and cosmos entity information
 */
public class SimpleCosmosEntityMetadata<T> implements CosmosEntityMetadata<T> {

    private final Class<T> type;
    private final CosmosEntityInformation<T, String> entityInformation;

    /**
     * Initialization
     *
     * @param type the actual domain class type
     * @param entityInformation cosmos entity
     */
    public SimpleCosmosEntityMetadata(Class<T> type, CosmosEntityInformation<T, String> entityInformation) {
        Assert.notNull(type, "type must not be null!");
        Assert.notNull(entityInformation, "entityInformation must not be null!");

        this.type = type;
        this.entityInformation = entityInformation;
    }

    /**
     * Return the actual domain class type
     *
     * @return type
     */
    public Class<T> getJavaType() {
        return type;
    }

    /**
     * Get collection name of cosmos
     *
     * @return container name
     */
    public String getCollectionName() {
        return entityInformation.getContainerName();
    }

    /**
     * Get container name of cosmos
     *
     * @return container name
     */
    public String getContainerName() {
        return entityInformation.getContainerName();
    }
}
