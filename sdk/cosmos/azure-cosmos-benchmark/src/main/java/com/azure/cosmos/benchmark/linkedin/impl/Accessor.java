// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.azure.cosmos.benchmark.linkedin.impl.models.Result;


/**
 * A generic interface for CRUD operations on any data store.
 *
 * @param <K> The key for the entity stored in the data store
 * @param <V> The entity stored in the data store
 */
public interface Accessor<K, V> {

    /**
     * Retrieves the entity from the data source, using the key and request options provided. The entire
     * key must be defined, and partial keys will NOT be accepted for GET. Use BatchGet for retrieving entities
     * based on partial primary keys.
     *
     * @param key the key to lookup from the underlying store
     * @param requestOptions Optional request options for this lookup
     * @return A Result is always returned, containing the results [if present] and metadata about the operation.
     * @throws AccessorException when the underlying data store throws an exception for any reason.
     */
    Result<K, V> get(final K key, final GetRequestOptions requestOptions) throws AccessorException;
}
