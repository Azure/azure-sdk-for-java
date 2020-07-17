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
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.common.Memoizer;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.core.query.DocumentQuery;
import com.azure.spring.data.cosmos.exception.CosmosExceptionUtils;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Template class of reactive cosmos
 */
@SuppressWarnings("unchecked")
public class ReactiveCosmosTemplate implements ReactiveCosmosOperations, ApplicationContextAware {

    private final MappingCosmosConverter mappingCosmosConverter;
    private final String databaseName;

    private final CosmosAsyncClient cosmosAsyncClient;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean isPopulateQueryMetrics;

    private final Function<Class<?>, CosmosEntityInformation<?, ?>> entityInfoCreator =
        Memoizer.memoize(this::getCosmosEntityInformation);

    /**
     * Constructor
     *
     * @param cosmosFactory the cosmos db factory
     * @param mappingCosmosConverter the mappingCosmosConverter
     * @param dbName database name
     */
    public ReactiveCosmosTemplate(CosmosFactory cosmosFactory,
                                  MappingCosmosConverter mappingCosmosConverter,
                                  String dbName) {
        Assert.notNull(cosmosFactory, "CosmosDbFactory must not be null!");
        Assert.notNull(mappingCosmosConverter, "MappingCosmosConverter must not be null!");

        this.mappingCosmosConverter = mappingCosmosConverter;
        this.databaseName = dbName;

        this.cosmosAsyncClient = cosmosFactory.getCosmosAsyncClient();
        this.responseDiagnosticsProcessor =
            cosmosFactory.getConfig().getResponseDiagnosticsProcessor();
        this.isPopulateQueryMetrics = cosmosFactory.getConfig().isQueryMetricsEnabled();
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
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to create database", throwable))
            .flatMap(cosmosDatabaseResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    cosmosDatabaseResponse.getDiagnostics(), null);
                final CosmosContainerProperties cosmosContainerProperties =
                    new CosmosContainerProperties(
                        information.getContainerName(),
                        "/" + information.getPartitionKeyFieldName());
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
                        CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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
        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));

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
        cosmosQueryRequestOptions.setQueryMetricsEnabled(isPopulateQueryMetrics);

        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems("SELECT * FROM r", cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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
        assertValidId(id);

        final String query = String.format("select * from root where root.id = '%s'",
            id.toString());
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(isPopulateQueryMetrics);

        return cosmosAsyncClient.getDatabase(databaseName)
                                .getContainer(containerName)
                                .queryItems(query, options, JsonNode.class)
                                .byPage()
                                .flatMap(cosmosItemFeedResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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
        assertValidId(id);

        final String containerName = getContainerName(domainType);
        return cosmosAsyncClient.getDatabase(databaseName)
                                .getContainer(containerName)
                                .readItem(id.toString(), partitionKey, JsonNode.class)
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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
        Assert.notNull(objectToSave, "domainType should not be null");

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
        Assert.notNull(objectToSave, "objectToSave should not be null");

        final Class<T> domainType = (Class<T>) objectToSave.getClass();
        final JsonNode originalItem =
            mappingCosmosConverter.writeJsonNode(objectToSave);
        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(getContainerName(objectToSave.getClass()))
                                .createItem(originalItem, new CosmosItemRequestOptions())
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable))
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.just(toDomainObject(domainType,
                                        cosmosItemResponse.getItem()));
                                });
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
        final JsonNode originalItem =
            mappingCosmosConverter.writeJsonNode(objectToSave);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }
        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .createItem(originalItem, partitionKey, options)
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable))
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.just(toDomainObject(domainType,
                                        cosmosItemResponse.getItem()));
                                });
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
        final JsonNode originalItem =
            mappingCosmosConverter.writeJsonNode(object);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        applyVersioning(object.getClass(), originalItem, options);

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .upsertItem(originalItem, options)
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.just(toDomainObject(domainType,
                                        cosmosItemResponse.getItem()));
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to upsert item", throwable));
    }

    /**
     * Delete an item by id
     *
     * @param containerName the container name
     * @param id the id
     * @param partitionKey the partition key
     * @return void Mono
     */
    @Override
    public Mono<Void> deleteById(String containerName, Object id, PartitionKey partitionKey) {
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");
        assertValidId(id);

        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .deleteItem(id.toString(), partitionKey)
                                .doOnNext(cosmosItemResponse ->
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null))
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable))
                                .then();
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

        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));

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
    public <T> Flux<T> delete(DocumentQuery query, Class<T> domainType, String containerName) {
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");

        final Flux<JsonNode> results = findItems(query, containerName);
        final List<String> partitionKeyName = getPartitionKeyNames(domainType);

        return results.flatMap(d -> deleteItem(d, partitionKeyName, containerName, domainType));
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
    public <T> Flux<T> find(DocumentQuery query, Class<T> domainType, String containerName) {
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
    public Mono<Boolean> exists(DocumentQuery query, Class<?> domainType, String containerName) {
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
        final DocumentQuery query = new DocumentQuery(Criteria.getInstance(CriteriaType.ALL));
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
    public Mono<Long> count(DocumentQuery query, String containerName) {
        return getCountValue(query, containerName);
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return mappingCosmosConverter;
    }

    private Mono<Long> getCountValue(DocumentQuery query, String containerName) {
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setQueryMetricsEnabled(isPopulateQueryMetrics);

        return executeQuery(querySpec, containerName, options)
            .doOnNext(feedResponse -> CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                null, feedResponse))
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
                             CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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

        return entityInfoCreator.apply(domainType).getContainerName();
    }

    private Flux<JsonNode> findItems(@NonNull DocumentQuery query,
                                     @NonNull String containerName) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(isPopulateQueryMetrics);

        return cosmosAsyncClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to query items", throwable));
    }

    private void assertValidId(Object id) {
        Assert.notNull(id, "id should not be null");
        if (id instanceof String) {
            Assert.hasText(id.toString(), "id should not be empty or only whitespaces.");
        }
    }

    private List<String> getPartitionKeyNames(Class<?> domainType) {
        final CosmosEntityInformation<?, ?> entityInfo = entityInfoCreator.apply(domainType);

        if (entityInfo.getPartitionKeyFieldName() == null) {
            return new ArrayList<>();
        }

        return Collections.singletonList(entityInfo.getPartitionKeyFieldName());
    }

    private <T> Mono<T> deleteItem(@NonNull JsonNode jsonNode,
                                   @NonNull List<String> partitionKeyNames,
                                   String containerName,
                                   @NonNull Class<T> domainType) {
        Assert.isTrue(partitionKeyNames.size() <= 1, "Only one Partition is supported.");

        PartitionKey partitionKey = null;

        if (!partitionKeyNames.isEmpty()
            && StringUtils.hasText(partitionKeyNames.get(0))) {
            partitionKey = new PartitionKey(jsonNode.get(partitionKeyNames.get(0)).asText());
        }

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, jsonNode, options);

        return cosmosAsyncClient.getDatabase(this.databaseName)
                                .getContainer(containerName)
                                .deleteItem(jsonNode.get("id").asText(), partitionKey)
                                .map(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
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

        if (entityInfoCreator.apply(domainType).isVersioned()) {
            options.setIfMatchETag(jsonNode.get("_etag").asText());
        }
    }

    private CosmosEntityInformation<?, ?> getCosmosEntityInformation(Class<?> domainType) {
        return new CosmosEntityInformation<>(domainType);
    }
}
