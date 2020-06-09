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

public interface ReactiveCosmosOperations {

    String getContainerName(Class<?> domainType);

    /**
     * Use createContainerIfNotExists() instead
     * @param information cosmos entity information
     * @return Mono of cosmos container response
     */
    @Deprecated
    Mono<CosmosContainerResponse> createCollectionIfNotExists(CosmosEntityInformation information);

    Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation information);

    <T> Flux<T> findAll(String containerName, Class<T> domainType);

    <T> Flux<T> findAll(Class<T> domainType);

    <T> Flux<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    <T> Mono<T> findById(Object id, Class<T> domainType);

    <T> Mono<T> findById(String containerName, Object id, Class<T> domainType);

    <T> Mono<T> findById(Object id, Class<T> domainType, PartitionKey partitionKey);

    <T> Mono<T> insert(T objectToSave, PartitionKey partitionKey);

    <T> Mono<T> insert(String containerName, Object objectToSave, PartitionKey partitionKey);

    <T> Mono<T> upsert(T object, PartitionKey partitionKey);

    <T> Mono<T> upsert(String containerName, T object, PartitionKey partitionKey);

    Mono<Void> deleteById(String containerName, Object id, PartitionKey partitionKey);

    Mono<Void> deleteAll(String containerName, String partitionKey);

    void deleteContainer(String containerName);

    <T> Flux<T> delete(DocumentQuery query, Class<T> domainType, String containerName);

    <T> Flux<T> find(DocumentQuery query, Class<T> domainType, String containerName);

    Mono<Boolean> exists(DocumentQuery query, Class<?> domainType, String containerName);

    Mono<Boolean> existsById(Object id, Class<?> domainType, String containerName);

    Mono<Long> count(String containerName);

    Mono<Long> count(DocumentQuery query, String containerName);

    MappingCosmosConverter getConverter();
}
