// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.core.query.QueryByExampleCriteriaBuilder;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.reactivestreams.Publisher;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.azure.spring.data.cosmos.repository.support.IndexPolicyCompareService.policyNeedsUpdate;

/**
 * Repository class for simple reactive Cosmos operation
 *
 * @param <T> the type of the domain class managed by this repository.
 * @param <K> the type of the id of the domain class managed by this repository.
 */
public class SimpleReactiveCosmosRepository<T, K extends Serializable> implements ReactiveCosmosRepository<T, K>, ReactiveQueryByExampleExecutor<T> {

    private static final String EXAMPLE_MUST_NOT_BE_NULL = "Example must not be null";
    private static final String QUERY_FUNCTION_MUST_NOT_BE_NULL = "Query function must not be null";
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

        if (this.entityInformation.isOverwriteIndexingPolicy()) {
            overwriteIndexingPolicy();
        }
    }

    private void overwriteIndexingPolicy() {
        CosmosContainerProperties currentProperties = getContainerProperties();
        if (currentProperties != null
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
    public <S extends T> Mono<S> save(K id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations) {
        Assert.notNull(id, "entity must not be null");
        // patch items
        return cosmosOperations.patch(id, partitionKey, domainType, patchOperations);
    }

    @Override
    public <S extends T> Mono<S> save(K id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options) {
        Assert.notNull(id, "entity must not be null");
        // patch items
        return cosmosOperations.patch(id, partitionKey, domainType, patchOperations, options);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Iterable<S> entities) {

        Assert.notNull(entities, "The given Iterable of entities must not be null!");

        if (entityInformation.getPartitionKeyFieldName() != null) {
            return cosmosOperations.insertAll(this.entityInformation, entities);
        } else {
            return Flux.fromIterable(entities).flatMap(this::save);
        }
    }

    @Override
    public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {

        Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

        if (entityInformation.getPartitionKeyFieldName() != null) {
            return cosmosOperations.insertAll(this.entityInformation, Flux.from(entityStream));
        } else {
            return Flux.from(entityStream).flatMap(this::save);
        }
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

        if (entityInformation.getPartitionKeyFieldName() != null) {
            return cosmosOperations.deleteEntities(this.entityInformation, entities);
        } else {
            return Flux.fromIterable(entities).flatMap(this::delete).then();
        }
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {

        Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

        if (entityInformation.getPartitionKeyFieldName() != null) {
            return cosmosOperations.deleteEntities(this.entityInformation, Flux.from(entityStream));
        } else {
            return Flux.from(entityStream)
                .map(entityInformation::getRequiredId)
                .flatMap(this::deleteById)
                .then();
        }
    }

    @Override
    public Mono<Void> deleteAll() {
        return cosmosOperations.deleteAll(entityInformation.getContainerName(), entityInformation.getJavaType());
    }

    @Override
    public <S extends T> Mono<S> findOne(Example<S> example) {
        return new ReactiveFluentQueryByExample<>(example, example.getProbeType()).one();
    }

    @Override
    public <S extends T> Flux<S> findAll(Example<S> example) {
        return new ReactiveFluentQueryByExample<>(example, example.getProbeType()).all();
    }

    @Override
    public <S extends T> Flux<S> findAll(Example<S> example, Sort sort) {
        return new ReactiveFluentQueryByExample<>(example, example.getProbeType()).sortBy(sort).all();
    }

    @Override
    public <S extends T> Mono<Long> count(Example<S> example) {
        return new ReactiveFluentQueryByExample<>(example, example.getProbeType()).count();
    }

    @Override
    public <S extends T> Mono<Boolean> exists(Example<S> example) {
        return new ReactiveFluentQueryByExample<>(example, example.getProbeType()).exists();
    }

    @Override
    public <S extends T, R, P extends Publisher<R>> P findBy(Example<S> example, Function<FluentQuery.ReactiveFluentQuery<S>, P> queryFunction) {

        Assert.notNull(example, EXAMPLE_MUST_NOT_BE_NULL);
        Assert.notNull(queryFunction, QUERY_FUNCTION_MUST_NOT_BE_NULL);

        return queryFunction.apply(new ReactiveFluentQueryByExample<>(example, example.getProbeType()));
    }

    /**
     * {@link org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery} using {@link Example}.
     */
    class ReactiveFluentQueryByExample<S, T> extends ReactiveFluentQuerySupport<Example<S>, T> {

        ReactiveFluentQueryByExample(Example<S> example, Class<T> resultType) {
            this(example, Sort.unsorted(), 0, resultType, Collections.emptyList());
        }

        ReactiveFluentQueryByExample(Example<S> example, Sort sort, int limit, Class<T> resultType, List<String> fieldsToInclude) {
            super(example, sort, limit, resultType, fieldsToInclude);
        }

        @Override
        protected <R> ReactiveFluentQueryByExample<S, R> create(Example<S> predicate, Sort sort, int limit, Class<R> resultType,
                                                                List<String> fieldsToInclude) {
            return new ReactiveFluentQueryByExample<>(predicate, sort, limit, resultType, fieldsToInclude);
        }

        @Override
        public Mono<T> one() {
            Flux<T> result = cosmosOperations.find(createQuery(), getResultType(), entityInformation.getContainerName());
            return result.collectList().flatMap(it -> {

                if (it.isEmpty()) {
                    return Mono.empty();
                }

                if (it.size() > 1) {
                    return Mono.error(
                        new IncorrectResultSizeDataAccessException("Query " + getResultType() + " returned non unique result", 1));
                }

                return Mono.just(it.get(0));
            });
        }

        @Override
        public Mono<T> first() {
            return this.all().next();
        }

        @Override
        public Flux<T> all() {
            return cosmosOperations.find(createQuery(), getResultType(), entityInformation.getContainerName());
        }

        @Override
        public Mono<Page<T>> page(Pageable pageable) {

            Assert.notNull(pageable, "Pageable must not be null");

            Mono<List<T>> items = cosmosOperations.find(
                    createQuery(q -> q.with(pageable)),
                    getResultType(),
                    entityInformation.getContainerName())
                .collectList();

            return items.flatMap(content -> ReactivePageableExecutionUtils.getPage(content, pageable, this.count()));
        }

        @Override
        public Mono<Long> count() {
            return cosmosOperations.count(createQuery(), entityInformation.getContainerName());
        }

        @Override
        public Mono<Boolean> exists() {
            return cosmosOperations.exists(createQuery(), entityInformation.getJavaType(), entityInformation.getContainerName());
        }

        private CosmosQuery createQuery() {
            return createQuery(UnaryOperator.identity());
        }

        private CosmosQuery createQuery(UnaryOperator<CosmosQuery> queryCustomizer) {

            Criteria predicate = QueryByExampleCriteriaBuilder.getPredicate(getPredicate());
            CosmosQuery query = new CosmosQuery(predicate);

            if (getSort().isSorted()) {
                query.with(getSort());
            }

            query.withLimit(getLimit());

            query = queryCustomizer.apply(query);

            return query;
        }

    }
}
