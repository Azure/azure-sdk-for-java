// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Operation class of reactive cosmos
 */
public interface ReactiveCosmosOperations {

    /**
     * Get container name
     *
     * @param domainType the domainType
     * @return container name
     */
    String getContainerName(Class<?> domainType);

    /**
     * Creates a container if it doesn't already exist
     *
     * @param information the CosmosEntityInformation
     * @return Mono of CosmosContainerResponse
     */
    Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Get properties for specified container
     *
     * @param containerName String
     * @return CosmosContainerProperties
     */
    Mono<CosmosContainerProperties> getContainerProperties(String containerName);

    /**
     * Replace container properties for the specified container
     *
     * @param containerName String
     * @param properties CosmosContainerProperties
     * @return CosmosContainerProperties
     */
    Mono<CosmosContainerProperties> replaceContainerProperties(String containerName,
                                                               CosmosContainerProperties properties);

    /**
     * Find all items in a given container
     *
     * @param containerName the containerName
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux of results
     */
    <T> Flux<T> findAll(String containerName, Class<T> domainType);

    /**
     * Find all items in a given container
     *
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux of results
     */
    <T> Flux<T> findAll(Class<T> domainType);

    /**
     * Find all items in a given container with partition key
     *
     * @param partitionKey partition Key
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux of results
     */
    <T> Flux<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Mono of result
     */
    <T> Mono<T> findById(Object id, Class<T> domainType);

    /**
     * Find by id
     *
     * @param containerName the containername
     * @param id the id
     * @param domainType type class
     * @param <T> type of domainType
     * @return Mono of result
     */
    <T> Mono<T> findById(String containerName, Object id, Class<T> domainType);

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType type class
     * @param partitionKey partition Key
     * @param <T> type of domainType
     * @return Mono of result
     */
    <T> Mono<T> findById(Object id, Class<T> domainType, PartitionKey partitionKey);

    /**
     * Insert
     *
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @param <T> type of inserted objectToSave
     * @return Mono of result
     */
    <T> Mono<T> insert(T objectToSave, PartitionKey partitionKey);

    /**
     * Insert
     *
     * @param <T> type of inserted objectToSave
     * @param containerName the container name
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @return Mono of result
     */
    <T> Mono<T> insert(String containerName, Object objectToSave, PartitionKey partitionKey);

    /**
     * Insert
     *
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param <T> type class of domain type
     * @return Mono of result
     */
    <T> Mono<T> insert(String containerName, T objectToSave);

    /**
     * Upsert an item with partition key
     *
     * @param object the object to upsert
     * @param <T> type class of object
     * @return Mono of result
     */
    <T> Mono<T> upsert(T object);

    /**
     * Upsert an item to container with partition key
     *
     * @param containerName the container name
     * @param object the object to save
     * @param <T> type class of object
     * @return Mono of result
     */
    <T> Mono<T> upsert(String containerName, T object);

    /**
     * Delete an item by id
     *
     * @param containerName the container name
     * @param id the id
     * @param partitionKey the partition key
     * @return void Mono
     */
    Mono<Void> deleteById(String containerName, Object id, PartitionKey partitionKey);

    /**
     * Delete using entity
     *
     * @param <T> type class of domain type
     * @param containerName the container name
     * @param entity the entity object
     * @return void Mono
     */
    <T> Mono<Void> deleteEntity(String containerName, T entity);

    /**
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param domainType the domainType
     * @return void Mono
     */
    Mono<Void> deleteAll(String containerName, Class<?> domainType);

    /**
     * Delete container
     *
     * @param containerName the container name
     */
    void deleteContainer(String containerName);

    /**
     * Delete items matching query
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return Flux of results
     */
    <T> Flux<T> delete(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Find items
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return Flux of results
     */
    <T> Flux<T> find(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Exists
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @return Mono of result
     */
    Mono<Boolean> exists(CosmosQuery query, Class<?> domainType, String containerName);

    /**
     * Exists
     * @param id the id
     * @param domainType type class
     * @param containerName the container name
     * @return Mono of result
     */
    Mono<Boolean> existsById(Object id, Class<?> domainType, String containerName);

    /**
     * Count
     *
     * @param containerName the container name
     * @return Mono of result
     */
    Mono<Long> count(String containerName);

    /**
     * Count
     *
     * @param query the document query
     * @param containerName the container name
     * @return Mono of result
     */
    Mono<Long> count(CosmosQuery query, String containerName);

    /**
     * Count
     *
     * @param querySpec the document query spec
     * @param containerName the container name
     * @return Mono of result
     */
    Mono<Long> count(SqlQuerySpec querySpec, String containerName);

    /**
     * To get converter
     * @return MappingCosmosConverter
     */
    MappingCosmosConverter getConverter();

    /**
     * Run the query.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param domainType the domain type
     * @param returnType the return type
     * @return the flux
     */
    <T> Flux<T> runQuery(SqlQuerySpec querySpec, Class<?> domainType, Class<T> returnType);

    /**
     * Run the query.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param sort the sort order
     * @param domainType the domain type
     * @param returnType the return type
     * @return the flux
     */
    <T> Flux<T> runQuery(SqlQuerySpec querySpec, Sort sort, Class<?> domainType, Class<T> returnType);

}
