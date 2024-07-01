// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.config.DatabaseThroughputConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.generator.NativeQueryGenerator;
import com.azure.spring.data.cosmos.core.mapping.event.AfterLoadEvent;
import com.azure.spring.data.cosmos.core.mapping.event.CosmosMappingEvent;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
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
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Template class of reactive cosmos
 */
@SuppressWarnings("unchecked")
public class ReactiveCosmosTemplate implements ReactiveCosmosOperations, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCosmosTemplate.class);

    private final CosmosFactory cosmosFactory;
    private final MappingCosmosConverter mappingCosmosConverter;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean queryMetricsEnabled;
    private final boolean indexMetricsEnabled;
    private final int maxDegreeOfParallelism;
    private final int maxBufferedItemCount;
    private final int responseContinuationTokenLimitInKb;
    private final IsNewAwareAuditingHandler cosmosAuditingHandler;
    private final DatabaseThroughputConfig databaseThroughputConfig;
    private boolean pointReadWarningLogged = false;
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
        this.cosmosFactory = cosmosFactory;
        this.responseDiagnosticsProcessor = cosmosConfig.getResponseDiagnosticsProcessor();
        this.queryMetricsEnabled = cosmosConfig.isQueryMetricsEnabled();
        this.indexMetricsEnabled = cosmosConfig.isIndexMetricsEnabled();
        this.maxDegreeOfParallelism = cosmosConfig.getMaxDegreeOfParallelism();
        this.maxBufferedItemCount = cosmosConfig.getMaxBufferedItemCount();
        this.responseContinuationTokenLimitInKb = cosmosConfig.getResponseContinuationTokenLimitInKb();
        this.cosmosAuditingHandler = cosmosAuditingHandler;
        this.databaseThroughputConfig = cosmosConfig.getDatabaseThroughputConfig();
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

    private String getDatabaseName() {
        return this.cosmosFactory.getDatabaseName();
    }

    private CosmosAsyncClient getCosmosAsyncClient() {
        return this.cosmosFactory.getCosmosAsyncClient();
    }

    /**
     * @param applicationContext the application context
     * @throws BeansException the bean exception
     */
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates a container if it doesn't already exist
     *
     * @param information the CosmosEntityInformation
     * @return Mono containing CosmosContainerResponse
     */
    @Override
    public Mono<CosmosContainerResponse> createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {

        return createDatabaseIfNotExists()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to create database", throwable,
                    this.responseDiagnosticsProcessor))
            .flatMap(cosmosDatabaseResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosDatabaseResponse.getDiagnostics(), null);
                final CosmosContainerProperties cosmosContainerProperties = getCosmosContainerPropertiesWithPartitionKeyPath(information);
                cosmosContainerProperties.setDefaultTimeToLiveInSeconds(information.getTimeToLive());
                cosmosContainerProperties.setIndexingPolicy(information.getIndexingPolicy());
                final UniqueKeyPolicy uniqueKeyPolicy = information.getUniqueKeyPolicy();
                if (uniqueKeyPolicy != null) {
                    cosmosContainerProperties.setUniqueKeyPolicy(uniqueKeyPolicy);
                }

                CosmosAsyncDatabase database =
                    this.getCosmosAsyncClient().getDatabase(cosmosDatabaseResponse.getProperties().getId());
                Mono<CosmosContainerResponse> cosmosContainerResponseMono;

                if (information.getRequestUnit() == null) {
                    cosmosContainerResponseMono =
                        database.createContainerIfNotExists(cosmosContainerProperties);
                } else {
                    ThroughputProperties throughputProperties = information.isAutoScale()
                        ? ThroughputProperties.createAutoscaledThroughput(information.getRequestUnit())
                        : ThroughputProperties.createManualThroughput(information.getRequestUnit());
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
                            throwable, this.responseDiagnosticsProcessor));
            });

    }

    private Mono<CosmosDatabaseResponse> createDatabaseIfNotExists() {
        if (databaseThroughputConfig == null) {
            return this.getCosmosAsyncClient()
                .createDatabaseIfNotExists(this.getDatabaseName());
        } else {
            ThroughputProperties throughputProperties = databaseThroughputConfig.isAutoScale()
                ? ThroughputProperties.createAutoscaledThroughput(databaseThroughputConfig.getRequestUnits())
                : ThroughputProperties.createManualThroughput(databaseThroughputConfig.getRequestUnits());
            return this.getCosmosAsyncClient()
                .createDatabaseIfNotExists(this.getDatabaseName(), throughputProperties);
        }
    }

    @Override
    public Mono<CosmosContainerProperties> getContainerProperties(String containerName) {
        containerName = getContainerNameOverride(containerName);
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .read()
            .map(CosmosContainerResponse::getProperties);
    }

    @Override
    public Mono<CosmosContainerProperties> replaceContainerProperties(String containerName,
                                                                CosmosContainerProperties properties) {
        containerName = getContainerNameOverride(containerName);
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .replace(properties)
            .map(CosmosContainerResponse::getProperties);
    }

    /**
     *
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
        cosmosQueryRequestOptions.setIndexMetricsEnabled(this.indexMetricsEnabled);
        cosmosQueryRequestOptions.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        cosmosQueryRequestOptions.setMaxBufferedItemCount(this.maxBufferedItemCount);
        cosmosQueryRequestOptions.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);

        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .queryItems("SELECT * FROM r", cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .map(cosmosItemProperties -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, cosmosItemProperties))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable,
                    this.responseDiagnosticsProcessor));
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
        CosmosEntityInformation<?, ?> cosmosEntityInformation = CosmosEntityInformation.getInstance(domainType);
        String containerPartitionKey = cosmosEntityInformation.getPartitionKeyFieldName();
        if ("id".equals(containerPartitionKey) && id != null) {
            return findById(id, domainType, new PartitionKey(id));
        }
        if (!this.pointReadWarningLogged) {
            LOGGER.warn("The partitionKey is not id!! Consider using findById(ID id, PartitionKey partitionKey) instead to avoid the need for using a cross partition query which results in higher latency and cost than necessary. See https://aka.ms/PointReadsInSpring for more info.");
            this.pointReadWarningLogged = true;
        }
        final String finalContainerName = getContainerNameOverride(containerName);
        final String query = "select * from root where root.id = @ROOT_ID";
        final SqlParameter param = new SqlParameter("@ROOT_ID", CosmosUtils.getStringIDValue(id));
        final SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, param);
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);
        options.setIndexMetricsEnabled(this.indexMetricsEnabled);
        options.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        options.setMaxBufferedItemCount(this.maxBufferedItemCount);
        options.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);

        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(finalContainerName)
                                .queryItems(sqlQuerySpec, options, JsonNode.class)
                                .byPage()
                                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                                .flatMap(cosmosItemFeedResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemFeedResponse.getCosmosDiagnostics(),
                                        cosmosItemFeedResponse);
                                    return Mono.justOrEmpty(cosmosItemFeedResponse
                                        .getResults()
                                        .stream()
                                        .map(cosmosItem -> emitOnLoadEventAndConvertToDomainObject(domainType, finalContainerName, cosmosItem))
                                        .findFirst());
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable,
                                        this.responseDiagnosticsProcessor))
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
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(containerName)
                                .readItem(idToFind, partitionKey, JsonNode.class)
                                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.justOrEmpty(emitOnLoadEventAndConvertToDomainObject(domainType,
                                        containerName, cosmosItemResponse.getItem()));
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable,
                                        this.responseDiagnosticsProcessor));
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
    public <T> Mono<T> insert(String containerName, T objectToSave,
                              PartitionKey partitionKey) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        Assert.notNull(objectToSave, "objectToSave should not be null");
        containerName = getContainerNameOverride(containerName);
        final Class<T> domainType = (Class<T>) objectToSave.getClass();
        markAuditedIfConfigured(objectToSave);
        generateIdIfNullAndAutoGenerationEnabled(objectToSave, domainType);
        List<String> transientFields = mappingCosmosConverter.getTransientFields(objectToSave, null);
        Map<Field, Object> transientFieldValuesMap = new HashMap<>();
        JsonNode originalItem;
        if (!transientFields.isEmpty()) {
            originalItem = mappingCosmosConverter.writeJsonNode(objectToSave, transientFields);
            transientFieldValuesMap = mappingCosmosConverter.getTransientFieldsMap(objectToSave, transientFields);
        } else {
            originalItem = mappingCosmosConverter.writeJsonNode(objectToSave);
        }
        Map<Field, Object> finalTransientFieldValuesMap = transientFieldValuesMap;
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        //  if the partition key is null, SDK will get the partitionKey from the object
        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .createItem(originalItem, partitionKey, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable,
                    this.responseDiagnosticsProcessor))
            .flatMap(cosmosItemResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null);
                return Mono.just(toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(
                    cosmosItemResponse.getItem(), finalTransientFieldValuesMap)));
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

    /**
     * Insert all items with bulk.
     *
     * @param entityInformation the CosmosEntityInformation
     * @param entities the Iterable entities to be inserted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     * @return Flux of result
     */
    public <S extends T, T> Flux<S> insertAll(CosmosEntityInformation<T, ?> entityInformation, Iterable<S> entities) {
        return insertAll(entityInformation, Flux.fromIterable(entities));
    }

    /**
     * Insert all items with bulk.
     *
     * @param entityInformation the CosmosEntityInformation
     * @param entities the Flux of entities to be inserted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     * @return Flux of result
     */
    public <S extends T, T> Flux<S> insertAll(CosmosEntityInformation<T, ?> entityInformation, Flux<S> entities) {
        Assert.notNull(entities, "entities to be inserted should not be null");
        String containerName = entityInformation.getContainerName();
        Class<T> domainType = entityInformation.getJavaType();
        Map<String, Map<Field, Object>> mapOfTransientFieldValuesMaps = new HashMap<>();
        Flux<CosmosItemOperation> cosmosItemOperationsFlux = entities.map(entity -> {
            markAuditedIfConfigured(entity);
            generateIdIfNullAndAutoGenerationEnabled(entity, domainType);
            List<String> transientFields = mappingCosmosConverter.getTransientFields(entity, entityInformation);
            JsonNode originalItem;
            if (!transientFields.isEmpty()) {
                originalItem = mappingCosmosConverter.writeJsonNode(entity, transientFields);
                Map<Field, Object> transientFieldValuesMap = mappingCosmosConverter.getTransientFieldsMap(entity, transientFields);
                mapOfTransientFieldValuesMaps.put(originalItem.get("id").asText(), transientFieldValuesMap);

            } else {
                originalItem = mappingCosmosConverter.writeJsonNode(entity);
            }

            PartitionKey partitionKey = getPartitionKeyFromValue(entityInformation, entity);
            final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
            applyBulkVersioning(domainType, originalItem, options);
            return CosmosBulkOperations.getUpsertItemOperation(originalItem, partitionKey, options);
        });

        // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
        // allows it to start at 1 and increase until it finds the appropriate batch size.
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

        return (Flux<S>) this.getCosmosAsyncClient()
                             .getDatabase(this.getDatabaseName())
                             .getContainer(containerName)
                             .executeBulkOperations(cosmosItemOperationsFlux, cosmosBulkExecutionOptions)
                             .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                             .onErrorResume(throwable ->
                                 CosmosExceptionUtils.exceptionHandler("Failed to insert item(s)", throwable,
                                     this.responseDiagnosticsProcessor))
                             .flatMap(r -> {
                                 CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                     r.getResponse().getCosmosDiagnostics(), null);
                                 JsonNode responseItem = r.getResponse().getItem(JsonNode.class);
                                 if (responseItem != null) {
                                     if (mapOfTransientFieldValuesMaps.containsKey(responseItem.get("id").asText())) {
                                         Map<Field, Object> transientFieldValuesMap = mapOfTransientFieldValuesMaps.get(responseItem.get("id").asText());
                                         return Flux.just(toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(responseItem, transientFieldValuesMap)));
                                     } else {
                                         return Flux.just(toDomainObject(domainType, responseItem));
                                     }
                                 } else {
                                     return Flux.empty();
                                 }
                             });
    }

    /**
     * Patches item
     *
     * applies partial update (patch) to an item
     * @param id must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param patchOperations must not be {@literal null}
     * @param <T> type class of domain type
     * @return the patched item
     */
    @Override
    public <T> Mono<T> patch(Object id, PartitionKey partitionKey, Class<T> domainType, CosmosPatchOperations patchOperations) {
        return patch(id, partitionKey, domainType, patchOperations,  null);
    }

    /**
     * applies partial update (patch) to an item with CosmosPatchItemRequestOptions
     *
     * @param id must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param domainType must not be {@literal null}
     * @param patchOperations must not be {@literal null}
     * @param options Optional CosmosPatchItemRequestOptions, e.g. options.setFilterPredicate("FROM products p WHERE p.used = false");
     * @param <T> type class of domain type
     * @return the patched item
     */
    public <T> Mono<T> patch(Object id, PartitionKey partitionKey, Class<T> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options) {
        Assert.notNull(patchOperations, "expected non-null cosmosPatchOperations");

        final String containerName = getContainerName(domainType);
        Assert.notNull(id, "id should not be null");
        Assert.notNull(partitionKey, "partitionKey should not be null, empty or only whitespaces");
        Assert.notNull(patchOperations, "patchOperations should not be null, empty or only whitespaces");

        LOGGER.debug("execute patchItem in database {} container {}", this.getDatabaseName(), containerName);

        if (options == null) {
            options = new CosmosPatchItemRequestOptions();
        }

        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .patchItem(id.toString(), partitionKey, patchOperations, options, JsonNode.class)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to patch item", throwable,
                    this.responseDiagnosticsProcessor))
            .flatMap(cosmosItemResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null);
                return Mono.just(toDomainObject(domainType, cosmosItemResponse.getItem()));
            });

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
        containerName = getContainerNameOverride(containerName);
        final Class<T> domainType = (Class<T>) object.getClass();
        markAuditedIfConfigured(object);
        List<String> transientFields = mappingCosmosConverter.getTransientFields(object, null);
        Map<Field, Object> transientFieldValuesMap = new HashMap<>();
        JsonNode originalItem;
        if (!transientFields.isEmpty()) {
            originalItem = mappingCosmosConverter.writeJsonNode(object, transientFields);
            transientFieldValuesMap = mappingCosmosConverter.getTransientFieldsMap(object, transientFields);
        } else {
            originalItem = mappingCosmosConverter.writeJsonNode(object);
        }
        Map<Field, Object> finalTransientFieldValuesMap = transientFieldValuesMap;
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(object.getClass(), originalItem, options);

        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(containerName)
                                .upsertItem(originalItem, options)
                                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                                .flatMap(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return Mono.just(toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(
                                        cosmosItemResponse.getItem(), finalTransientFieldValuesMap)));
                                })
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to upsert item", throwable,
                                        this.responseDiagnosticsProcessor));
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
        containerName = getContainerNameOverride(containerName);
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");
        String idToDelete = CosmosUtils.getStringIDValue(id);

        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }

        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(containerName)
                                .deleteItem(idToDelete, partitionKey, cosmosItemRequestOptions)
                                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                                .doOnNext(cosmosItemResponse ->
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null))
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable,
                                        this.responseDiagnosticsProcessor))
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
     * Delete all items with bulk.
     *
     * @param entityInformation the CosmosEntityInformation
     * @param entities the Iterable entities to be deleted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     * @return void Mono
     */
    public <S extends T, T> Mono<Void> deleteEntities(CosmosEntityInformation<T, ?> entityInformation, Iterable<S> entities) {
        return deleteEntities(entityInformation, Flux.fromIterable(entities));
    }

    /**
     * Delete all items with bulk.
     *
     * @param entityInformation the CosmosEntityInformation
     * @param entities the Iterable entities to be deleted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     * @return void Mono
     */
    public <S extends T, T> Mono<Void> deleteEntities(CosmosEntityInformation<T, ?> entityInformation, Flux<S> entities) {
        Assert.notNull(entities, "entities to be deleted should not be null");

        String containerName = entityInformation.getContainerName();
        Class<T> domainType = entityInformation.getJavaType();

        Flux<CosmosItemOperation> cosmosItemOperationFlux = entities.map(entity -> {
            JsonNode originalItem = mappingCosmosConverter.writeJsonNode(entity);
            PartitionKey partitionKey = getPartitionKeyFromValue(entityInformation, entity);
            final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
            applyBulkVersioning(domainType, originalItem, options);
            return CosmosBulkOperations.getDeleteItemOperation(String.valueOf(entityInformation.getId(entity)),
                partitionKey, options);
        });

        // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
        // allows it to start at 1 and increase until it finds the appropriate batch size.
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

        return this.getCosmosAsyncClient()
                   .getDatabase(this.getDatabaseName())
                   .getContainer(containerName)
                   .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions)
                   .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                   .onErrorResume(throwable ->
                       CosmosExceptionUtils.exceptionHandler("Failed to delete item(s)", throwable,
                           this.responseDiagnosticsProcessor)).then();
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
     * Delete items matching query with bulk if PK exists
     *
     * @param query the document query
     * @param domainType the entity class
     * @param containerName the container name
     * @return Mono
     */
    @Override
    public <T> Flux<T> delete(CosmosQuery query, Class<T> domainType, String containerName) {
        String finalContainerName = getContainerNameOverride(containerName);
        Assert.notNull(query, "DocumentQuery should not be null.");
        Assert.notNull(domainType, "domainType should not be null.");
        Assert.hasText(containerName, "container name should not be null, empty or only whitespaces");

        @SuppressWarnings("unchecked")
        CosmosEntityInformation<T, Object> entityInfo = (CosmosEntityInformation<T, Object>) CosmosEntityInformation.getInstance(domainType);

        final Flux<JsonNode> results = findItems(query, finalContainerName, domainType);

        if (entityInfo.getPartitionKeyFieldName() != null) {
            Flux<CosmosItemOperation> cosmosItemOperationFlux = results.map(item -> {
                T object = toDomainObject(domainType, item);
                Object id = entityInfo.getId(object);
                String idString = id != null ? id.toString() : "";
                final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
                applyBulkVersioning(domainType, item, options);
                return CosmosBulkOperations.getDeleteItemOperation(idString,
                    getPartitionKeyFromValue(entityInfo, object), options,
                    object); // setup the original object in the context
            });

            // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
            // allows it to start at 1 and increase until it finds the appropriate batch size.
            CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
            cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

            return this.getCosmosAsyncClient()
                .getDatabase(this.getDatabaseName())
                .getContainer(containerName)
                .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions)
                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                .onErrorResume(throwable ->
                    CosmosExceptionUtils.exceptionHandler("Failed to delete item(s)", throwable,
                        this.responseDiagnosticsProcessor))
                .map(itemResponse -> itemResponse.getOperation().getContext());
        } else {
            return results.flatMap(d -> deleteItem(d, finalContainerName, domainType));
        }
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
        return findItems(query, containerName, domainType)
            .map(cosmosItemProperties -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, cosmosItemProperties));
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
            .flatMap(o -> Mono.just(o != null))
            .switchIfEmpty(Mono.just(false));
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
        final SqlQuerySpec querySpec = new CountQueryGenerator().generateCosmos(query);
        return getCountValue(querySpec, containerName);
    }

    /**
     * Count
     *
     * @param querySpec the document query spec
     * @param containerName the container name
     * @return Mono with count or error
     */
    @Override
    public Mono<Long> count(SqlQuerySpec querySpec, String containerName) {
        return getCountValue(querySpec, containerName);
    }

    @Override
    public MappingCosmosConverter getConverter() {
        return mappingCosmosConverter;
    }

    @Override
    public <T> Flux<T> runQuery(SqlQuerySpec querySpec, Class<?> domainType, Class<T> returnType) {
        return runQuery(querySpec, Sort.unsorted(), domainType, returnType);
    }

    @Override
    public <T> Flux<T> runQuery(SqlQuerySpec querySpec, Sort sort, Class<?> domainType, Class<T> returnType) {
        SqlQuerySpec sortedQuerySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, sort);
        return runQuery(sortedQuerySpec, domainType)
            .map(cosmosItemProperties -> emitOnLoadEventAndConvertToDomainObject(returnType, getContainerName(domainType), cosmosItemProperties));
    }

    private Flux<JsonNode> runQuery(SqlQuerySpec querySpec, Class<?> domainType) {
        String containerName = getContainerName(domainType);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        options.setMaxBufferedItemCount(this.maxBufferedItemCount);
        options.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                   .getContainer(containerName)
                   .queryItems(querySpec, options, JsonNode.class)
                   .byPage()
                   .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                   .flatMap(cosmosItemFeedResponse -> {
                       CosmosUtils
                           .fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                                              cosmosItemFeedResponse.getCosmosDiagnostics(),
                                                              cosmosItemFeedResponse);
                       return Flux.fromIterable(cosmosItemFeedResponse.getResults());
                   })
                   .onErrorResume(throwable ->
                                      CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable,
                                          this.responseDiagnosticsProcessor));
    }

    private Mono<Long> getCountValue(SqlQuerySpec querySpec, String containerName) {
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);
        options.setIndexMetricsEnabled(this.indexMetricsEnabled);
        options.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        options.setMaxBufferedItemCount(this.maxBufferedItemCount);
        options.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);

        return executeQuery(querySpec, containerName, options)
            .doOnNext(feedResponse -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                feedResponse.getCosmosDiagnostics(), feedResponse))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to get count value", throwable,
                    this.responseDiagnosticsProcessor))
            .next()
            .map(r -> r.getResults().get(0).asLong());
    }

    private Flux<FeedResponse<JsonNode>> executeQuery(SqlQuerySpec sqlQuerySpec,
                                                      String containerName,
                                                      CosmosQueryRequestOptions options) {
        containerName = getContainerNameOverride(containerName);
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(containerName)
                                .queryItems(sqlQuerySpec, options, JsonNode.class)
                                .byPage()
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to execute query", throwable,
                                        this.responseDiagnosticsProcessor));
    }

    /**
     * Delete container with container name
     *
     * @param containerName the container name
     */
    @Override
    public void deleteContainer(@NonNull String containerName) {
        containerName = getContainerNameOverride(containerName);
        Assert.hasText(containerName, "containerName should have text.");
        this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                         .getContainer(containerName)
                         .delete()
                         .doOnNext(cosmosContainerResponse ->
                             CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                 cosmosContainerResponse.getDiagnostics(), null))
                         .onErrorResume(throwable ->
                             CosmosExceptionUtils.exceptionHandler("Failed to delete container",
                                 throwable, this.responseDiagnosticsProcessor))
                         .block();
    }

    @Override
    public String getContainerName(Class<?> domainType) {
        Assert.notNull(domainType, "domainType should not be null");
        return getContainerNameOverride(CosmosEntityInformation.getInstance(domainType).getContainerName());
    }

    /**
     * Check if an overridden version of containerName is present, and if so, return it
     *
     * @param containerName Container name of database
     * @return containerName
     */
    public String getContainerNameOverride(String containerName) {
        if (this.cosmosFactory.overrideContainerName() != null) {
            return this.cosmosFactory.overrideContainerName();
        }
        Assert.notNull(containerName, "containerName should not be null");
        return containerName;
    }

    private void markAuditedIfConfigured(Object object) {
        if (cosmosAuditingHandler != null) {
            cosmosAuditingHandler.markAudited(object);
        }
    }

    private <T> Flux<JsonNode> findItems(@NonNull CosmosQuery query,
                                         @NonNull String containerName,
                                         @NonNull Class<T> domainType) {
        containerName = getContainerNameOverride(containerName);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);
        cosmosQueryRequestOptions.setIndexMetricsEnabled(this.indexMetricsEnabled);
        cosmosQueryRequestOptions.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        cosmosQueryRequestOptions.setMaxBufferedItemCount(this.maxBufferedItemCount);
        cosmosQueryRequestOptions.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        Optional<Object> partitionKeyValue = query.getPartitionKeyValue(domainType);
        partitionKeyValue.ifPresent(o -> {
            LOGGER.debug("Setting partition key {}", o);
            cosmosQueryRequestOptions.setPartitionKey(new PartitionKey(o));
        });

        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to query items", throwable,
                    this.responseDiagnosticsProcessor));
    }

    private <T> Mono<T> deleteItem(@NonNull JsonNode jsonNode,
                                   String containerName,
                                   @NonNull Class<T> domainType) {
        containerName = getContainerNameOverride(containerName);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, jsonNode, options);

        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
                                .getContainer(containerName)
                                .deleteItem(jsonNode, options)
                                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                                .map(cosmosItemResponse -> {
                                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                                        cosmosItemResponse.getDiagnostics(), null);
                                    return cosmosItemResponse;
                                })
                                .flatMap(objectCosmosItemResponse -> Mono.just(toDomainObject(domainType, jsonNode)))
                                .onErrorResume(throwable ->
                                    CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable,
                                        this.responseDiagnosticsProcessor));
    }

    private <T> T emitOnLoadEventAndConvertToDomainObject(@NonNull Class<T> domainType, String containerName, JsonNode responseJsonNode) {
        containerName = getContainerNameOverride(containerName);
        maybeEmitEvent(new AfterLoadEvent<>(responseJsonNode, domainType, containerName));
        return toDomainObject(domainType, responseJsonNode);
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

    private void applyBulkVersioning(Class<?> domainType,
                                     JsonNode jsonNode,
                                     CosmosBulkItemRequestOptions options) {
        CosmosEntityInformation<?, ?> entityInformation = CosmosEntityInformation.getInstance(domainType);
        if (entityInformation.isVersioned()) {
            options.setIfMatchETag(jsonNode.get(Constants.ETAG_PROPERTY_DEFAULT_NAME).asText());
        }
    }

    @SuppressWarnings("unchecked")
    private <T, S extends T> PartitionKey getPartitionKeyFromValue(CosmosEntityInformation<T, ?> information, S entity) {
        Object pkFieldValue = information.getPartitionKeyFieldValue(entity);
        PartitionKey partitionKey;
        if (pkFieldValue instanceof Collection<?>) {
            ArrayList<Object> valueArray = ((ArrayList<Object>) pkFieldValue);
            Object[] objectArray = new Object[valueArray.size()];
            for (int i = 0; i < valueArray.size(); i++) {
                objectArray[i] = valueArray.get(i);
            }
            partitionKey = PartitionKey.fromObjectArray(objectArray, false);
        } else {
            partitionKey = new PartitionKey(pkFieldValue);
        }
        return partitionKey;
    }

    private <T> CosmosContainerProperties getCosmosContainerPropertiesWithPartitionKeyPath(CosmosEntityInformation<T, ?> information) {
        String pkPath = information.getPartitionKeyPath();
        if (pkPath.contains(",")) {
            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            partitionKeyDef.setKind(PartitionKind.MULTI_HASH);
            partitionKeyDef.setVersion(PartitionKeyDefinitionVersion.V2);
            ArrayList<String> pkDefPaths = new ArrayList<>();
            List<String> paths = Arrays.stream(pkPath.split(",")).collect(Collectors.toList());
            paths.forEach(path -> {
                pkDefPaths.add(path.trim());
            });
            partitionKeyDef.setPaths(pkDefPaths);
            return new CosmosContainerProperties(getContainerNameOverride(information.getContainerName()), partitionKeyDef);
        } else {
            return new CosmosContainerProperties(getContainerNameOverride(information.getContainerName()), pkPath);
        }
    }

    private void maybeEmitEvent(CosmosMappingEvent<?> event) {
        if (canPublishEvent()) {
            this.applicationContext.publishEvent(event);
        }
    }

    private boolean canPublishEvent() {
        return this.applicationContext != null;
    }

}
