// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
 * Repository interface with search and delete operation
 *
 * @param <T> the type of the domain class.
 * @param <K> the type of the id of the entity the repository manages.
 */
@NoRepositoryBean
public interface ReactiveCosmosRepository<T, K> extends ReactiveSortingRepository<T, K>, ReactiveCrudRepository<T, K> {

    /**
     * Retrieves an entity by its id and partition key.
     * @param id must not be {@literal null}.
     * @param partitionKey partition key value of the entity, must not be null.
     * @return {@link Mono} emitting the entity with the given id or {@link Mono#empty()} if none found.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    Mono<T> findById(K id, PartitionKey partitionKey);

    /**
     * Deletes an entity by its id and partition key.
     * @param id must not be {@literal null}.
     * @param partitionKey partition key value of the entity, must not be null.
     * @return {@link Mono} emitting the void Mono.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}.
     */
    Mono<Void> deleteById(K id, PartitionKey partitionKey);

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
    <S extends T> Mono<S> save(K id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations);

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
    <S extends T> Mono<S> save(K id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options);

    /**
     * Returns Flux of items in a specific partition
     * @param partitionKey partition key value
     * @return {@link Flux} of items with partition key value
     */
    Flux<T> findAll(PartitionKey partitionKey);
}
