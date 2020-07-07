// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.CosmosDbFactory;
import com.microsoft.azure.spring.data.cosmosdb.common.Memoizer;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.core.generator.CountQueryGenerator;
import com.microsoft.azure.spring.data.cosmosdb.core.generator.FindQuerySpecGenerator;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CosmosPageImpl;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CosmosPageRequest;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.azure.spring.data.cosmosdb.common.CosmosdbUtils.fillAndProcessResponseDiagnostics;
import static com.microsoft.azure.spring.data.cosmosdb.exception.CosmosDBExceptionUtils.exceptionHandler;
import static com.microsoft.azure.spring.data.cosmosdb.exception.CosmosDBExceptionUtils.findAPIExceptionHandler;

/**
 * Template class for cosmos db
 */
public class CosmosTemplate implements CosmosOperations, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTemplate.class);

    private static final String COUNT_VALUE_KEY = "_aggregate";

    private final MappingCosmosConverter mappingCosmosConverter;
    private final String databaseName;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean isPopulateQueryMetrics;

    private final CosmosClient cosmosClient;
    private final Function<Class<?>, CosmosEntityInformation<?, ?>> entityInfoCreator =
            Memoizer.memoize(this::getCosmosEntityInformation);

    /**
     * Initialization
     * @param cosmosDbFactory must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     * @param dbName must not be {@literal null}
     */
    public CosmosTemplate(CosmosDbFactory cosmosDbFactory,
                          MappingCosmosConverter mappingCosmosConverter,
                          String dbName) {
        Assert.notNull(cosmosDbFactory, "CosmosDbFactory must not be null!");
        Assert.notNull(mappingCosmosConverter, "MappingCosmosConverter must not be null!");

        this.mappingCosmosConverter = mappingCosmosConverter;

        this.databaseName = dbName;
        this.cosmosClient = cosmosDbFactory.getCosmosClient();
        this.responseDiagnosticsProcessor = cosmosDbFactory.getConfig().getResponseDiagnosticsProcessor();
        this.isPopulateQueryMetrics = cosmosDbFactory.getConfig().isPopulateQueryMetrics();
    }

    /**
     * Sets the application context
     * @param applicationContext must not be {@literal null}
     * @throws BeansException the bean exception
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    }

    /**
     *
     * Inserts item
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    public <T> T insert(T objectToSave, PartitionKey partitionKey) {
        Assert.notNull(objectToSave, "domainType should not be null");

        return insert(getContainerName(objectToSave.getClass()), objectToSave, partitionKey);
    }

    /**
     * Inserts item into the given container
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    public <T> T insert(String containerName, T objectToSave, PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(objectToSave, "objectToSave should not be null");

        final CosmosItemProperties originalItem = mappingCosmosConverter.writeCosmosItemProperties(objectToSave);

        LOGGER.debug("execute createItem in database {} container {}", this.databaseName, containerName);

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(partitionKey);

        @SuppressWarnings("unchecked")
        final Class<T> domainType = (Class<T>) objectToSave.getClass();

        final CosmosItemResponse response = cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .createItem(originalItem, options)
            .doOnNext(cosmosItemResponse -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                cosmosItemResponse, null))
            .onErrorResume(throwable ->
                exceptionHandler("Failed to insert item", throwable))
            .block();

        assert response != null;
        return mappingCosmosConverter.read(domainType, response.properties());
    }

    /**
     * Finds item by id
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    public <T> T findById(Object id, Class<T> domainType) {
        Assert.notNull(domainType, "domainType should not be null");

        return findById(getContainerName(domainType), id, domainType);
    }

    @Override
    public <T> T findById(Object id, Class<T> domainType, PartitionKey partitionKey) {
        Assert.notNull(domainType, "domainType should not be null");
        Assert.notNull(partitionKey, "partitionKey should not be null");
        assertValidId(id);

        final String containerName = getContainerName(domainType);
        return cosmosClient
            .getDatabase(databaseName)
            .getContainer(containerName)
            .getItem(id.toString(), partitionKey)
            .read()
            .flatMap(cosmosItemResponse -> {
                fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    cosmosItemResponse, null);
                return Mono.justOrEmpty(toDomainObject(domainType,
                    cosmosItemResponse.properties()));
            })
            .onErrorResume(throwable ->
                findAPIExceptionHandler("Failed to find item", throwable))
            .block();
    }

    /**
     * Finds item by id
     * @param containerName must not be {@literal null}
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    public <T> T findById(String containerName, Object id, Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");
        assertValidId(id);

        final String query = String.format("select * from root where root.id = '%s'", id.toString());
        final FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(isPopulateQueryMetrics);
        return cosmosClient
            .getDatabase(databaseName)
            .getContainer(containerName)
            .queryItems(query, options)
            .flatMap(cosmosItemFeedResponse -> {
                fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, cosmosItemFeedResponse);
                return Mono.justOrEmpty(cosmosItemFeedResponse
                    .results()
                    .stream()
                    .map(cosmosItem -> mappingCosmosConverter.read(domainType, cosmosItem))
                    .findFirst());
            })
            .onErrorResume(throwable ->
                findAPIExceptionHandler("Failed to find item", throwable))
            .blockFirst();
    }

    /**
     * Upserts an item with partition key
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     */
    public <T> void upsert(T object, PartitionKey partitionKey) {
        Assert.notNull(object, "Upsert object should not be null");

        upsert(getContainerName(object.getClass()), object, partitionKey);
    }

    /**
     * Upserts an item into container with partition key
     * @param containerName the container name
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     */
    public <T> void upsert(String containerName, T object, PartitionKey partitionKey) {
        upsertAndReturnEntity(containerName, object,  partitionKey);
    }

    /**
     * Upserts an item and return item properties
     * @param containerName the container name
     * @param object upsert object
     * @param partitionKey the partition key
     * @param <T> type of upsert object
     * @return upsert object entity
     */
    public <T> T upsertAndReturnEntity(String containerName, T object, PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(object, "Upsert object should not be null");

        final CosmosItemProperties originalItem = mappingCosmosConverter.writeCosmosItemProperties(object);

        LOGGER.debug("execute upsert item in database {} container {}", this.databaseName, containerName);

        @SuppressWarnings("unchecked")
        final Class<T> domainType = (Class<T>) object.getClass();

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(partitionKey);
        applyVersioning(domainType, originalItem, options);

        final CosmosItemResponse cosmosItemResponse = cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .upsertItem(originalItem, options)
            .doOnNext(response -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                response, null))
            .onErrorResume(throwable -> exceptionHandler("Failed to upsert item", throwable))
            .block();

        assert cosmosItemResponse != null;
        return mappingCosmosConverter.read(domainType, cosmosItemResponse.properties());
    }

    /**
     * Find the DocumentQuery, find all the items specified by domain type.
     *
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    public <T> List<T> findAll(Class<T> domainType) {
        Assert.notNull(domainType, "domainType should not be null");

        return findAll(getContainerName(domainType), domainType);
    }

    /**
     * Find the DocumentQuery, find all the items specified by domain type in the given container.
     *
     * @param containerName the container name
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    public <T> List<T> findAll(String containerName, final Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");

        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));

        final List<CosmosItemProperties> items = findItems(query, domainType, containerName);
        return items.stream()
                .map(d -> getConverter().read(domainType, d))
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> findAll(PartitionKey partitionKey, final Class<T> domainType) {
        Assert.notNull(partitionKey, "partitionKey should not be null");
        Assert.notNull(domainType, "domainType should not be null");

        final String containerName = getContainerName(domainType);

        final FeedOptions feedOptions = new FeedOptions();
        feedOptions.partitionKey(partitionKey);
        feedOptions.populateQueryMetrics(isPopulateQueryMetrics);

        return cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .readAllItems(feedOptions)
            .flatMap(cosmosItemFeedResponse -> {
                fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.results());
            })
            .map(cosmosItemProperties -> toDomainObject(domainType, cosmosItemProperties))
            .onErrorResume(throwable ->
                exceptionHandler("Failed to find items", throwable))
            .collectList()
            .block();
    }

    /**
     * Delete the DocumentQuery, delete all the items in the given container.
     *
     * @param containerName Container name of database
     * @param domainType the domain type
     */
    public void deleteAll(@NonNull String containerName, @NonNull Class<?> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");

        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));

        this.delete(query, domainType, containerName);
    }

    @Override
    public void deleteCollection(@NonNull String containerName) {
        deleteContainer(containerName);
    }

    @Override
    public void deleteContainer(@NonNull String containerName) {
        Assert.hasText(containerName, "containerName should have text.");
        cosmosClient.getDatabase(this.databaseName)
                    .getContainer(containerName)
                    .delete()
                    .doOnNext(response -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                        response, null))
                    .onErrorResume(throwable ->
                        exceptionHandler("Failed to delete container",  throwable))
                    .block();
    }

    /**
     * To get collection name by domaintype
     * @param domainType class type
     * @return String
     */
    public String getCollectionName(Class<?> domainType) {
        return getContainerName(domainType);
    }

    @Override
    public String getContainerName(Class<?> domainType) {
        Assert.notNull(domainType, "domainType should not be null");

        return entityInfoCreator.apply(domainType).getContainerName();
    }

    @Override
    public CosmosContainerProperties createCollectionIfNotExists(@NonNull CosmosEntityInformation<?, ?> information) {
        return createContainerIfNotExists(information);
    }

    @Override
    public CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {
        final CosmosContainerResponse response = cosmosClient
            .createDatabaseIfNotExists(this.databaseName)
            .onErrorResume(throwable ->
                exceptionHandler("Failed to create database", throwable))
            .flatMap(cosmosDatabaseResponse -> {
                fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    cosmosDatabaseResponse, null);
                final CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(
                    information.getContainerName(), "/"
                        + information.getPartitionKeyFieldName());
                cosmosContainerProperties.defaultTimeToLive(information.getTimeToLive());
                cosmosContainerProperties.indexingPolicy(information.getIndexingPolicy());
                return cosmosDatabaseResponse
                    .database()
                    .createContainerIfNotExists(cosmosContainerProperties, information.getRequestUnit())
                    .onErrorResume(throwable ->
                        exceptionHandler("Failed to create container", throwable))
                    .doOnNext(cosmosContainerResponse ->
                        fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                            cosmosContainerResponse, null));
            })
            .block();
        assert response != null;
        return response.properties();
    }

    /**
     * Delete the DocumentQuery, need to query by id at first, then delete the item
     * from the result.
     *
     * @param containerName Container name of database
     * @param id item id
     * @param partitionKey the paritition key
     */
    public void deleteById(String containerName, Object id, PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        assertValidId(id);

        LOGGER.debug("execute deleteById in database {} container {}", this.databaseName, containerName);

        if (partitionKey == null) {
            partitionKey = PartitionKey.None;
        }
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(partitionKey);
        cosmosClient.getDatabase(this.databaseName)
                    .getContainer(containerName)
                    .getItem(id.toString(), partitionKey)
                    .delete(options)
                    .doOnNext(response -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                        response, null))
                    .onErrorResume(throwable ->
                        exceptionHandler("Failed to delete item", throwable))
                    .block();
    }

    @Override
    public <T, ID> List<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName) {
        Assert.notNull(ids, "Id list should not be null");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.IN, "id",
                Collections.singletonList(ids)));
        return find(query, domainType, containerName);
    }

    /**
     * Finds the document query items
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return All the found items as List.
     */
    public <T> List<T> find(@NonNull DocumentQuery query, @NonNull Class<T> domainType, String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        return findItems(query, domainType, containerName)
            .stream()
            .map(cosmosItemProperties -> toDomainObject(domainType, cosmosItemProperties))
            .collect(Collectors.toList());
    }

    /**
     * Checks if document query items exist
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return if items exist
     */
    public <T> Boolean exists(@NonNull DocumentQuery query, @NonNull Class<T> domainType, String containerName) {
        return this.find(query, domainType, containerName).size() > 0;
    }

    /**
     * Delete the DocumentQuery, need to query the domains at first, then delete the item
     * from the result.
     * The cosmosdb Sql API do _NOT_ support DELETE query, we cannot add one DeleteQueryGenerator.
     *
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return All the deleted items as List.
     */
    @Override
    public <T> List<T> delete(@NonNull DocumentQuery query, @NonNull Class<T> domainType,
            @NonNull String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        final List<CosmosItemProperties> results = findItems(query, domainType, containerName);
        final List<String> partitionKeyName = getPartitionKeyNames(domainType);

        return results.stream().map(cosmosItemProperties -> {
            final CosmosItemResponse cosmosItemResponse = deleteItem(cosmosItemProperties,
                partitionKeyName, containerName, domainType);
            return getConverter().read(domainType, cosmosItemResponse.properties());
        }).collect(Collectors.toList());
    }

    @Override
    public <T> Page<T> findAll(Pageable pageable, Class<T> domainType, String containerName) {
        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL)).with(pageable);
        if (pageable.getSort().isSorted()) {
            query.with(pageable.getSort());
        }

        return paginationQuery(query, domainType, containerName);
    }

    @Override
    public <T> Page<T> paginationQuery(DocumentQuery query, Class<T> domainType, String containerName) {
        Assert.isTrue(query.getPageable().getPageSize() > 0, "pageable should have page size larger than 0");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        final Pageable pageable = query.getPageable();
        final FeedOptions feedOptions = new FeedOptions();
        if (pageable instanceof CosmosPageRequest) {
            feedOptions.requestContinuation(((CosmosPageRequest) pageable).getRequestContinuation());
        }

        feedOptions.maxItemCount(pageable.getPageSize());
        feedOptions.enableCrossPartitionQuery(query.isCrossPartitionQuery(getPartitionKeyNames(domainType)));
        feedOptions.populateQueryMetrics(isPopulateQueryMetrics);

        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final FeedResponse<CosmosItemProperties> feedResponse = cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, feedOptions)
            .doOnNext(propertiesFeedResponse -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                null, propertiesFeedResponse))
            .onErrorResume(throwable ->
                exceptionHandler("Failed to query items", throwable))
            .next()
            .block();

        assert feedResponse != null;
        final Iterator<CosmosItemProperties> it = feedResponse.results().iterator();

        final List<T> result = new ArrayList<>();
        for (int index = 0; it.hasNext()
                && index < pageable.getPageSize(); index++) {

            final CosmosItemProperties cosmosItemProperties = it.next();
            if (cosmosItemProperties == null) {
                continue;
            }

            final T entity = mappingCosmosConverter.read(domainType, cosmosItemProperties);
            result.add(entity);
        }

        final long total = count(query, domainType, containerName);
        final int contentSize = result.size();

        int pageSize;

        if (contentSize < pageable.getPageSize()
                && contentSize > 0) {
            //  If the content size is less than page size,
            //  this means, cosmosDB is returning less items than page size,
            //  because of either RU limit, or payload limit

            //  Set the page size to content size.
            pageSize = contentSize;
        } else {
            pageSize = pageable.getPageSize();
        }

        final CosmosPageRequest pageRequest = CosmosPageRequest.of(pageable.getOffset(),
            pageable.getPageNumber(),
            pageSize,
            feedResponse.continuationToken(),
            query.getSort());

        return new CosmosPageImpl<>(result, pageRequest, total);
    }

    @Override
    public long count(String containerName) {
        Assert.hasText(containerName, "container name should not be empty");

        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));
        final Long count = getCountValue(query, true, containerName);
        assert count != null;
        return count;
    }

    @Override
    public <T> long count(DocumentQuery query, Class<T> domainType, String containerName) {
        Assert.notNull(domainType, "domainType should not be null");
        Assert.hasText(containerName, "container name should not be empty");

        final boolean isCrossPartitionQuery =
                query.isCrossPartitionQuery(getPartitionKeyNames(domainType));
        final Long count = getCountValue(query, isCrossPartitionQuery, containerName);
        assert count != null;
        return count;
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return this.mappingCosmosConverter;
    }

    private Long getCountValue(DocumentQuery query, boolean isCrossPartitionQuery, String containerName) {
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        final FeedOptions options = new FeedOptions();

        options.enableCrossPartitionQuery(isCrossPartitionQuery);
        options.populateQueryMetrics(isPopulateQueryMetrics);

        return executeQuery(querySpec, containerName, options)
                .onErrorResume(throwable ->
                    exceptionHandler("Failed to get count value", throwable))
                .doOnNext(response -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, response))
                .next()
                .map(r -> r.results().get(0).getLong(COUNT_VALUE_KEY))
                .block();
    }

    private Flux<FeedResponse<CosmosItemProperties>> executeQuery(SqlQuerySpec sqlQuerySpec, String containerName,
            FeedOptions options) {
        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .queryItems(sqlQuerySpec, options)
                           .onErrorResume(throwable ->
                               exceptionHandler("Failed to execute query", throwable));
    }

    private List<String> getPartitionKeyNames(Class<?> domainType) {
        final CosmosEntityInformation<?, ?> entityInfo = entityInfoCreator.apply(domainType);

        if (entityInfo.getPartitionKeyFieldName() == null) {
            return new ArrayList<>();
        }

        return Collections.singletonList(entityInfo.getPartitionKeyFieldName());
    }

    private void assertValidId(Object id) {
        Assert.notNull(id, "id should not be null");
        if (id instanceof String) {
            Assert.hasText(id.toString(), "id should not be empty or only whitespaces.");
        }
    }

    private List<CosmosItemProperties> findItems(@NonNull DocumentQuery query,
                                                 @NonNull Class<?> domainType,
                                                 @NonNull String containerName) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final boolean isCrossPartitionQuery =
                query.isCrossPartitionQuery(getPartitionKeyNames(domainType));
        final FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(isCrossPartitionQuery);
        feedOptions.populateQueryMetrics(isPopulateQueryMetrics);

        return cosmosClient
                .getDatabase(this.databaseName)
                .getContainer(containerName)
                .queryItems(sqlQuerySpec, feedOptions)
                .flatMap(cosmosItemFeedResponse -> {
                    fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                        null, cosmosItemFeedResponse);
                    return Flux.fromIterable(cosmosItemFeedResponse.results());
                })
                .onErrorResume(throwable ->
                    exceptionHandler("Failed to find items", throwable))
                .collectList()
                .block();
    }

    private CosmosItemResponse deleteItem(@NonNull CosmosItemProperties cosmosItemProperties,
                                          @NonNull List<String> partitionKeyNames,
                                          String containerName,
                                          @NonNull Class<?> domainType) {
        Assert.isTrue(partitionKeyNames.size() <= 1, "Only one Partition is supported.");

        PartitionKey partitionKey = null;

        if (!partitionKeyNames.isEmpty()
                && StringUtils.hasText(partitionKeyNames.get(0))) {
            partitionKey = new PartitionKey(cosmosItemProperties.get(partitionKeyNames.get(0)));
        }

        if (partitionKey == null) {
            partitionKey = PartitionKey.None;
        }

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions(partitionKey);
        applyVersioning(domainType, cosmosItemProperties, options);

        return cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .getItem(cosmosItemProperties.id(), partitionKey)
            .delete(options)
            .doOnNext(response -> fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                response, null))
            .onErrorResume(throwable ->
                exceptionHandler("Failed to delete item", throwable))
            .block();
    }

    private <T> T toDomainObject(@NonNull Class<T> domainType, CosmosItemProperties cosmosItemProperties) {
        return mappingCosmosConverter.read(domainType, cosmosItemProperties);
    }

    private void applyVersioning(Class<?> domainType,
            CosmosItemProperties cosmosItemProperties,
            CosmosItemRequestOptions options) {

        if (entityInfoCreator.apply(domainType).isVersioned()) {
            final AccessCondition accessCondition = new AccessCondition();
            accessCondition.type(AccessConditionType.IF_MATCH);
            accessCondition.condition(cosmosItemProperties.etag());
            options.accessCondition(accessCondition);
        }
    }

    private CosmosEntityInformation<?, ?> getCosmosEntityInformation(Class<?> domainType) {
        return new CosmosEntityInformation<>(domainType);
    }

}
