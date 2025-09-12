// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.core.query.QueryByExampleCriteriaBuilder;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.spring.data.cosmos.repository.support.IndexPolicyCompareService.policyNeedsUpdate;

/**
 * Repository class for simple Cosmos operation
 *
 * @param <T> domain type.
 * @param <ID> id type.
 */
public class SimpleCosmosRepository<T, ID extends Serializable> implements CosmosRepository<T, ID>, QueryByExampleExecutor<T> {

    private static final String EXAMPLE_MUST_NOT_BE_NULL = "Example must not be null";
    private static final String QUERY_FUNCTION_MUST_NOT_BE_NULL = "Query function must not be null";

    private final CosmosOperations operation;
    private final CosmosEntityInformation<T, ID> information;
    private ProjectionFactory projectionFactory;

    /**
     * Initialization
     *
     * @param metadata for cosmos entity information
     * @param dbOperations for cosmosDB operation
     */
    public SimpleCosmosRepository(CosmosEntityInformation<T, ID> metadata,
                                  CosmosOperations dbOperations) {
        this.operation = dbOperations;
        this.information = metadata;

        if (this.information.isAutoCreateContainer()) {
            createContainerIfNotExists();
        }

        if (this.information.isOverwriteIndexingPolicy()) {
            overwriteIndexingPolicy();
        }
    }

    private void overwriteIndexingPolicy() {
        CosmosContainerProperties currentProperties = getContainerProperties();
        if (currentProperties != null
            && policyNeedsUpdate(currentProperties.getIndexingPolicy(), information.getIndexingPolicy())) {
            currentProperties.setIndexingPolicy(information.getIndexingPolicy());
            replaceContainerProperties(currentProperties);
        }
    }

    private CosmosContainerProperties getContainerProperties() {
        try {
            return this.operation.getContainerProperties(this.information.getContainerName());
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) {
                return null;
            } else {
                throw ex;
            }
        }
    }

    private CosmosContainerProperties replaceContainerProperties(CosmosContainerProperties properties) {
        return this.operation.replaceContainerProperties(this.information.getContainerName(), properties);
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
            return operation.insert(information.getContainerName(), entity);
        } else {
            return operation.upsertAndReturnEntity(information.getContainerName(), entity);
        }
    }

    /**
     * patch entity with CosmosPatchItemRequestOptions
     * @param id of entity to be patched
     * @param partitionKey of entity to be patched
     * @param patchOperations for entity to be patched
     * @param <S> domainType of entity
     */
    @Override
    public <S extends T> S save(ID id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(partitionKey, "partitionKey must not be null");
        // patch items
        return operation.patch(id, partitionKey, domainType, patchOperations);
    }

    /**
     * patch entity with CosmosPatchItemRequestOptions
     * @param id of entity to be patched
     * @param partitionKey of entity to be patched
     * @param patchOperations for entity to be patched
     * @param options options
     * @param <S> domainType of entity
     */
    @Override
    public <S extends T> S save(ID id, PartitionKey partitionKey, Class<S> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(partitionKey, "partitionKey must not be null");
        // patch items
        return operation.patch(id, partitionKey, domainType, patchOperations, options);
    }

    /**
     * Batch save entities. Uses bulk if possible.
     *
     * @param entities Batch entities
     * @param <S> type of entities
     * @return return the saved entities
     */
    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "Iterable entities should not be null");

        if (information.getPartitionKeyFieldName() != null) {
            return operation.insertAll(this.information, entities);
        } else {
            final List<S> savedEntities = new ArrayList<>();
            entities.forEach(entity -> {
                final S savedEntity = this.save(entity);
                savedEntities.add(savedEntity);
            });

            return savedEntities;
        }
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
    public Iterable<T> findAllById(Iterable<ID> ids) {
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

        return Optional.ofNullable(operation.findById(information.getContainerName(), id,
            information.getJavaType()));
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

        operation.deleteEntity(information.getContainerName(), entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        Assert.notNull(ids, "Iterable entities should not be null");
        StreamSupport.stream(ids.spliterator(), true).forEach(this::deleteById);
    }

    /**
     * Delete all the domains of a container. Uses bulk if possible.
     */
    @Override
    public void deleteAll() {
        operation.deleteAll(information.getContainerName(), information.getJavaType());
    }

    /**
     * Delete list of entities without partitions. Uses bulk if possible.
     *
     * @param entities list of entities to be deleted
     */
    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Iterable entities should not be null");

        if (information.getPartitionKeyFieldName() != null) {
            this.operation.deleteEntities(this.information, entities);
        } else {
            StreamSupport.stream(entities.spliterator(), true).forEach(this::delete);
        }
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
        final CosmosQuery query =
            new CosmosQuery(Criteria.getInstance(CriteriaType.ALL)).with(sort);

        return operation.find(query, information.getJavaType(), information.getContainerName());
    }

    /**
     * FindQuerySpecGenerator Returns a Page of entities meeting the paging restriction provided in the Pageable
     * object.
     *
     * @param pageable the Pageable object providing paging restriction
     * @return a page of entities
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        Assert.notNull(pageable, "pageable should not be null");

        return operation.findAll(pageable, information.getJavaType(),
            information.getContainerName());
    }

    @Override
    public Iterable<T> findAll(PartitionKey partitionKey) {
        return operation.findAll(partitionKey, information.getJavaType());
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return new FluentQueryByExample<>(example, example.getProbeType()).one();
    }

    @Override
    public <S extends T> Iterable<S> findAll(Example<S> example) {
        return new FluentQueryByExample<>(example, example.getProbeType()).all();
    }

    @Override
    public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(sort, "sort of findAll should not be null");
        return new FluentQueryByExample<>(example, example.getProbeType()).sortBy(sort).all();
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Assert.notNull(pageable, "pageable should not be null");
        return new FluentQueryByExample<>(example, example.getProbeType()).page(pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return new FluentQueryByExample<>(example, example.getProbeType()).count();
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return new FluentQueryByExample<>(example, example.getProbeType()).exists();
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {

        Assert.notNull(example, EXAMPLE_MUST_NOT_BE_NULL);
        Assert.notNull(queryFunction, QUERY_FUNCTION_MUST_NOT_BE_NULL);

        return queryFunction.apply(new FluentQueryByExample<>(example, example.getProbeType()));
    }

    /**
     * {@link org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery} using {@link Example}.
     */
    class FluentQueryByExample<S, T> extends FetchableFluentQuerySupport<Example<S>, T> {

        FluentQueryByExample(Example<S> example, Class<T> resultType) {
            this(example, Sort.unsorted(), 0, resultType, Collections.emptyList());
        }

        FluentQueryByExample(Example<S> example, Sort sort, int limit, Class<T> resultType, List<String> fieldsToInclude) {
            super(example, sort, limit, resultType, fieldsToInclude);
        }

        @Override
        protected <R> FluentQueryByExample<S, R> create(Example<S> predicate, Sort sort, int limit, Class<R> resultType,
                                                        List<String> fieldsToInclude) {
            return new FluentQueryByExample<>(predicate, sort, limit, resultType, fieldsToInclude);
        }

        @Override
        public T oneValue() {
            Iterable<T> resultsIterable = operation.find(createQuery(), getResultType(), information.getContainerName());
            final List<T> results = new ArrayList<>();
            resultsIterable.forEach(results::add);
            final T result;
            if (results.isEmpty()) {
                result = null;
            } else if (results.size() == 1) {
                result = results.get(0);
            } else {
                throw new CosmosAccessException("Too many results - return type "
                    + getResultType()
                    + " is not of type Iterable but find returned "
                    + results.size()
                    + " results");
            }

            return result;
        }

        @Override
        public T firstValue() {
            return this.stream().findFirst().orElse(null);
        }

        @Override
        public List<T> all() {
            return this.stream().toList();
        }

        @Override
        public Window<T> scroll(ScrollPosition scrollPosition) {
            Page<T> result;
            if (scrollPosition instanceof KeysetScrollPosition keysetScrollPosition) {
                String continuationToken = keysetScrollPosition.getKeys().get("continuationToken").toString();
                result = this.page(CosmosPageRequest.of(0, 0, continuationToken, getSort()));
                return Window.from(result.stream().toList(), value -> {
                    keysetScrollPosition.getKeys().put("continuationToken", ((CosmosPageRequest) result).getRequestContinuation());
                    return keysetScrollPosition;
                });
            } else if (scrollPosition instanceof OffsetScrollPosition offsetScrollPosition) {
                int offset = Math.toIntExact(offsetScrollPosition.getOffset() % 100);
                int pageNumber = Math.toIntExact(offsetScrollPosition.getOffset() / 100);
                result = this.page(CosmosPageRequest.of(offset, pageNumber, 100, null, getSort()));
                return Window.from(result.stream().toList(), offsetScrollPosition.positionFunction());
            } else {
                throw new IllegalArgumentException(String.format("ScrollPosition %s not supported", scrollPosition));
            }
        }

        @Override
        public Page<T> page(Pageable pageable) {

            Assert.notNull(pageable, "Pageable must not be null");

            CosmosQuery query = createQuery(q -> q.with(pageable));

            if (pageable.getPageNumber() != 0
                && !(pageable instanceof CosmosPageRequest)) {
                throw new IllegalStateException("Not the first page but Pageable is not a valid "
                    + "CosmosPageRequest, requestContinuation is required for non first page request");
            }

            query.with(pageable);

            return operation.paginationQuery(query, getResultType(), information.getContainerName());
        }

        @Override
        public Stream<T> stream() {
            Spliterator<T> spliterator = operation.find(createQuery(), getResultType(), information.getContainerName())
                .spliterator();
            return StreamSupport.stream(spliterator, false);
        }

        @Override
        public long count() {
            return operation.count(createQuery(), information.getContainerName());
        }

        @Override
        public boolean exists() {
            return operation.exists(createQuery(), information.getJavaType(), information.getContainerName());
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
