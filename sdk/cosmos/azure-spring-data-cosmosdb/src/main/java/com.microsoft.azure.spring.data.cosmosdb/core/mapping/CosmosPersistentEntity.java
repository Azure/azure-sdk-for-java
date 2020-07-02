// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * Represents a cosmos persistent entity.
 */
public interface CosmosPersistentEntity<T> extends PersistentEntity<T, CosmosPersistentProperty> {
    /**
     * To get collection
     * @return String
     * @deprecated use {@link #getContainer()} instead
     */
    @Deprecated
    String getCollection();

    /**
     * To get container of entity
     * @return String
     */
    String getContainer();

    /**
     * To get language
     * @return String
     */
    String getLanguage();
}
