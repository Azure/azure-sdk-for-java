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

/**
 * Interface for cosmosdb operations
 */
public interface CosmosOperations {

    /**
     * Use getContainerName() instead
     * @param domainType class type
     * @return container name
     * @deprecated Use {@link #getContainerName(Class)} instead
     */
    @Deprecated
    String getCollectionName(Class<?> domainType);

    /**
     * To get container name by domaintype
     * @param domainType class type
     * @return String
     */
    String getContainerName(Class<?> domainType);

    /**
     * Use createContainerIfNotExists() instead
     * @param information cosmos entity information
     * @return created container properties
     * @deprecated Use {@link #createContainerIfNotExists(CosmosEntityInformation)} instead
     */
    @Deprecated
    CosmosContainerProperties createCollectionIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Creates container if not exists
     * @param information CosmosEntityInformation
     * @return CosmosContainerProperties
     */
    CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Find the DocumentQuery, find all the items specified by domain type.
     *
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    <T> List<T> findAll(Class<T> domainType);

    /**
     * Find the DocumentQuery, find all the items specified by domain type in the given container.
     *
     * @param containerName the container name
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    <T> List<T> findAll(String containerName, Class<T> domainType);

    /**
     * Find the DocumentQuery, find all the items specified by domain type in the given container.
     *
     * @param partitionKey the partition key
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    <T> List<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    /**
     * Finds item by id
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    <T> T findById(Object id, Class<T> domainType);

    /**
     * Finds item by id
     * @param containerName must not be {@literal null}
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    <T> T findById(String containerName, Object id, Class<T> domainType);

    /**
     * Finds item by id
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    <T> T findById(Object id, Class<T> domainType, PartitionKey partitionKey);

    /**
     * Inserts item
     *
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    <T> T insert(T objectToSave, PartitionKey partitionKey);

    /**
     * Inserts item
     *
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    <T> T insert(String containerName, T objectToSave, PartitionKey partitionKey);

    /**
     * Upserts an item with partition key
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     */
    <T> void upsert(T object, PartitionKey partitionKey);

    /**
     * Upserts an item into container with partition key
     * @param containerName the container name
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     */
    <T> void upsert(String containerName, T object, PartitionKey partitionKey);

    /**
     * Upserts an item and return item properties
     * @param containerName the container name
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     * @return upsert object entity
     */
    <T> T upsertAndReturnEntity(String containerName, T object, PartitionKey partitionKey);

    /**
     * Delete an item by id
     *
     * @param containerName the container name
     * @param id the id
     * @param partitionKey the partition key
     */
    void deleteById(String containerName, Object id, PartitionKey partitionKey);

    /**
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param domainType the partition key path
     */
    void deleteAll(String containerName, Class<?> domainType);

    /**
     * Use deleteContainer() instead
     * @param containerName container name
     * @deprecated Use {@link #deleteContainer(String)} instead.
     */
    @Deprecated
    void deleteCollection(String containerName);

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
     * @return deleted items in a List
     */
    <T> List<T> delete(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Find query
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domaintype
     * @return found results in a List
     */
    <T> List<T> find(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Find by ids
     *
     * @param ids iterable of ids
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type of domainType
     * @param <ID> type of ID
     * @return Mono
     */
    <T, ID> List<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName);

    /**
     * Exists
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type of domainType
     * @return Boolean
     */
    <T> Boolean exists(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Find all items in a given container with partition key
     *
     * @param pageable Pageable object
     * @param domainType the domainType
     * @param containerName the container name
     * @param <T> type of domainType
     * @return Page
     */
    <T> Page<T> findAll(Pageable pageable, Class<T> domainType, String containerName);

    /**
     * Pagination query
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domaintype
     * @return Page
     */
    <T> Page<T> paginationQuery(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * Count
     *
     * @param containerName the container name
     * @return count result
     */
    long count(String containerName);

    /**
     * Count
     *
     * @param query the document query
     * @param domainType the domain type
     * @param containerName the container name
     * @param <T> type class of domaintype
     * @return count result
     */
    <T> long count(DocumentQuery query, Class<T> domainType, String containerName);

    /**
     * To get converter
     * @return MappingCosmosConverter
     */
    MappingCosmosConverter getConverter();
}
