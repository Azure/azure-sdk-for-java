// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

/**
 * Interface for cosmosDB operations
 */
public interface CosmosOperations {

    /**
     * To get container name by domainType
     *
     * @param domainType class type
     * @return String
     */
    String getContainerName(Class<?> domainType);

    /**
     * Creates container if not exists
     *
     * @param information CosmosEntityInformation
     * @return CosmosContainerProperties
     */
    CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information);

    /**
     * Get properties for specified container
     *
     * @param containerName String
     * @return CosmosContainerProperties
     */
    CosmosContainerProperties getContainerProperties(String containerName);

    /**
     * Replace container properties for the specified container
     *
     * @param containerName String
     * @param properties CosmosContainerProperties
     * @return CosmosContainerProperties
     */
    CosmosContainerProperties replaceContainerProperties(String containerName, CosmosContainerProperties properties);

    /**
     * Find the DocumentQuery, find all the items specified by domain type.
     *
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return results in an Iterable
     */
    <T> Iterable<T> findAll(Class<T> domainType);

    /**
     * Find the DocumentQuery, find all the items specified by domain type in the given container.
     *
     * @param containerName the container name
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return results in an Iterable
     */
    <T> Iterable<T> findAll(String containerName, Class<T> domainType);

    /**
     * Find the DocumentQuery, find all the items specified by domain type in the given container.
     *
     * @param partitionKey the partition key
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return results in an Iterable
     */
    <T> Iterable<T> findAll(PartitionKey partitionKey, Class<T> domainType);

    /**
     * Finds item by id
     *
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    <T> T findById(Object id, Class<T> domainType);

    /**
     * Finds item by id
     *
     * @param containerName must not be {@literal null}
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    <T> T findById(String containerName, Object id, Class<T> domainType);

    /**
     * Finds item by id
     *
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
     * Inserts item
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    <T> T insert(String containerName, T objectToSave);

    /**
     * Upserts an item with partition key
     *
     * @param object upsert object
     * @param <T> type of upsert object
     */
    <T> void upsert(T object);

    /**
     * Upserts an item into container with partition key
     *
     * @param containerName the container name
     * @param object upsert object
     * @param <T> type of upsert object
     */
    <T> void upsert(String containerName, T object);

    /**
     * Upserts an item and return item properties
     *
     * @param containerName the container name
     * @param object upsert object
     * @param <T> type of upsert object
     * @return upsert object entity
     */
    <T> T upsertAndReturnEntity(String containerName, T object);

    /**
     * Delete an item by id
     *
     * @param containerName the container name
     * @param id the id
     * @param partitionKey the partition key
     */
    void deleteById(String containerName, Object id, PartitionKey partitionKey);

    /**
     * Delete using entity
     *
     * @param <T> type class of domain type
     * @param containerName the container name
     * @param entity the entity object
     */
    <T> void deleteEntity(String containerName, T entity);

    /**
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param domainType the domainType
     */
    void deleteAll(String containerName, Class<?> domainType);

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
     * @return deleted items in a Iterable
     */
    <T> Iterable<T> delete(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Find query
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return results in an Iterable
     */
    <T> Iterable<T> find(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Find by ids
     *
     * @param ids iterable of ids
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type of domainType
     * @param <ID> type of ID
     * @return results in an Iterable
     */
    <T, ID> Iterable<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName);

    /**
     * Exists
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type of domainType
     * @return Boolean
     */
    <T> Boolean exists(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Find all items in a given container with partition key
     *
     * @param pageable Pageable object
     * @param domainType the domainType
     * @param containerName the container name
     * @param <T> type of domainType
     * @return results as Page
     */
    <T> Page<T> findAll(Pageable pageable, Class<T> domainType, String containerName);

    /**
     * Pagination query
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return results as Page
     */
    <T> Page<T> paginationQuery(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Slice query
     *
     * @param query the document query
     * @param domainType type class
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return results as Slice
     */
    <T> Slice<T> sliceQuery(CosmosQuery query, Class<T> domainType, String containerName);

    /**
     * Run custom SQL query
     *
     * @param querySpec the query spec
     * @param pageable the pageable
     * @param domainType the domain type
     * @param returnType the return type
     * @param <T> the type parameter
     * @return the Page
     */
    <T> Slice<T> runSliceQuery(SqlQuerySpec querySpec, Pageable pageable, Class<?> domainType, Class<T> returnType);

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
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return count result
     */
    <T> long count(CosmosQuery query, String containerName);

    /**
     * Count
     *
     * @param querySpec the document query spec
     * @param containerName the container name
     * @param <T> type class of domainType
     * @return count result
     */
    <T> long count(SqlQuerySpec querySpec, String containerName);

    /**
     * To get converter
     *
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
     * @return the Iterable
     */
    <T> Iterable<T> runQuery(SqlQuerySpec querySpec, Class<?> domainType, Class<T> returnType);

    /**
     * Run the query.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param sort the sort order
     * @param domainType the domain type
     * @param returnType the return type
     * @return the Iterable
     */
    <T> Iterable<T> runQuery(SqlQuerySpec querySpec, Sort sort, Class<?> domainType, Class<T> returnType);

    /**
     * Run the query.
     *
     * @param <T> the type parameter
     * @param querySpec the query spec
     * @param pageable the pageable
     * @param domainType the domain type
     * @param returnType the return type
     * @return the Page
     */
    <T> Page<T> runPaginationQuery(SqlQuerySpec querySpec, Pageable pageable, Class<?> domainType, Class<T> returnType);

}
