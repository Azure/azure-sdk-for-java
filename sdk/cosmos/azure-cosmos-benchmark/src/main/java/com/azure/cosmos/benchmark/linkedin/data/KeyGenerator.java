// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

/**
 * Interface to model key generation, independent of the entity we are testing
 */
public interface KeyGenerator {
    /**
     * @return a Key instance for this entity, composed of id and partitioningKey
     */
    Key key();
}
