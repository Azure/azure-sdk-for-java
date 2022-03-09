// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

/**
 * Interface for modeling the configurations for each entity, allowing the same implementation
 * to be leveraged for different use-cases
 */
public interface EntityConfiguration {

    /**
     * @return KeyGenerator for this entity
     */
    KeyGenerator keyGenerator();

    /**
     * @return Data Generator for this entity, which facilitate generating documents conforming to this
     *         entities schema
     */
    DataGenerator dataGenerator();

    /**
     * @return The configuration for the underlying collection used to store this entity's data
     */
    CollectionAttributes collectionAttributes();
}
