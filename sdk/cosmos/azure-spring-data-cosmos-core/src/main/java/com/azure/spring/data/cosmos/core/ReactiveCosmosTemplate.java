// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.exception.CosmosExceptionUtils;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Template class of reactive cosmos
 */
@SuppressWarnings("unchecked")
public class ReactiveCosmosTemplate implements ReactiveCosmosOperations, ApplicationContextAware {

    private final MappingCosmosConverter mappingCosmosConverter;
    private final String databaseName;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean queryMetricsEnabled;
    private final CosmosAsyncClient cosmosAsyncClient;
    private final IsNewAwareAuditingHandler cosmosAuditingHandler;

    /**
     * Initialization
     *
     * @param client must not be {@literal null}
     * @param databaseName must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     * @param cosmosAuditingHandler can be {@literal null}
     */
    public ReactiveCosmosTemplate(CosmosAsyncClient client, String databaseName,
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
    public ReactiveCosmosTemplate(CosmosAsyncClient client, String databaseName,
                                  CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
        this(new CosmosFactory(client, databaseName), cosmosConfig, mappingCosmosConverter, null);
    }

    /**
     * Constructor
     *
     * @param cosmosFactory the cosmos db factory
     * @param cosmosConfig the cosmos config
     * @param mappingCosmosConverter the mappingCosmosConverter
     * @param cosmosAuditingHandler the auditing handler
     */
    public ReactiveCosmosTemplate(CosmosFactory cosmosFactory,
                                  CosmosConfig cosmosConfig,
                                  MappingCosmosConverter mappingCosmosConverter,
                                  IsNewAwareAuditingHandler cosmosAuditingHandler) {
        Assert.notNull(cosmosFactory, "CosmosFactory must not be null!");
        Assert.notNull(cosmosConfig, "CosmosConfig must not be null!");
        Assert.notNull(mappingCosmosConverter, "MappingCosmosConverter must not be null!");

        this.mappingCosmosConverter = mappingCosmosConverter;
        this.cosmosAsyncClient = cosmosFactory.getCosmosAsyncClient();
        this.databaseName = cosmosFactory.getDatabaseName();
        this.responseDiagnosticsProcessor = cosmosConfig.getResponseDiagnosticsProcessor();
        this.queryMetricsEnabled = cosmosConfig.isQueryMetricsEnabled();
        this.cosmosAuditingHandler = cosmosAuditingHandler;
    }

    /**
     * Initialization
     *
     * @param cosmosFactory must not be {@literal null}
     * @param cosmosConfig must not be {@literal null}
     * @param mappingCosmosConverter must not be {@literal null}
     */
    public ReactiveCosmosTemplate(CosmosFactory cosmosFactory,
                                  CosmosConfig cosmosConfig,
                                  MappingCosmosConverter mappingCosmosConverter) {
        this(cosmosFactory, cosmosConfig, mappingCosmosConverter, null);
    }

    /**
     * @param applicationContext the application context
     * @throws BeansException the bean exception
     */
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        //  NOTE: When application context instance variable gets introduced, assign it here.
    }

    /**
     * Creates a container if it doesn't already exist
     *
     * @param information the CosmosEntityInformation
     * @return Mono containing CosmosContainerResponse
     */
    @Override
    public Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {

        return cosmosAsyncClient
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

                CosmosAsyncDatabase database =
                    cosmosAsyncClient.getDatabase(cosmosDatabaseResponse.getProperties().getId());
                Mono<CosmosContainerResponse> cosmosContainerResponseMono;

                if (information.getRequestUnit() == null) {
                    cosmosContainerResponseMono =
                        database.createContainerIfNotExists(cosmosContainerProperties);
                } else {
                    ThroughputProperties throughputProperties =
                        ThroughputProperties.createManualThroughput(information.getRequestUnit());
                    cosmosContainerResponseMono =
                        database.createContainerIfNotExists(cosmosContainerProperties,
                            throughputProperties);
                }

                return cosmosContainerResponseMono
                    .map(cosmosContainerResponse -> {
                        CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                            cosmosContainerResponse.getDiagnostics(), null);
                        return cosmosContainerResponse;
                    })
                    .onErrorResume(throwable ->
                        CosmosExceptionUtils.exceptionHandler("Failed to create container",
                            throwable));
            });

    }

    /**
     * Find all items in a given container
     *
     * @param containerName the containerName
     * @param domainType the domainType
     * @return Flux with all the found items or error
     */
    @Override
    public <T> Flux<T> findAll(String containerName, Class<T> domainType) {
        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));

        return find(query, domainType, containerName);
    }

    /**
     * Find all items in a given container
     *
     * @param domainType the domainType
     * @return Flux with all the found items or error
     */
    @Override
    public <T> Flux<T> findAll(Class<T> domainType) {
        return findAll(domainType.getSimpleName(), domainType);
    }

    @Override
    public <T> Flux<T> findAll(PartitionKey partitionKey, Class<T> domainType) {
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
            .map(cosmosItemProperties -> toDomainObject(domainType, cosmosItemProperties))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable));
    }

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType the domainType
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> findById(Object id, Class<T> domainType) {
        Assert.notNull(domainType, "domainType should not be null");
        return findById(getContainerName(domainType), id, domainType);
    }

    /**
     * Find by id
     *
     * @param containerName the container name
     * @param id the id
     * @param domainType the entity class
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> findById(String containerName, Object id, Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");

        final String query = String.format("select * from root where root.id = '%s'",
            CosmosUtils.getStringIDValue(id));
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .queryItems(query, options, JsonNode.class)
                                .byPage()
                                .publishOn(Schedulers.parallel())
                                .flatMap(cosmosItemFeedResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemFeedResponse.getCosmosDiagnostics(),
                                        cosmosItemFeedResponse);
                                    return Mono.justOrEmpty(cosmosItemFeedResponse
                                        .getResults()
                                        .stream()
                                        .map(cosmosItem -> toDomainObject(domainType, cosmosItem))
                                        .findFirst());
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable))
                                .next();
    }

    /**
     * Find by id
     *
     * @param id the id
     * @param domainType the entity class
     * @param partitionKey partition Key
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> findById(Object id, Class<T> domainType, PartitionKey partitionKey) {
        Assert.notNull(domainType, "domainType should not be null");
        String idToFind = CosmosUtils.getStringIDValue(id);

        final String containerName = getContainerName(domainType);
        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .readItem(idToFind, partitionKey, JsonNode.class)
                                .publishOn(Schedulers.parallel())
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.justOrEmpty(toDomainObject(domainType,
                                        cosmosItemResponse.getItem()));
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable));
    }

    /**
     * Insert
     *
     * @param <T> type of inserted objectToSave
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @return Mono with the item or error
     */
    public <T> Mono<T> insert(T objectToSave, PartitionKey partitionKey) {
        return insert(getContainerName(objectToSave.getClass()), objectToSave, partitionKey);
    }

    /**
     * Insert
     *
     * @param objectToSave the object to save
     * @param <T> type of inserted objectToSave
     * @return Mono with the item or error
     */
    public <T> Mono<T> insert(T objectToSave) {
        return insert(getContainerName(objectToSave.getClass()), objectToSave, null);
    }

    /**
     * Insert
     *
     * @param <T> type of inserted objectToSave
     * @param containerName the container name
     * @param objectToSave the object to save
     * @param partitionKey the partition key
     * @return Mono with the item or error
     */
    public <T> Mono<T> insert(String containerName, Object objectToSave,
                              PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(objectToSave, "objectToSave should not be null");

        final Class<T> domainType = (Class<T>) objectToSave.getClass();
        generateIdIfNullAndAutoGenerationEnabled(objectToSave, domainType);
        final JsonNode originalItem = prepareToPersistAndConvertToItemProperties(objectToSave);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        //  if the partition key is null, SDK will get the partitionKey from the object
        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .createItem(originalItem, partitionKey, options)
            .publishOn(Schedulers.parallel())
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable))
            .flatMap(cosmosItemResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null);
                return Mono.just(toDomainObject(domainType, cosmosItemResponse.getItem()));
            });
    }

    /**
     * Insert
     *
     * @param <T> type of inserted objectToSave
     * @param containerName the container name
     * @param objectToSave the object to save
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> insert(String containerName, T objectToSave) {
        return insert(containerName, objectToSave, null);
    }

    @SuppressWarnings("unchecked")
    private <T> void generateIdIfNullAndAutoGenerationEnabled(T originalItem, Class<?> type) {
        CosmosEntityInformation<?, ?> entityInfo = CosmosEntityInformation.getInstance(type);
        if (entityInfo.shouldGenerateId() && ReflectionUtils.getField(entityInfo.getIdField(), originalItem) == null) {
            ReflectionUtils.setField(entityInfo.getIdField(), originalItem, UUID.randomUUID().toString());
        }
    }

    /**
     * Upsert
     *
     * @param object the object to upsert
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> upsert(T object) {
        return upsert(getContainerName(object.getClass()), object);
    }

    /**
     * Upsert
     *
     * @param containerName the container name
     * @param object the object to save
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> upsert(String containerName, T object) {
        final Class<T> domainType = (Class<T>) object.getClass();
        final JsonNode originalItem = prepareToPersistAndConvertToItemProperties(object);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        applyVersioning(object.getClass(), originalItem, options);

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .upsertItem(originalItem, options)
                                .publishOn(Schedulers.parallel())
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.just(toDomainObject(domainType,
                                        cosmosItemResponse.getItem()));
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to upsert item", throwable));
    }

    /**
     * Deletes the item with id and partition key.
     *
     * @param containerName Container name of database
     * @param id item id
     * @param partitionKey the partition key
     */
    @Override
    public Mono<Void> deleteById(String containerName, Object id, PartitionKey partitionKey) {
        return deleteById(containerName, id, partitionKey, new CosmosItemRequestOptions());
    }

    private Mono<Void> deleteById(String containerName, Object id, PartitionKey partitionKey,
                                  CosmosItemRequestOptions cosmosItemRequestOptions) {
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");
        String idToDelete = CosmosUtils.getStringIDValue(id);

        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .deleteItem(idToDelete, partitionKey, cosmosItemRequestOptions)
                                .publishOn(Schedulers.parallel())
                                .doOnNext(cosmosItemResponse ->
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null))
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable))
                                .then();
    }

    /**
     * Deletes the entity
     *
     * @param <T> type class of domain type
     * @param containerName Container name of database
     * @param entity the entity to delete
     * @return void Mono
     */
    public <T> Mono<Void> deleteEntity(String containerName, T entity) {
        Assert.notNull(entity, "entity to be deleted should not be null");
        @SuppressWarnings("unchecked")
        final Class<T> domainType = (Class<T>) entity.getClass();
        final JsonNode originalItem = mappingCosmosConverter.writeJsonNode(entity);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(entity.getClass(), originalItem, options);
        return deleteItem(originalItem, containerName, domainType).then();
    }

    /**
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param domainType the domainType
     * @return void Mono
     */
    @Override
    public Mono<Void> deleteAll(@NonNull String containerName, @NonNull Class<?> domainType) {
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");

        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));

        return this.delete(query, domainType, containerName).then();
    }

    /**
     * Delete items matching query
     *
     * @param query the document query
     * @param domainType the entity class
     * @param containerName the container name
     * @return Mono
     */
    @Override
    public <T> Flux<T> delete(CosmosQuery query, Class<T> domainType, String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");

        final Flux<JsonNode> results = findItems(query, containerName);

        return results.flatMap(d -> deleteItem(d, containerName, domainType));
    }

    /**
     * Find items
     *
     * @param query the document query
     * @param domainType the entity class
     * @param containerName the container name
     * @return Flux with found items or error
     */
    @Override
    public <T> Flux<T> find(CosmosQuery query, Class<T> domainType, String containerName) {
        return findItems(query, containerName)
            .map(cosmosItemProperties -> toDomainObject(domainType, cosmosItemProperties));
    }

    /**
     * Exists
     *
     * @param query the document query
     * @param domainType the entity class
     * @param containerName the container name
     * @return Mono with a boolean or error
     */
    @Override
    public Mono<Boolean> exists(CosmosQuery query, Class<?> domainType, String containerName) {
        return count(query, containerName).flatMap(count -> Mono.just(count > 0));
    }

    /**
     * Exists
     *
     * @param id the id
     * @param domainType the entity class
     * @param containerName the container name
     * @return Mono with a boolean or error
     */
    public Mono<Boolean> existsById(Object id, Class<?> domainType, String containerName) {
        return findById(containerName, id, domainType)
            .flatMap(o -> Mono.just(o != null));
    }

    /**
     * Count
     *
     * @param containerName the container name
     * @return Mono with the count or error
     */
    @Override
    public Mono<Long> count(String containerName) {
        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));
        return count(query, containerName);
    }

    /**
     * Count
     *
     * @param query the document query
     * @param containerName the container name
     * @return Mono with count or error
     */
    @Override
    public Mono<Long> count(CosmosQuery query, String containerName) {
        return getCountValue(query, containerName);
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return mappingCosmosConverter;
    }

    private Mono<Long> getCountValue(CosmosQuery query, String containerName) {
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setQueryMetricsEnabled(this.queryMetricsEnabled);

        return executeQuery(querySpec, containerName, options)
            .doOnNext(feedResponse -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                feedResponse.getCosmosDiagnostics(), feedResponse))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to get count value", throwable))
            .next()
            .map(r -> r.getResults().get(0).asLong());
    }

    private Flux<FeedResponse<JsonNode>> executeQuery(SqlQuerySpec sqlQuerySpec,
                                                      String containerName,
                                                      CosmosQueryRequestOptions options) {

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .queryItems(sqlQuerySpec, options, JsonNode.class)
                                .byPage()
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to execute query", throwable));
    }

    /**
     * Delete container with container name
     *
     * @param containerName the container name
     */
    @Override
    public void deleteContainer(@NonNull String containerName) {
        Assert.hasText(containerName, "containerName should have text.");
        cosmosAsyncClient.getDatabase(this.databaseName)
                         .getContainer(containerName)
                         .delete()
                         .doOnNext(cosmosContainerResponse ->
                             CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                 cosmosContainerResponse.getDiagnostics(), null))
                         .onErrorResume(throwable ->
                             CosmosExceptionUtils.exceptionHandler("Failed to delete container",
                                 throwable))
                         .block();
    }

    /**
     * @param domainType the domain class
     * @return the container name
     */
    public String getContainerName(Class<?> domainType) {
        Assert.notNull(domainType, "domainType should not be null");

        return CosmosEntityInformation.getInstance(domainType).getContainerName();
    }

    private JsonNode prepareToPersistAndConvertToItemProperties(Object object) {
        if (cosmosAuditingHandler != null) {
            cosmosAuditingHandler.markAudited(object);
        }
        return mappingCosmosConverter.writeJsonNode(object);
    }


    private Flux<JsonNode> findItems(@NonNull CosmosQuery query,
                                     @NonNull String containerName) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
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
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to query items", throwable));
    }

    private <T> Mono<T> deleteItem(@NonNull JsonNode jsonNode,
                                   String containerName,
                                   @NonNull Class<T> domainType) {
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, jsonNode, options);

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .deleteItem(jsonNode, options)
                                .publishOn(Schedulers.parallel())
                                .map(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return cosmosItemResponse;
                                })
                                .flatMap(objectCosmosItemResponse -> Mono.just(toDomainObject(domainType, jsonNode)))
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable));
    }

    private <T> T toDomainObject(@NonNull Class<T> domainType, JsonNode jsonNode) {
        return mappingCosmosConverter.read(domainType, jsonNode);
    }

    private void applyVersioning(Class<?> domainType,
                                 JsonNode jsonNode,
                                 CosmosItemRequestOptions options) {
        CosmosEntityInformation<?, ?> entityInformation = CosmosEntityInformation.getInstance(domainType);
        if (entityInformation.isVersioned()) {
            options.setIfMatchETag(jsonNode.get(Constants.ETAG_PROPERTY_DEFAULT_NAME).asText());
        }
    }
}
