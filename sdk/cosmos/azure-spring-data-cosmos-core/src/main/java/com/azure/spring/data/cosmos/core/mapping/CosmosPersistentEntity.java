// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * Represents a cosmos persistent entity.
 */
public interface CosmosPersistentEntity<T> extends PersistentEntity<T, CosmosPersistentProperty> {

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
