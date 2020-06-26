// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.core.CosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Repository class for simple Cosmos operation
 */
public class SimpleCosmosRepository<T, ID extends Serializable> implements CosmosRepository<T, ID> {

    private final CosmosOperations operation;
    private final CosmosEntityInformation<T, ID> information;

    /**
     * Initialization
     *
     * @param metadata for cosmos entity information
     * @param applicationContext to get bean of CosmosOperations class
     */
    public SimpleCosmosRepository(CosmosEntityInformation<T, ID> metadata,
                                  ApplicationContext applicationContext) {
        this.operation = applicationContext.getBean(CosmosOperations.class);
        this.information = metadata;

        if (this.information.isAutoCreateContainer()) {
            createContainerIfNotExists();
        }
    }

    /**
     * Initialization
     *
     * @param metadata for cosmos entity information
     * @param dbOperations for cosmosdb operation
     */
    public SimpleCosmosRepository(CosmosEntityInformation<T, ID> metadata,
                                  CosmosOperations dbOperations) {
        this.operation = dbOperations;
        this.information = metadata;

        if (this.information.isAutoCreateContainer()) {
            createContainerIfNotExists();
        }
    }

    private CosmosContainerProperties createContainerIfNotExists() {
        return this.operation.createContainerIfNotExists(this.information);
    }

    /**
     * save entity without partition
     *
     * @param entity to be saved
     * @param <S> type of entity
     * @return entity
     */
    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "entity must not be null");

        // save entity
        if (information.isNew(entity)) {
            return operation.insert(information.getContainerName(),
                    entity,
                    createKey(information.getPartitionKeyFieldValue(entity)));
        } else {
            return operation.upsertAndReturnEntity(information.getContainerName(),
                    entity, createKey(information.getPartitionKeyFieldValue(entity)));
        }
    }

    private PartitionKey createKey(String partitionKeyValue) {
        if (StringUtils.isEmpty(partitionKeyValue)) {
            return PartitionKey.None;
        }

        return new PartitionKey(partitionKeyValue);
    }

    /**
     * batch save entities
     *
     * @param entities Batch entities
     * @param <S> type of entities
     * @return return the saved entities
     */
    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "Iterable entities should not be null");

        final List<S> savedEntities = new ArrayList<>();
        entities.forEach(entity -> {
            final S savedEntity = this.save(entity);
            savedEntities.add(savedEntity);
        });

        return savedEntities;
    }

    /**
     * find all entities from one container without configuring partition key value
     *
     * @return return Iterable of the found entities List
     */
    @Override
    public Iterable<T> findAll() {
        return operation.findAll(information.getContainerName(), information.getJavaType());
    }

    /**
     * find entities based on id list from one container without partitions
     *
     * @param ids id list used to find entities
     * @return return a List of all found entities
     */
    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "Iterable ids should not be null");

        return operation.findByIds(ids, information.getJavaType(), information.getContainerName());
    }

    /**
     * find one entity per id without partitions
     *
     * @param id an id used to find entity
     * @return return the searching result
     */
    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "id must not be null");

        if (id instanceof String
                && !StringUtils.hasText((String) id)) {
            return Optional.empty();
        }

        return Optional.ofNullable(operation.findById(information.getContainerName(), id, information.getJavaType()));
    }

    @Override
    public Optional<T> findById(ID id, PartitionKey partitionKey) {
        Assert.notNull(id, "id must not be null");

        if (id instanceof String
                && !StringUtils.hasText((String) id)) {
            return Optional.empty();
        }

        return Optional.ofNullable(operation.findById(id, information.getJavaType(), partitionKey));
    }

    /**
     * return count of documents in one container without partitions
     *
     * @return count of documents in one container without partitions
     */
    @Override
    public long count() {
        return operation.count(information.getContainerName());
    }

    /**
     * delete one document per id without configuring partition key value
     *
     * @param id an id used to specify the deleted document
     */
    @Override
    public void deleteById(ID id) {
        Assert.notNull(id, "id to be deleted should not be null");

        operation.deleteById(information.getContainerName(), id, null);
    }

    @Override
    public void deleteById(ID id, PartitionKey partitionKey) {
        Assert.notNull(id, "id to be deleted should not be null");
        Assert.notNull(partitionKey, "partitionKey to be deleted should not be null");

        operation.deleteById(information.getContainerName(), id, partitionKey);
    }

    /**
     * delete one document per entity
     *
     * @param entity the entity used to specify a document
     */
    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "entity to be deleted should not be null");

        final String partitionKeyValue = information.getPartitionKeyFieldValue(entity);

        operation.deleteById(information.getContainerName(),
                information.getId(entity),
                partitionKeyValue == null ? null : new PartitionKey(partitionKeyValue));
    }

    /**
     * delete all the domains of a container
     */
    @Override
    public void deleteAll() {
        operation.deleteAll(information.getContainerName(), information.getJavaType());
    }

    /**
     * delete list of entities without partitions
     *
     * @param entities list of entities to be deleted
     */
    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Iterable entities should not be null");

        StreamSupport.stream(entities.spliterator(), true).forEach(this::delete);
    }

    /**
     * check if an entity exists per id without partition
     *
     * @param primaryKey an id to specify an entity
     * @return if the entity exists
     */
    @Override
    public boolean existsById(ID primaryKey) {
        Assert.notNull(primaryKey, "primaryKey should not be null");

        return findById(primaryKey).isPresent();
    }

    /**
     * Returns all entities sorted by the given options.
     *
     * @param sort the Sort option for queries.
     * @return all entities sorted by the given options
     */
    @Override
    public Iterable<T> findAll(@NonNull Sort sort) {
        Assert.notNull(sort, "sort of findAll should not be null");
        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL)).with(sort);

        return operation.find(query, information.getJavaType(), information.getContainerName());
    }

    /**
     * FindQuerySpecGenerator
     * Returns a Page of entities meeting the paging restriction provided in the Pageable object.
     *
     * @param pageable the Pageable object providing paging restriction
     * @return a page of entities
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        Assert.notNull(pageable, "pageable should not be null");

        return operation.findAll(pageable, information.getJavaType(), information.getContainerName());
    }

    @Override
    public List<T> findAll(PartitionKey partitionKey) {
        return operation.findAll(partitionKey, information.getJavaType());
    }
}
