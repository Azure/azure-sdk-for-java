// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.BatchGetResult;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.azure.cosmos.benchmark.linkedin.impl.models.QueryOptions;
import com.azure.cosmos.benchmark.linkedin.impl.models.Result;


/**
 * A generic interface for CRUD operations on any data store.
 *
 * @param <K> The key for the entity stored in the data store
 * @param <V> The entity stored in the data store
 */
public interface Accessor<K, V> {

    /**
     * Accessor initialization operations
     */
    void initialize();

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

    /**
     * Retrieves the entity from the data source using SQL expression, using the query options provided.
     *
     * @param queryOptions The SQL query, and related options for executing this query
     * @return A BatchGetResult containing the results [if present] and metadata about the operation
     * @throws AccessorException when the underlying data store throws an exception for any reason.
     */
    BatchGetResult<K, V> query(final QueryOptions queryOptions)
        throws AccessorException;
}
