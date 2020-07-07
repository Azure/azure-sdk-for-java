// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
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
     * Use createContainerIfNotExists() instead
     * @param information cosmos entity information
     * @return Mono of cosmos container response
     * @deprecated use {@link #createContainerIfNotExists(CosmosEntityInformation)} instead.
     */
    @Deprecated
    Mono<CosmosContainerResponse> createCollectionIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Creates a container if it doesn't already exist
     *
     * @param information the CosmosEntityInformation
     * @return Mono
     */
    Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Find all items in a given container
     *
     * @param containerName the containerName
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux
     */
    <T> Flux<T> findAll(String containerName, Class<T> domainType);

    /**
     * Find all items in a given container
     *
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux
     */
    <T> Flux<T> findAll(Class<T> domainType);

    /**
     * Find all items in a given container with partition key
     *
     * @param partitionKey partition Key
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Flux
     */
    <T> Flux<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType the domainType
     * @param <T> type of domainType
     * @return Mono
     */
    <T> Mono<T> findById(Object id, Class<T> domainType);

    /**
     * Find by id
     *
     * @param containerName the containername
     * @param id the id
     * @param domainType type class
     * @param <T> type of domainType
     * @return Mono
     */
    <T> Mono<T> findById(String containerName, Object id, Class<T> domainType);

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType type class
     * @param partitionKey partition Key
     * @param <T> type of domainType
     * @return Mono
     */
    <T> Mono<T> findById(Object id, Class<T> domainType, PartitionKey partitionKey);

    /**
     * Insert
     *
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @param <T> type of inserted objectToSave
     * @return Mono
     */
    <T> Mono<T> insert(T objectToSave, PartitionKey partitionKey);

    /**
     * Insert
     *
     * @param <T> type of inserted objectToSave
     * @param containerName the container name
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @return Mono
     */
    <T> Mono<T> insert(String containerName, Object objectToSave, PartitionKey partitionKey);

    /**
     * Upsert
     *
     * @param object the object to upsert
     * @param partitionKey the partition key
     * @param <T> type class of object
     * @return Mono
     */
    <T> Mono<T> upsert(T object, PartitionKey partitionKey);

    /**
     * Upsert
     *
     * @param containerName the container name
     * @param object the object to save
     * @param partitionKey the partition key
     * @param <T> type class of object
     * @return Mono
     */
    <T> Mono<T> upsert(String containerName, T object, PartitionKey partitionKey);

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
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param partitionKey the partition key path
     * @return void Mono
     */
    Mono<Void> deleteAll(String containerName, String partitionKey);

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
     * @param <T> type class of domaintype
     * @return Flux
     */
    <T> Flux<T> delete(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Find items
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domaintype
     * @return Flux
     */
    <T> Flux<T> find(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Exists
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @return Mono
     */
    Mono<Boolean> exists(DocumentQuery query, Class<?> domainType, String containerName);

    /**
     * Exists
     * @param id the id
     * @param domainType type class
     * @param containerName the containercontainer nam,e
     * @return Mono
     */
    Mono<Boolean> existsById(Object id, Class<?> domainType, String containerName);

    /**
     * Count
     *
     * @param containerName the container name
     * @return Mono
     */
    Mono<Long> count(String containerName);

    /**
     * Count
     *
     * @param query the document query
     * @param containerName the container name
     * @return Mono
     */
    Mono<Long> count(DocumentQuery query, String containerName);

    /**
     * To get converter
     * @return MappingCosmosConverter
     */
    MappingCosmosConverter getConverter();
}
