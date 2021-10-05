// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.generator.NativeQueryGenerator;
import com.azure.spring.data.cosmos.core.mapping.event.AfterLoadEvent;
import com.azure.spring.data.cosmos.core.mapping.event.CosmosMappingEvent;
import com.azure.spring.data.cosmos.core.query.CosmosPageImpl;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.CosmosSliceImpl;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.exception.CosmosExceptionUtils;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Template class for cosmos db
 */
public class CosmosTemplate implements CosmosOperations, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTemplate.class);

    private final MappingCosmosConverter mappingCosmosConverter;
    private final IsNewAwareAuditingHandler cosmosAuditingHandler;

    private final String databaseName;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean queryMetricsEnabled;
    private final CosmosAsyncClient cosmosAsyncClient;

    private ApplicationContext applicationContext;

    /**
     * Initialization
     *
     * @param client must not be {@literal null}
     * @param databaseName must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     * @param cosmosAuditingHandler can be {@literal null}
     */
    public CosmosTemplate(CosmosAsyncClient client, String databaseName,
                          CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter,
                          IsNewAwareAuditingHandler cosmosAuditingHandler) {
        this(new CosmosFactory(client, databaseName), cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
    }

    /**
     * Initialization
     *
     * @param client must not be {@literal null}
     * @param databaseName must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     */
    public CosmosTemplate(CosmosAsyncClient client, String databaseName,
                          CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
        this(new CosmosFactory(client, databaseName), cosmosConfig, mappingCosmosConverter, null);
    }

    /**
     * Initialization
     *
     * @param cosmosFactory must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     * @param cosmosAuditingHandler can be {@literal null}
     */
    public CosmosTemplate(CosmosFactory cosmosFactory,
                          CosmosConfig cosmosConfig,
                          MappingCosmosConverter mappingCosmosConverter,
                          IsNewAwareAuditingHandler cosmosAuditingHandler) {
        Assert.notNull(cosmosFactory, "CosmosFactory must not be null!");
        Assert.notNull(mappingCosmosConverter, "MappingCosmosConverter must not be null!");
        this.mappingCosmosConverter = mappingCosmosConverter;
        this.cosmosAuditingHandler = cosmosAuditingHandler;
        this.cosmosAsyncClient = cosmosFactory.getCosmosAsyncClient();
        this.databaseName = cosmosFactory.getDatabaseName();
        this.responseDiagnosticsProcessor = cosmosConfig.getResponseDiagnosticsProcessor();
        this.queryMetricsEnabled = cosmosConfig.isQueryMetricsEnabled();
    }

    /**
     * Initialization
     *
     * @param cosmosFactory must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     */
    public CosmosTemplate(CosmosFactory cosmosFactory,
                          CosmosConfig cosmosConfig,
                          MappingCosmosConverter mappingCosmosConverter) {
        this(cosmosFactory, cosmosConfig, mappingCosmosConverter, null);
    }

    /**
     * Sets the application context
     *
     * @param applicationContext must not be {@literal null}
     * @throws BeansException the bean exception
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Inserts item
     *
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    public <T> T insert(T objectToSave, PartitionKey partitionKey) {
        return insert(getContainerName(objectToSave.getClass()), objectToSave, partitionKey);
    }

    /**
     * Inserts item into the given container
     *
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    @Override
    public <T> T insert(String containerName, T objectToSave) {
        return insert(containerName, objectToSave, null);
    }

    /**
     * Inserts item into the given container
     *
     * @param containerName must not be {@literal null}
     * @param objectToSave must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param <T> type class of domain type
     * @return the inserted item
     */
    public <T> T insert(String containerName, T objectToSave, PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(objectToSave, "objectToSave should not be null");

        @SuppressWarnings("unchecked") final Class<T> domainType = (Class<T>) objectToSave.getClass();

        markAuditedIfConfigured(objectToSave);
        generateIdIfNullAndAutoGenerationEnabled(objectToSave, domainType);

        final JsonNode originalItem = mappingCosmosConverter.writeJsonNode(objectToSave);

        LOGGER.debug("execute createItem in database {} container {}", this.databaseName,
            containerName);

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        //  if the partition key is null, SDK will get the partitionKey from the object
        final CosmosItemResponse<JsonNode> response = cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .createItem(originalItem, partitionKey, options)
            .publishOn(Schedulers.parallel())
            .doOnNext(cosmosItemResponse ->
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable))
            .block();

        assert response != null;
        return toDomainObject(domainType, response.getItem());
    }

    @SuppressWarnings("unchecked")
    private <T> void generateIdIfNullAndAutoGenerationEnabled(T originalItem, Class<?> type) {
        CosmosEntityInformation<?, ?> entityInfo = CosmosEntityInformation.getInstance(type);
        if (entityInfo.shouldGenerateId() && ReflectionUtils.getField(entityInfo.getIdField(), originalItem) == null) {
            ReflectionUtils.setField(entityInfo.getIdField(), originalItem, UUID.randomUUID().toString());
        }
    }

    /**
     * Finds item by id
     *
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
        String idToQuery = CosmosUtils.getStringIDValue(id);
        final String containerName = getContainerName(domainType);
        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .readItem(idToQuery, partitionKey, JsonNode.class)
            .publishOn(Schedulers.parallel())
            .flatMap(cosmosItemResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null);
                return Mono.justOrEmpty(emitOnLoadEventAndConvertToDomainObject(domainType, containerName, cosmosItemResponse.getItem()));
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable))
            .block();
    }

    /**
     * Finds item by id
     *
     * @param containerName must not be {@literal null}
     * @param id must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param <T> type class of domain type
     * @return found item
     */
    public <T> T findById(String containerName, Object id, Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");

        final String query = "select * from root where root.id = @ROOT_ID";
        final SqlParameter param = new SqlParameter("@ROOT_ID", CosmosUtils.getStringIDValue(id));
        final SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, param);
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);
        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, options, JsonNode.class)
            .byPage()
            .publishOn(Schedulers.parallel())
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Mono.justOrEmpty(cosmosItemFeedResponse
                    .getResults()
                    .stream()
                    .map(cosmosItem -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, cosmosItem))
                    .findFirst());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable))
            .blockFirst();
    }

    /**
     * Upserts an item with partition key
     *
     * @param object upsert object
     * @param <T> type of upsert object
     */
    public <T> void upsert(T object) {
        Assert.notNull(object, "Upsert object should not be null");

        upsert(getContainerName(object.getClass()), object);
    }

    /**
     * Upserts an item into container with partition key
     *
     * @param containerName the container name
     * @param object upsert object
     * @param <T> type of upsert object
     */
    public <T> void upsert(String containerName, T object) {
        upsertAndReturnEntity(containerName, object);
    }

    /**
     * Upserts an item and return item properties
     *
     * @param containerName the container name
     * @param object upsert object
     * @param <T> type of upsert object
     * @return upsert object entity
     */
    public <T> T upsertAndReturnEntity(String containerName, T object) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(object, "Upsert object should not be null");

        markAuditedIfConfigured(object);

        final JsonNode originalItem = mappingCosmosConverter.writeJsonNode(object);

        LOGGER.debug("execute upsert item in database {} container {}", this.databaseName,
            containerName);

        @SuppressWarnings("unchecked") final Class<T> domainType = (Class<T>) object.getClass();

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, originalItem, options);

        final CosmosItemResponse<JsonNode> cosmosItemResponse = cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .upsertItem(originalItem, options)
            .publishOn(Schedulers.parallel())
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to upsert item", throwable))
            .block();

        assert cosmosItemResponse != null;
        return toDomainObject(domainType, cosmosItemResponse.getItem());
    }

    /**
     * Find the DocumentQuery, find all the items specified by domain type.
     *
     * @param domainType the domain type
     * @param <T> class type of domain
     * @return found results in a List
     */
    public <T> Iterable<T> findAll(Class<T> domainType) {
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
    public <T> Iterable<T> findAll(String containerName, final Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");

        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));

        return findItems(query, containerName, domainType);
    }

    @Override
    public <T> Iterable<T> findAll(PartitionKey partitionKey, final Class<T> domainType) {
        Assert.notNull(partitionKey, "partitionKey should not be null");
        Assert.notNull(domainType, "domainType should not be null");

        final String containerName = getContainerName(domainType);

        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setPartitionKey(partitionKey);
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);

        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems("SELECT * FROM r", cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .publishOn(Schedulers.parallel())
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .map(jsonNode -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, jsonNode))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable))
            .toIterable();
    }

    /**
     * Delete the DocumentQuery, delete all the items in the given container.
     *
     * @param containerName Container name of database
     * @param domainType the domain type
     */
    public void deleteAll(@NonNull String containerName, @NonNull Class<?> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");

        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));

        this.delete(query, domainType, containerName);
    }

    @Override
    public void deleteContainer(@NonNull String containerName) {
        Assert.hasText(containerName, "containerName should have text.");
        cosmosAsyncClient.getDatabase(this.databaseName)
                         .getContainer(containerName)
                         .delete()
                         .publishOn(Schedulers.parallel())
                         .doOnNext(response -> {
                             CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                 response.getDiagnostics(), null);
                         })
                         .onErrorResume(throwable ->
                             CosmosExceptionUtils.exceptionHandler("Failed to delete container", throwable))
                         .block();
    }

    @Override
    public String getContainerName(Class<?> domainType) {
        Assert.notNull(domainType, "domainType should not be null");
        return CosmosEntityInformation.getInstance(domainType).getContainerName();
    }

    @Override
    public CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {

        final CosmosContainerResponse response = cosmosAsyncClient
            .createDatabaseIfNotExists(this.databaseName)
            .publishOn(Schedulers.parallel())
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to create database", throwable))
            .flatMap(cosmosDatabaseResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosDatabaseResponse.getDiagnostics(), null);

                final CosmosContainerProperties cosmosContainerProperties =
                    new CosmosContainerProperties(information.getContainerName(), information.getPartitionKeyPath());
                cosmosContainerProperties.setDefaultTimeToLiveInSeconds(information.getTimeToLive());
                cosmosContainerProperties.setIndexingPolicy(information.getIndexingPolicy());

                CosmosAsyncDatabase cosmosAsyncDatabase = cosmosAsyncClient
                    .getDatabase(cosmosDatabaseResponse.getProperties().getId());
                Mono<CosmosContainerResponse> cosmosContainerResponseMono;

                if (information.getRequestUnit() == null) {
                    cosmosContainerResponseMono =
                        cosmosAsyncDatabase.createContainerIfNotExists(cosmosContainerProperties);
                } else {
                    ThroughputProperties throughputProperties = information.isAutoScale()
                        ? ThroughputProperties.createAutoscaledThroughput(information.getRequestUnit())
                        : ThroughputProperties.createManualThroughput(information.getRequestUnit());
                    cosmosContainerResponseMono =
                        cosmosAsyncDatabase.createContainerIfNotExists(cosmosContainerProperties,
                            throughputProperties);
                }

                return cosmosContainerResponseMono
                    .onErrorResume(throwable ->
                        CosmosExceptionUtils.exceptionHandler("Failed to create container",
                            throwable))
                    .doOnNext(cosmosContainerResponse ->
                        CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                            cosmosContainerResponse.getDiagnostics(), null));
            })
            .block();
        assert response != null;
        return response.getProperties();
    }

    @Override
    public CosmosContainerProperties getContainerProperties(String containerName) {
        final CosmosContainerResponse response = cosmosAsyncClient.getDatabase(this.databaseName)
            .getContainer(containerName)
            .read()
            .block();
        assert response != null;
        return response.getProperties();
    }

    @Override
    public CosmosContainerProperties replaceContainerProperties(String containerName,
                                                                CosmosContainerProperties properties) {
        CosmosContainerResponse response = this.cosmosAsyncClient.getDatabase(this.databaseName)
            .getContainer(containerName)
            .replace(properties)
            .block();
        assert response != null;
        return response.getProperties();
    }

    /**
     * Deletes the item by id and partition key.
     *
     * @param containerName Container name of database
     * @param id item id
     * @param partitionKey the partition key
     */
    public void deleteById(String containerName, Object id, PartitionKey partitionKey) {
        deleteById(containerName, id, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Deletes the entity
     *
     * @param <T> type class of domain type
     * @param containerName the container name
     * @param entity the entity object
     */
    @Override
    public <T> void deleteEntity(String containerName, T entity) {
        Assert.notNull(entity, "entity to be deleted should not be null");
        @SuppressWarnings("unchecked")
        final Class<T> domainType = (Class<T>) entity.getClass();
        final JsonNode originalItem = mappingCosmosConverter.writeJsonNode(entity);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(entity.getClass(), originalItem, options);
        deleteItem(originalItem, containerName, domainType);
    }

    private void deleteById(String containerName, Object id, PartitionKey partitionKey,
                            CosmosItemRequestOptions options) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        String idToDelete = CosmosUtils.getStringIDValue(id);
        LOGGER.debug("execute deleteById in database {} container {}", this.databaseName,
            containerName);

        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }

        cosmosAsyncClient.getDatabase(this.databaseName)
                         .getContainer(containerName)
                         .deleteItem(idToDelete, partitionKey, options)
                         .publishOn(Schedulers.parallel())
                         .doOnNext(response ->
                             CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                 response.getDiagnostics(), null))
                         .onErrorResume(throwable ->
                             CosmosExceptionUtils.exceptionHandler("Failed to delete item",
                                 throwable))
                         .block();
    }

    @Override
    public <T, ID> Iterable<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName) {
        Assert.notNull(ids, "Id list should not be null");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");
        final List<Object> idList = new ArrayList<>();
        for (ID id : ids) {
            idList.add(CosmosUtils.getStringIDValue(id));
        }
        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.IN, "id",
            Collections.singletonList(idList), Part.IgnoreCaseType.NEVER));
        return find(query, domainType, containerName);
    }

    /**
     * Finds the document query items
     *
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return All the found items as List.
     */
    public <T> Iterable<T> find(@NonNull CosmosQuery query, @NonNull Class<T> domainType,
                                String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        return findItems(query, containerName, domainType);
    }

    /**
     * Checks if document query items exist
     *
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return if items exist
     */
    public <T> Boolean exists(@NonNull CosmosQuery query, @NonNull Class<T> domainType,
                              String containerName) {
        return this.count(query, containerName) > 0;
    }

    /**
     * Delete the DocumentQuery, need to query the domains at first, then delete the item from the result. The cosmos db
     * Sql API do _NOT_ support DELETE query, we cannot add one DeleteQueryGenerator.
     *
     * @param query The representation for query method.
     * @param domainType Class of domain
     * @param containerName Container name of database
     * @param <T> class of domainType
     * @return All the deleted items as List.
     */
    @Override
    public <T> Iterable<T> delete(@NonNull CosmosQuery query, @NonNull Class<T> domainType,
                                  @NonNull String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        final List<JsonNode> results = findItemsAsFlux(query, containerName, domainType).collectList().block();
        assert results != null;
        return results.stream()
                      .map(item -> deleteItem(item, containerName, domainType))
                      .collect(Collectors.toList());
    }

    @Override
    public <T> Page<T> findAll(Pageable pageable, Class<T> domainType, String containerName) {
        final CosmosQuery query =
            new CosmosQuery(Criteria.getInstance(CriteriaType.ALL)).with(pageable);
        if (pageable.getSort().isSorted()) {
            query.with(pageable.getSort());
        }

        return paginationQuery(query, domainType, containerName);
    }

    @Override
    public <T> Page<T> runPaginationQuery(SqlQuerySpec querySpec, Pageable pageable, Class<?> domainType, Class<T> returnType) {
        final String containerName = getContainerName(domainType);
        final SqlQuerySpec sortedQuerySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, pageable.getSort());
        final SqlQuerySpec countQuerySpec = NativeQueryGenerator.getInstance().generateCountQuery(querySpec);
        return paginationQuery(sortedQuerySpec, countQuerySpec, pageable,
            pageable.getSort(), returnType, containerName, Optional.empty());
    }

    @Override
    public <T> Page<T> paginationQuery(CosmosQuery query, Class<T> domainType, String containerName) {
        final SqlQuerySpec querySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final SqlQuerySpec countQuerySpec = new CountQueryGenerator().generateCosmos(query);
        Optional<Object> partitionKeyValue = query.getPartitionKeyValue(domainType);
        return paginationQuery(querySpec, countQuerySpec, query.getPageable(),
            query.getSort(), domainType, containerName, partitionKeyValue);
    }

    @Override
    public <T> Slice<T> sliceQuery(CosmosQuery query, Class<T> domainType, String containerName) {
        final SqlQuerySpec querySpec = new FindQuerySpecGenerator().generateCosmos(query);
        Optional<Object> partitionKeyValue = query.getPartitionKeyValue(domainType);
        return sliceQuery(querySpec, query.getPageable(), query.getSort(), domainType, containerName, partitionKeyValue);
    }

    @Override
    public <T> Slice<T> runSliceQuery(SqlQuerySpec querySpec, Pageable pageable, Class<?> domainType, Class<T> returnType) {
        final String containerName = getContainerName(domainType);
        final SqlQuerySpec sortedQuerySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, pageable.getSort());
        return sliceQuery(sortedQuerySpec, pageable, pageable.getSort(), returnType, containerName, Optional.empty());
    }

    private <T> Page<T> paginationQuery(SqlQuerySpec querySpec, SqlQuerySpec countQuerySpec,
                                       Pageable pageable, Sort sort,
                                       Class<T> returnType, String containerName,
                                        Optional<Object> partitionKeyValue) {
        Slice<T> response = sliceQuery(querySpec, pageable, sort, returnType, containerName, partitionKeyValue);
        final long total = getCountValue(countQuerySpec, containerName);
        return new CosmosPageImpl<>(response.getContent(), response.getPageable(), total);
    }

    private <T> Slice<T> sliceQuery(SqlQuerySpec querySpec,
                                   Pageable pageable, Sort sort,
                                   Class<T> returnType, String containerName,
                                    Optional<Object> partitionKeyValue) {
        Assert.isTrue(pageable.getPageSize() > 0,
            "pageable should have page size larger than 0");
        Assert.hasText(containerName, "container should not be null, empty or only whitespaces");

        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);
        partitionKeyValue.ifPresent(o -> {
            LOGGER.debug("Setting partition key {}", o);
            cosmosQueryRequestOptions.setPartitionKey(new PartitionKey(o));
        });

        CosmosAsyncContainer container =
            cosmosAsyncClient.getDatabase(this.databaseName).getContainer(containerName);

        Flux<FeedResponse<JsonNode>> feedResponseFlux;
        if (pageable instanceof CosmosPageRequest) {
            feedResponseFlux = container
                .queryItems(querySpec, cosmosQueryRequestOptions, JsonNode.class)
                .byPage(((CosmosPageRequest) pageable).getRequestContinuation(),
                    pageable.getPageSize());
        } else {
            feedResponseFlux = container
                .queryItems(querySpec, cosmosQueryRequestOptions, JsonNode.class)
                .byPage(pageable.getPageSize());
        }

        final FeedResponse<JsonNode> feedResponse = feedResponseFlux
            .publishOn(Schedulers.parallel())
            .doOnNext(propertiesFeedResponse ->
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    propertiesFeedResponse.getCosmosDiagnostics(), propertiesFeedResponse))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to query items", throwable))
            .next()
            .block();

        assert feedResponse != null;
        final Iterator<JsonNode> it = feedResponse.getResults().iterator();

        final List<T> result = new ArrayList<>();
        for (int index = 0; it.hasNext()
            && index < pageable.getPageSize(); index++) {

            final JsonNode jsonNode = it.next();
            if (jsonNode == null) {
                continue;
            }

            final T entity = mappingCosmosConverter.read(returnType, jsonNode);
            result.add(entity);
        }

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
            feedResponse.getContinuationToken(),
            sort);

        return new CosmosSliceImpl<>(result, pageRequest, feedResponse.getContinuationToken() != null);
    }

    @Override
    public long count(String containerName) {
        Assert.hasText(containerName, "container name should not be empty");

        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));
        final Long count = getCountValue(query, containerName);
        assert count != null;
        return count;
    }

    @Override
    public <T> long count(CosmosQuery query, String containerName) {
        Assert.hasText(containerName, "container name should not be empty");

        final Long count = getCountValue(query, containerName);
        assert count != null;
        return count;
    }

    @Override
    public <T> long count(SqlQuerySpec querySpec, String containerName) {
        Assert.hasText(containerName, "container name should not be empty");

        final Long count = getCountValue(querySpec, containerName);
        assert count != null;
        return count;
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return this.mappingCosmosConverter;
    }

    @Override
    public <T> Iterable<T> runQuery(SqlQuerySpec querySpec, Class<?> domainType, Class<T> returnType) {
        return runQuery(querySpec, Sort.unsorted(), domainType, returnType);
    }

    @Override
    public <T> Iterable<T> runQuery(SqlQuerySpec querySpec, Sort sort, Class<?> domainType, Class<T> returnType) {
        querySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, sort);
        return getJsonNodeFluxFromQuerySpec(getContainerName(domainType), querySpec)
                   .map(jsonNode -> emitOnLoadEventAndConvertToDomainObject(returnType, getContainerName(domainType), jsonNode))
                   .collectList()
                   .block();
    }

    private void markAuditedIfConfigured(Object object) {
        if (cosmosAuditingHandler != null) {
            cosmosAuditingHandler.markAudited(object);
        }
    }

    private Long getCountValue(CosmosQuery query, String containerName) {
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        return getCountValue(querySpec, containerName);
    }

    private Long getCountValue(SqlQuerySpec querySpec, String containerName) {
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);

        return executeQuery(querySpec, containerName, options)
            .publishOn(Schedulers.parallel())
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to get count value", throwable))
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getCosmosDiagnostics(), response))
            .next()
            .map(r -> r.getResults().get(0).asLong())
            .block();
    }

    private Flux<FeedResponse<JsonNode>> executeQuery(SqlQuerySpec sqlQuerySpec,
                                                      String containerName,
                                                      CosmosQueryRequestOptions options) {
        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .queryItems(sqlQuerySpec, options, JsonNode.class)
                                .byPage();
    }

    private <T> Flux<JsonNode> findItemsAsFlux(@NonNull CosmosQuery query,
                                               @NonNull String containerName,
                                               @NonNull Class<T> domainType) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);
        Optional<Object> partitionKeyValue = query.getPartitionKeyValue(domainType);
        partitionKeyValue.ifPresent(o -> {
            LOGGER.debug("Setting partition key {}", o);
            cosmosQueryRequestOptions.setPartitionKey(new PartitionKey(o));
        });

        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .publishOn(Schedulers.parallel())
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                                              cosmosItemFeedResponse.getCosmosDiagnostics(),
                                                              cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable));
    }

    private Flux<JsonNode> getJsonNodeFluxFromQuerySpec(
        @NonNull String containerName, SqlQuerySpec sqlQuerySpec) {
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);

        return cosmosAsyncClient
                   .getDatabase(this.databaseName)
                   .getContainer(containerName)
                   .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, JsonNode.class)
                   .byPage()
                   .publishOn(Schedulers.parallel())
                   .flatMap(cosmosItemFeedResponse -> {
                       CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                                                     cosmosItemFeedResponse.getCosmosDiagnostics(),
                                                                     cosmosItemFeedResponse);
                       return Flux.fromIterable(cosmosItemFeedResponse.getResults());
                   })
                   .onErrorResume(throwable ->
                                      CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable));
    }

    private <T> Iterable<T> findItems(@NonNull CosmosQuery query,
                                      @NonNull String containerName,
                                      @NonNull Class<T> domainType) {
        return findItemsAsFlux(query, containerName, domainType)
            .map(jsonNode -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, jsonNode))
            .toIterable();
    }

    private <T> T deleteItem(@NonNull JsonNode jsonNode,
                             String containerName,
                             @NonNull Class<T> domainType) {

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, jsonNode, options);

        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .deleteItem(jsonNode, options)
            .publishOn(Schedulers.parallel())
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable))
            .flatMap(objectCosmosItemResponse -> Mono.just(toDomainObject(domainType, jsonNode)))
            .block();
    }

    private <T> T emitOnLoadEventAndConvertToDomainObject(@NonNull Class<T> domainType, String containerName, JsonNode responseJsonNode) {
        maybeEmitEvent(new AfterLoadEvent<>(responseJsonNode, domainType, containerName));
        return toDomainObject(domainType, responseJsonNode);
    }

    private <T> T toDomainObject(@NonNull Class<T> domainType, JsonNode responseJsonNode) {
        return mappingCosmosConverter.read(domainType, responseJsonNode);
    }

    private void applyVersioning(Class<?> domainType,
                                 JsonNode jsonNode,
                                 CosmosItemRequestOptions options) {
        CosmosEntityInformation<?, ?> entityInformation = CosmosEntityInformation.getInstance(domainType);
        if (entityInformation.isVersioned()) {
            options.setIfMatchETag(jsonNode.get(Constants.ETAG_PROPERTY_DEFAULT_NAME).asText());
        }
    }

    private void maybeEmitEvent(CosmosMappingEvent<?> event) {
        try {
            if (canPublishEvent()) {
                this.applicationContext.publishEvent(event);
            }
        } catch (Exception ex) {
            LOGGER.warn("Encountered an exception while trying to emit spring application event", ex);
        }
    }

    private boolean canPublishEvent() {
        return this.applicationContext != null;
    }

}
