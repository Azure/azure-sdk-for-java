// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.Optional;

/**
 * Extension of {@link PagingAndSortingRepository} to provide additional methods to retrieve entities using the
 * pagination and sorting abstraction.
 */
@NoRepositoryBean
public interface CosmosRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @param partitionKey partition key value of entity, must not be null.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    Optional<T> findById(ID id, PartitionKey partitionKey);

    /**
     * Deletes an entity by its id and partition key.
     *
     * @param id must not be {@literal null}.
     * @param partitionKey partition key value of the entity, must not be null.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    void deleteById(ID id, PartitionKey partitionKey);

    /**
     * patches an entity by its id and partition key.
     *
     * @param patchOperations patch operations, must not be null.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    <T> T save(T objectToPatch, CosmosPatchOperations patchOperations);

    /**
     * patches an entity by its id and partition key with CosmosPatchItemRequestOptions
     *
     * @param patchOperations patch operations, must not be null.
     * @param options additional CosmosPatchItemRequestOptions options, e.g. options.setFilterPredicate("FROM products p WHERE p.used = false");
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    <T> T save(T objectToPatch, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options);

    /**
     * Returns list of items in a specific partition
     *
     * @param partitionKey partition key value
     * @return Iterable of items with partition key value
     */
    Iterable<T> findAll(PartitionKey partitionKey);

}

