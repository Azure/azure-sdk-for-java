// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

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
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.PartitionKey;
import com.azure.spring.data.cosmos.common.CosmosdbUtils;
import com.azure.spring.data.cosmos.common.Memoizer;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.core.query.DocumentQuery;
import com.azure.spring.data.cosmos.exception.CosmosDBExceptionUtils;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.CosmosDbFactory;
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
    private static final String COUNT_VALUE_KEY = "_aggregate";

    private final MappingCosmosConverter mappingCosmosConverter;
    private final String databaseName;

    private final CosmosClient cosmosClient;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean isPopulateQueryMetrics;

    private final Function<Class<?>, CosmosEntityInformation<?, ?>> entityInfoCreator =
        Memoizer.memoize(this::getCosmosEntityInformation);

    private final List<String> containerNameCache;

    /**
     * Constructor
     *
     * @param cosmosDbFactory the cosmosdbfactory
     * @param mappingCosmosConverter the mappingCosmosConverter
     * @param dbName database name
     */
    public ReactiveCosmosTemplate(CosmosDbFactory cosmosDbFactory,
                                  MappingCosmosConverter mappingCosmosConverter,
                                  String dbName) {
        Assert.notNull(cosmosDbFactory, "CosmosDbFactory must not be null!");
        Assert.notNull(mappingCosmosConverter, "MappingCosmosConverter must not be null!");

        this.mappingCosmosConverter = mappingCosmosConverter;
        this.databaseName = dbName;
        this.containerNameCache = new ArrayList<>();

        this.cosmosClient = cosmosDbFactory.getCosmosClient();
        this.responseDiagnosticsProcessor = cosmosDbFactory.getConfig().getResponseDiagnosticsProcessor();
        this.isPopulateQueryMetrics = cosmosDbFactory.getConfig().isPopulateQueryMetrics();
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
    public Mono<CosmosContainerResponse> createCollectionIfNotExists(CosmosEntityInformation<?, ?> information) {
        return createContainerIfNotExists(information);
    }

    /**
     * Creates a container if it doesn't already exist
     *
     * @param information the CosmosEntityInformation
     * @return Mono containing CosmosContainerResponse
     */
    @Override
    public Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {

        return cosmosClient
            .createDatabaseIfNotExists(this.databaseName)
            .onErrorResume(throwable ->
                CosmosDBExceptionUtils.exceptionHandler("Failed to create database", throwable))
            .flatMap(cosmosDatabaseResponse -> {
                CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    cosmosDatabaseResponse, null);
                final CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(
                    information.getContainerName(),
                    "/" + information.getPartitionKeyFieldName());
                cosmosContainerProperties.defaultTimeToLive(information.getTimeToLive());
                cosmosContainerProperties.indexingPolicy(information.getIndexingPolicy());
                
                CosmosDatabase database = cosmosDatabaseResponse.database();
                Mono<CosmosContainerResponse> mono = null;
                
                if (information.getRequestUnit() == null) {
                    mono = database.createContainerIfNotExists(cosmosContainerProperties);
                } else {
                    mono = database.createContainerIfNotExists(cosmosContainerProperties, information.getRequestUnit());
                }
                
                return mono
                    .map(cosmosContainerResponse -> {
                        CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                            cosmosContainerResponse, null);
                        this.containerNameCache.add(information.getContainerName());
                        return cosmosContainerResponse;
                    })
                    .onErrorResume(throwable ->
                        CosmosDBExceptionUtils.exceptionHandler("Failed to create container", throwable));
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

        final FeedOptions feedOptions = new FeedOptions();
        feedOptions.partitionKey(partitionKey);
        feedOptions.populateQueryMetrics(isPopulateQueryMetrics);

        return cosmosClient
            .getDatabase(this.databaseName)
            .getContainer(containerName)
            .readAllItems(feedOptions)
            .flatMap(cosmosItemFeedResponse -> {
                CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.results());
            })
            .map(cosmosItemProperties -> toDomainObject(domainType, cosmosItemProperties))
            .onErrorResume(throwable ->
                CosmosDBExceptionUtils.exceptionHandler("Failed to find items", throwable));
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
     * @param containerName the containername
     * @param id the id
     * @param domainType the entity class
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> findById(String containerName, Object id, Class<T> domainType) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(domainType, "domainType should not be null");
        assertValidId(id);

        final String query = String.format("select * from root where root.id = '%s'", id.toString());
        final FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(isPopulateQueryMetrics);

        return cosmosClient.getDatabase(databaseName)
                           .getContainer(containerName)
                           .queryItems(query, options)
                           .flatMap(cosmosItemFeedResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   null, cosmosItemFeedResponse);
                               return Mono.justOrEmpty(cosmosItemFeedResponse
                                   .results()
                                   .stream()
                                   .map(cosmosItem -> toDomainObject(domainType, cosmosItem))
                                   .findFirst());
                           })
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable))
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
        return cosmosClient.getDatabase(databaseName)
                           .getContainer(containerName)
                           .getItem(id.toString(), partitionKey)
                           .read()
                           .flatMap(cosmosItemResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   cosmosItemResponse, null);
                               return Mono.justOrEmpty(toDomainObject(domainType,
                                   cosmosItemResponse.properties()));
                           })
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable));
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
        final CosmosItemProperties originalItem = mappingCosmosConverter.writeCosmosItemProperties(objectToSave);
        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(getContainerName(objectToSave.getClass()))
                           .createItem(originalItem, new CosmosItemRequestOptions())
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to insert item", throwable))
                           .flatMap(cosmosItemResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   cosmosItemResponse, null);
                               return Mono.just(toDomainObject(domainType, cosmosItemResponse.properties()));
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
    public <T> Mono<T> insert(String containerName, Object objectToSave, PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(objectToSave, "objectToSave should not be null");

        final Class<T> domainType = (Class<T>) objectToSave.getClass();
        final CosmosItemProperties originalItem = mappingCosmosConverter.writeCosmosItemProperties(objectToSave);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        if (partitionKey != null) {
            options.partitionKey(partitionKey);
        }
        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .createItem(originalItem, options)
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to insert item", throwable))
                           .flatMap(cosmosItemResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   cosmosItemResponse, null);
                               return Mono.just(toDomainObject(domainType, cosmosItemResponse.properties()));
                           });
    }

    /**
     * Upsert
     *
     * @param object the object to upsert
     * @param partitionKey the partition key
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> upsert(T object, PartitionKey partitionKey) {
        return upsert(getContainerName(object.getClass()), object, partitionKey);
    }

    /**
     * Upsert
     *
     * @param containerName the container name
     * @param object the object to save
     * @param partitionKey the partition key
     * @return Mono with the item or error
     */
    @Override
    public <T> Mono<T> upsert(String containerName, T object, PartitionKey partitionKey) {
        final Class<T> domainType = (Class<T>) object.getClass();
        final CosmosItemProperties originalItem = mappingCosmosConverter.writeCosmosItemProperties(object);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        if (partitionKey != null) {
            options.partitionKey(partitionKey);
        }

        applyVersioning(object.getClass(), originalItem, options);

        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .upsertItem(originalItem, options)
                           .flatMap(cosmosItemResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   cosmosItemResponse, null);
                               return Mono.just(toDomainObject(domainType, cosmosItemResponse.properties()));
                           })
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to upsert item", throwable));
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
            partitionKey = PartitionKey.None;
        }

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(partitionKey);
        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .getItem(id.toString(), partitionKey)
                           .delete(options)
                           .doOnNext(cosmosItemResponse ->
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                               cosmosItemResponse, null))
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to delete item", throwable))
                           .then();
    }

    /**
     * Delete all items in a container
     *
     * @param containerName the container name
     * @param partitionKeyName the partition key path
     * @return void Mono
     */
    @Override
    public Mono<Void> deleteAll(String containerName, String partitionKeyName) {
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");
        Assert.notNull(partitionKeyName, "partitionKeyName should not be null");

        final Criteria criteria = Criteria.getInstance(CriteriaType.ALL);
        final DocumentQuery query = new DocumentQuery(criteria);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final FeedOptions options = new FeedOptions();
        final boolean isCrossPartitionQuery = query.isCrossPartitionQuery(Collections.singletonList(partitionKeyName));
        options.enableCrossPartitionQuery(isCrossPartitionQuery);
        options.populateQueryMetrics(isPopulateQueryMetrics);
        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .queryItems(sqlQuerySpec, options)
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to query items", throwable))
                           .flatMap(cosmosItemFeedResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                    null, cosmosItemFeedResponse);
                               return Flux.fromIterable(cosmosItemFeedResponse.results());
                           })
                           .flatMap(cosmosItemProperties -> cosmosClient
                               .getDatabase(this.databaseName)
                               .getContainer(containerName)
                               .getItem(cosmosItemProperties.id(), cosmosItemProperties.get(partitionKeyName))
                               .delete()
                               .doOnNext(cosmosItemResponse ->
                                   CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                       cosmosItemResponse, null))
                               .onErrorResume(throwable ->
                                   CosmosDBExceptionUtils.exceptionHandler("Failed to delete items", throwable)))
                           .then();
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

        final Flux<CosmosItemProperties> results = findItems(query, domainType, containerName);
        final List<String> partitionKeyName = getPartitionKeyNames(domainType);

        return results.flatMap(d -> deleteItem(d, partitionKeyName, containerName, domainType))
                      .flatMap(cosmosItemProperties -> Mono.just(toDomainObject(domainType, cosmosItemProperties)));
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
        return findItems(query, domainType, containerName)
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
        return count(query, true, containerName).flatMap(count -> Mono.just(count > 0));
    }

    /**
     * Exists
     * @param id the id
     * @param domainType the entity class
     * @param containerName the containercontainer nam,e
     * @return Mono with a boolean or error
     */
    public Mono<Boolean> existsById(Object id, Class<?> domainType, String containerName) {
        return findById(containerName, id, domainType)
                .flatMap(o -> Mono.just(o !=  null));
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
        return count(query, true, containerName);
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
        return count(query, true, containerName);
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return mappingCosmosConverter;
    }

    /**
     *Count
     *
     * @param query the document query
     * @param isCrossPartitionQuery flag of cross partition
     * @param containerName the container name
     * @return Mono
     */
    public Mono<Long> count(DocumentQuery query, boolean isCrossPartitionQuery, String containerName) {
        return getCountValue(query, isCrossPartitionQuery, containerName);
    }

    private Mono<Long> getCountValue(DocumentQuery query, boolean isCrossPartitionQuery, String containerName) {
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        final FeedOptions options = new FeedOptions();

        options.enableCrossPartitionQuery(isCrossPartitionQuery);
        options.populateQueryMetrics(isPopulateQueryMetrics);

        return executeQuery(querySpec, containerName, options)
                .doOnNext(feedResponse -> CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                    null, feedResponse))
                .onErrorResume(throwable ->
                    CosmosDBExceptionUtils.exceptionHandler("Failed to get count value", throwable))
                .next()
                .map(r -> r.results().get(0).getLong(COUNT_VALUE_KEY));
    }

    private Flux<FeedResponse<CosmosItemProperties>> executeQuery(SqlQuerySpec sqlQuerySpec, String containerName,
                                                        FeedOptions options) {

        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .queryItems(sqlQuerySpec, options)
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to execute query", throwable));
    }

    /**
     * Delete container with container name
     *
     * @param containerName the container name
     */
    @Override
    public void deleteContainer(@NonNull String containerName) {
        Assert.hasText(containerName, "containerName should have text.");
        cosmosClient.getDatabase(this.databaseName)
                    .getContainer(containerName)
                    .delete()
                    .doOnNext(cosmosContainerResponse ->
                        CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                            cosmosContainerResponse, null))
                    .onErrorResume(throwable ->
                        CosmosDBExceptionUtils.exceptionHandler("Failed to delete container", throwable))
                    .block();
        this.containerNameCache.remove(containerName);
    }

    /**
     * @param domainType the domain class
     * @return the container name
     */
    public String getContainerName(Class<?> domainType) {
        Assert.notNull(domainType, "domainType should not be null");

        return entityInfoCreator.apply(domainType).getContainerName();
    }

    private Flux<CosmosItemProperties> findItems(@NonNull DocumentQuery query, @NonNull Class<?> domainType,
                                                 @NonNull String containerName) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final boolean isCrossPartitionQuery = query.isCrossPartitionQuery(getPartitionKeyNames(domainType));
        final FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(isCrossPartitionQuery);
        feedOptions.populateQueryMetrics(isPopulateQueryMetrics);

        return cosmosClient
                .getDatabase(this.databaseName)
                .getContainer(containerName)
                .queryItems(sqlQuerySpec, feedOptions)
                .flatMap(cosmosItemFeedResponse -> {
                    CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                        null, cosmosItemFeedResponse);
                    return Flux.fromIterable(cosmosItemFeedResponse.results());
                })
                .onErrorResume(throwable ->
                    CosmosDBExceptionUtils.exceptionHandler("Failed to query items", throwable));
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

    private Mono<CosmosItemProperties> deleteItem(@NonNull CosmosItemProperties cosmosItemProperties,
                                                  @NonNull List<String> partitionKeyNames,
                                                  String containerName,
                                                  @NonNull Class<?> domainType) {
        Assert.isTrue(partitionKeyNames.size() <= 1, "Only one Partition is supported.");

        PartitionKey partitionKey = null;

        if (!partitionKeyNames.isEmpty()
                && StringUtils.hasText(partitionKeyNames.get(0))) {
            partitionKey = new PartitionKey(cosmosItemProperties.get(partitionKeyNames.get(0)));
        }

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions(partitionKey);
        applyVersioning(domainType, cosmosItemProperties, options);

        return cosmosClient.getDatabase(this.databaseName)
                           .getContainer(containerName)
                           .getItem(cosmosItemProperties.id(), partitionKey)
                           .delete(options)
                           .map(cosmosItemResponse -> {
                               CosmosdbUtils.fillAndProcessResponseDiagnostics(responseDiagnosticsProcessor,
                                   cosmosItemResponse, null);
                               return cosmosItemProperties;
                           })
                           .onErrorResume(throwable ->
                               CosmosDBExceptionUtils.exceptionHandler("Failed to delete item", throwable));
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
