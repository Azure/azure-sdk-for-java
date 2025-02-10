// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.Optional;

/**
 * Extension of {@link PagingAndSortingRepository} to provide additional methods to retrieve entities using the
 * pagination and sorting abstraction.
 *
 * @param <T> domain type.
 * @param <ID> id type.
 */
@NoRepositoryBean
public interface CosmosRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID>,
    CrudRepository<T, ID> {

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
     * Patches an entity by its id and partition key with CosmosPatchItemRequestOptions
     *
     * @param id must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param patchOperations must not be {@literal null}, max operations is 10
     * @param <S> type class of domain type
     * @return the patched entity
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    <S extends T> S save(ID id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations);

    /**
     * Patches an entity by its id and partition key with CosmosPatchItemRequestOptions
     *
     * @param id must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param patchOperations must not be {@literal null}, max operations is 10
     * @param options Optional CosmosPatchItemRequestOptions, e.g. options.setFilterPredicate("FROM products p WHERE p.used = false");
     * @param <S> type class of domain type
     * @return the patched entity
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    <S extends T> S save(ID id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options);

    /**
     * Returns list of items in a specific partition
     *
     * @param partitionKey partition key value
     * @return Iterable of items with partition key value
     */
    Iterable<T> findAll(PartitionKey partitionKey);

}

