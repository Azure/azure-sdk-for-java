// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

import static com.azure.spring.data.cosmos.repository.support.IndexPolicyCompareService.policyNeedsUpdate;

/**
 * Repository class for simple reactive Cosmos operation
 */
public class SimpleReactiveCosmosRepository<T, K extends Serializable> implements ReactiveCosmosRepository<T, K> {

    private final CosmosEntityInformation<T, K> entityInformation;
    private final ReactiveCosmosOperations cosmosOperations;

    /**
     * Initialization with metadata and reactiveCosmosOperations
     *
     * @param metadata for entityInformation
     * @param reactiveCosmosOperations for cosmosOperations
     */
    public SimpleReactiveCosmosRepository(CosmosEntityInformation<T, K> metadata,
                                          ReactiveCosmosOperations reactiveCosmosOperations) {
        this.cosmosOperations = reactiveCosmosOperations;
        this.entityInformation = metadata;

        if (this.entityInformation.isAutoCreateContainer()) {
            createContainerIfNotExists();
        }

        CosmosContainerProperties currentProperties = getContainerProperties();
        if (currentProperties != null
            && entityInformation.isIndexingPolicySpecified()
            && policyNeedsUpdate(currentProperties.getIndexingPolicy(), entityInformation.getIndexingPolicy())) {
            currentProperties.setIndexingPolicy(entityInformation.getIndexingPolicy());
            replaceContainerProperties(currentProperties);
        }
    }

    private CosmosContainerProperties getContainerProperties() {
        try {
            return this.cosmosOperations.getContainerProperties(this.entityInformation.getContainerName()).block();
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) {
                return null;
            } else {
                throw ex;
            }
        }
    }

    private CosmosContainerProperties replaceContainerProperties(CosmosContainerProperties properties) {
        return this.cosmosOperations.replaceContainerProperties(this.entityInformation.getContainerName(), properties)
            .block();
    }

    private CosmosContainerResponse createContainerIfNotExists() {
        return this.cosmosOperations.createContainerIfNotExists(this.entityInformation).block();
    }

    @Override
    public Flux<T> findAll(Sort sort) {
        Assert.notNull(sort, "Sort must not be null!");

        final CosmosQuery query =
            new CosmosQuery(Criteria.getInstance(CriteriaType.ALL)).with(sort);

        return cosmosOperations.find(query, entityInformation.getJavaType(),
            entityInformation.getContainerName());
    }

    @Override
    public Flux<T> findAll(PartitionKey partitionKey) {
        return cosmosOperations.findAll(partitionKey, entityInformation.getJavaType());
    }

    @Override
    public <S extends T> Mono<S> save(S entity) {

        Assert.notNull(entity, "Entity must not be null!");

        if (entityInformation.isNew(entity)) {
            return cosmosOperations.insert(entityInformation.getContainerName(), entity);
        } else {
            return cosmosOperations.upsert(entityInformation.getContainerName(), entity);
        }
    }

    @Override
    public <S extends T> Flux<S> saveAll(Iterable<S> entities) {

        Assert.notNull(entities, "The given Iterable of entities must not be null!");

        return Flux.fromIterable(entities).flatMap(this::save);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {

        Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

        return Flux.from(entityStream).flatMap(this::save);
    }

    @Override
    public Mono<T> findById(K id) {
        Assert.notNull(id, "The given id must not be null!");
        return cosmosOperations.findById(entityInformation.getContainerName(), id,
            entityInformation.getJavaType());
    }

    @Override
    public Mono<T> findById(Publisher<K> publisher) {
        Assert.notNull(publisher, "The given id must not be null!");

        return Mono.from(publisher).flatMap(
            id -> cosmosOperations.findById(entityInformation.getContainerName(),
                id, entityInformation.getJavaType()));
    }

    @Override
    public Mono<T> findById(K id, PartitionKey partitionKey) {
        Assert.notNull(id, "The given id must not be null!");
        return cosmosOperations.findById(id,
            entityInformation.getJavaType(), partitionKey);
    }

    @Override
    public Mono<Boolean> existsById(K id) {
        Assert.notNull(id, "The given id must not be null!");

        return cosmosOperations.existsById(id, entityInformation.getJavaType(),
            entityInformation.getContainerName());
    }

    @Override
    public Mono<Boolean> existsById(Publisher<K> publisher) {
        Assert.notNull(publisher, "The given id must not be null!");

        return Mono.from(publisher).flatMap(id -> cosmosOperations.existsById(id,
            entityInformation.getJavaType(),
            entityInformation.getContainerName()));
    }

    @Override
    public Flux<T> findAll() {
        return cosmosOperations.findAll(entityInformation.getContainerName(),
            entityInformation.getJavaType());
    }

    @Override
    public Flux<T> findAllById(Iterable<K> ids) {
        Assert.notNull(ids, "Iterable ids should not be null");
        throw new UnsupportedOperationException();
    }

    @Override
    public Flux<T> findAllById(Publisher<K> ids) {
        Assert.notNull(ids, "The given Publisher of Id's must not be null!");
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Long> count() {
        return cosmosOperations.count(entityInformation.getContainerName());
    }

    @Override
    public Mono<Void> deleteById(K id) {
        Assert.notNull(id, "The given id must not be null!");

        return cosmosOperations.deleteById(entityInformation.getContainerName(), id, null);
    }

    @Override
    public Mono<Void> deleteById(Publisher<K> publisher) {
        Assert.notNull(publisher, "Id must not be null!");

        return Mono.from(publisher).flatMap(id -> cosmosOperations.deleteById(entityInformation.getContainerName(),
            id, null)).then();
    }

    @Override
    public Mono<Void> deleteById(K id, PartitionKey partitionKey) {
        Assert.notNull(id, "Id must not be null!");
        Assert.notNull(partitionKey, "PartitionKey must not be null!");

        return cosmosOperations.deleteById(entityInformation.getContainerName(), id, partitionKey);

    }

    @Override
    public Mono<Void> delete(@NonNull T entity) {
        Assert.notNull(entity, "entity to be deleted must not be null!");

        return cosmosOperations.deleteEntity(entityInformation.getContainerName(), entity);
    }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends K> ids) {
        Assert.notNull(ids, "The given Iterable of ids must not be null!");
        return Flux.fromIterable(ids).flatMap(this::deleteById).then();
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null!");

        return Flux.fromIterable(entities).flatMap(this::delete).then();
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {

        Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

        return Flux.from(entityStream)//
                   .map(entityInformation::getRequiredId)//
                   .flatMap(this::deleteById)//
                   .then();
    }

    @Override
    public Mono<Void> deleteAll() {
        return cosmosOperations.deleteAll(entityInformation.getContainerName(), entityInformation.getJavaType());
    }

}
