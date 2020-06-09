// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CosmosOperations {

    /**
     * Use getContainerName() instead
     * @param domainType class type
     * @return container name
     */
    @Deprecated
    String getCollectionName(Class<?> domainType);

    String getContainerName(Class<?> domainType);

    /**
     * Use createContainerIfNotExists() instead
     * @param information cosmos entity information
     * @return created container properties
     */
    @Deprecated
    CosmosContainerProperties createCollectionIfNotExists(CosmosEntityInformation<?, ?> information);

    CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information);

    <T> List<T> findAll(Class<T> domainType);

    <T> List<T> findAll(String containerName, Class<T> domainType);

    <T> List<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    <T> T findById(Object id, Class<T> domainType);

    <T> T findById(String containerName, Object id, Class<T> domainType);

    <T> T findById(Object id, Class<T> domainType, PartitionKey partitionKey);

    <T> T insert(T objectToSave, PartitionKey partitionKey);

    <T> T insert(String containerName, T objectToSave, PartitionKey partitionKey);

    <T> void upsert(T object, PartitionKey partitionKey);

    <T> void upsert(String containerName, T object, PartitionKey partitionKey);

    <T> T upsertAndReturnEntity(String containerName, T object, PartitionKey partitionKey);

    void deleteById(String containerName, Object id, PartitionKey partitionKey);

    void deleteAll(String containerName, Class<?> domainType);

    /**
     * Use deleteContainer() instead
     * @param containerName container name
     */
    @Deprecated
    void deleteCollection(String containerName);

    void deleteContainer(String containerName);

    <T> List<T> delete(DocumentQuery query, Class<T> domainType, String containerName);

    <T> List<T> find(DocumentQuery query, Class<T> domainType, String containerName);

    <T, ID> List<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName);

    <T> Boolean exists(DocumentQuery query, Class<T> domainType, String containerName);

    <T> Page<T> findAll(Pageable pageable, Class<T> domainType, String containerName);

    <T> Page<T> paginationQuery(DocumentQuery query, Class<T> domainType, String containerName);

    long count(String containerName);

    <T> long count(DocumentQuery query, Class<T> domainType, String containerName);

    MappingCosmosConverter getConverter();
}
