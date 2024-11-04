// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private final CosmosFactory cosmosFactory;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
    private final boolean queryMetricsEnabled;
    private final boolean indexMetricsEnabled;
    private final int maxDegreeOfParallelism;
    private final int maxBufferedItemCount;
    private final int responseContinuationTokenLimitInKb;
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
        this.cosmosFactory = cosmosFactory;
        this.responseDiagnosticsProcessor = cosmosConfig.getResponseDiagnosticsProcessor();
        this.queryMetricsEnabled = cosmosConfig.isQueryMetricsEnabled();
        this.indexMetricsEnabled = cosmosConfig.isIndexMetricsEnabled();
        this.maxDegreeOfParallelism = cosmosConfig.getMaxDegreeOfParallelism();
        this.maxBufferedItemCount = cosmosConfig.getMaxBufferedItemCount();
        this.responseContinuationTokenLimitInKb = cosmosConfig.getResponseContinuationTokenLimitInKb();
        this.databaseThroughputConfig = cosmosConfig.getDatabaseThroughputConfig();
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

    private String getDatabaseName() {
        return this.cosmosFactory.getDatabaseName();
    }

    private CosmosAsyncClient getCosmosAsyncClient() {
        return this.cosmosFactory.getCosmosAsyncClient();
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

        List<String> transientFields = mappingCosmosConverter.getTransientFields(objectToSave, null);
        Map<Field, Object> transientFieldValuesMap = new HashMap<>();
        JsonNode originalItem;
        if (!transientFields.isEmpty()) {
            originalItem = mappingCosmosConverter.writeJsonNode(objectToSave, transientFields);
            transientFieldValuesMap = mappingCosmosConverter.getTransientFieldsMap(objectToSave, transientFields);
        } else {
            originalItem = mappingCosmosConverter.writeJsonNode(objectToSave);
        }

        containerName = getContainerName(domainType);

        LOGGER.debug("execute createItem in database {} container {}", this.getDatabaseName(), containerName);

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        //  if the partition key is null, SDK will get the partitionKey from the object
        final CosmosItemResponse<JsonNode> response = this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .createItem(originalItem, partitionKey, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(cosmosItemResponse ->
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to insert item", throwable,
                    this.responseDiagnosticsProcessor))
            .block();

        assert response != null;
        return toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(
            response.getItem(), transientFieldValuesMap));
    }

    /**
     * Insert all items with bulk.
     *
     * @param information the CosmosEntityInformation
     * @param entities the Iterable entities to be inserted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     * @return Flux of result
     */
    @SuppressWarnings("unchecked")
    public <S extends T, T> Iterable<S> insertAll(CosmosEntityInformation<T, ?> information, Iterable<S> entities) {
        Assert.notNull(entities, "entities to be inserted should not be null");
        String containerName = information.getContainerName();
        Class<T> domainType = information.getJavaType();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        Map<String, Map<Field, Object>> mapOfTransientFieldValuesMaps = new HashMap<>();
        entities.forEach(entity -> {
            markAuditedIfConfigured(entity);
            generateIdIfNullAndAutoGenerationEnabled(entity, domainType);
            List<String> transientFields = mappingCosmosConverter.getTransientFields(entity, information);
            JsonNode originalItem;
            if (!transientFields.isEmpty()) {
                originalItem = mappingCosmosConverter.writeJsonNode(entity, transientFields);
                Map<Field, Object> transientFieldValuesMap = mappingCosmosConverter.getTransientFieldsMap(entity, transientFields);
                mapOfTransientFieldValuesMaps.put(originalItem.get("id").asText(), transientFieldValuesMap);
            } else {
                originalItem = mappingCosmosConverter.writeJsonNode(entity);
            }
            PartitionKey partitionKey = getPartitionKeyFromValue(information, entity);
            final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
            applyBulkVersioning(domainType, originalItem, options);
            cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(originalItem,
                partitionKey, options));
        });

        // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
        // allows it to start at 1 and increase until it finds the appropriate batch size.
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

        return (Iterable<S>) this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .executeBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to insert item(s)", throwable,
                    this.responseDiagnosticsProcessor))
            .flatMap(r -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    r.getResponse().getCosmosDiagnostics(), null);
                JsonNode responseItem = r.getResponse().getItem(JsonNode.class);
                if (responseItem != null) {
                    if (!mapOfTransientFieldValuesMaps.isEmpty()) {
                        Map<Field, Object> transientFieldValuesMap = mapOfTransientFieldValuesMaps.get(responseItem.get("id").asText());
                        return Flux.just(toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(responseItem, transientFieldValuesMap)));
                    } else {
                        return Flux.just(toDomainObject(domainType, responseItem));
                    }
                } else {
                    return Flux.empty();
                }
            })
            .collectList().block();
    }

    /**
     * Patches item
     *
     * applies partial update (patch) to an item
     * @param id must not be {@literal null}
     * @param partitionKey must not be {@literal null}
     * @param patchOperations must not be {@literal null}
     * @param <T> type class of domain type
     * @return the patched item
     */
    @Override
    public <T> T patch(Object id, PartitionKey partitionKey, Class<T> domainType, CosmosPatchOperations patchOperations) {
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
    public <T> T patch(Object id, PartitionKey partitionKey, Class<T> domainType, CosmosPatchOperations patchOperations, CosmosPatchItemRequestOptions options) {
        Assert.notNull(patchOperations, "expected non-null cosmosPatchOperations");

        final String containerName = getContainerName(domainType);
        Assert.notNull(id, "id should not be null");
        Assert.notNull(partitionKey, "partitionKey should not be null, empty or only whitespaces");
        Assert.notNull(patchOperations, "patchOperations should not be null, empty or only whitespaces");

        LOGGER.debug("execute patchItem in database {} container {}", this.getDatabaseName(),
            containerName);

        if (options == null) {
            options = new CosmosPatchItemRequestOptions();
        }
        final CosmosItemResponse<JsonNode> response = this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .patchItem(id.toString(), partitionKey, patchOperations, options, JsonNode.class)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(cosmosItemResponse ->
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to patch item", throwable,
                    this.responseDiagnosticsProcessor))
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
        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .readItem(idToQuery, partitionKey, JsonNode.class)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .flatMap(cosmosItemResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemResponse.getDiagnostics(), null);
                return Mono.justOrEmpty(emitOnLoadEventAndConvertToDomainObject(domainType, containerName, cosmosItemResponse.getItem()));
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable,
                    this.responseDiagnosticsProcessor))
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
        CosmosEntityInformation<?, ?> cosmosEntityInformation = CosmosEntityInformation.getInstance(domainType);
        String containerPartitionKey = cosmosEntityInformation.getPartitionKeyFieldName();
        if ("id".equals(containerPartitionKey) && id != null) {
            return findById(id, domainType, new PartitionKey(id));
        }
        if (!this.pointReadWarningLogged) {
            LOGGER.warn("The partitionKey is not id!! Consider using findById(ID id, PartitionKey partitionKey) instead to avoid the need for using a cross partition query which results in higher latency and cost than necessary. See https://aka.ms/PointReadsInSpring for more info.");
            this.pointReadWarningLogged = true;
        }
        String finalContainerName = getContainerNameOverride(containerName);
        final String query = "select * from root where root.id = @ROOT_ID";
        final SqlParameter param = new SqlParameter("@ROOT_ID", CosmosUtils.getStringIDValue(id));
        final SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, param);
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(this.queryMetricsEnabled);
        options.setIndexMetricsEnabled(this.indexMetricsEnabled);
        options.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        options.setMaxBufferedItemCount(this.maxBufferedItemCount);
        options.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(finalContainerName)
            .queryItems(sqlQuerySpec, options, JsonNode.class)
            .byPage()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(), cosmosItemFeedResponse);
                return Mono.justOrEmpty(cosmosItemFeedResponse
                    .getResults()
                    .stream()
                    .map(cosmosItem -> emitOnLoadEventAndConvertToDomainObject(domainType, finalContainerName, cosmosItem))
                    .findFirst());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.findAPIExceptionHandler("Failed to find item", throwable,
                    this.responseDiagnosticsProcessor))
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
        containerName = getContainerName(object.getClass());
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

        LOGGER.debug("execute upsert item in database {} container {}", this.getDatabaseName(),
            containerName);

        @SuppressWarnings("unchecked") final Class<T> domainType = (Class<T>) object.getClass();
        containerName = getContainerName(domainType);

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, originalItem, options);

        final CosmosItemResponse<JsonNode> cosmosItemResponse = this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .upsertItem(originalItem, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to upsert item", throwable,
                    this.responseDiagnosticsProcessor))
            .block();

        assert cosmosItemResponse != null;
        return toDomainObject(domainType, mappingCosmosConverter.repopulateTransientFields(
            cosmosItemResponse.getItem(), transientFieldValuesMap));
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
            .map(jsonNode -> emitOnLoadEventAndConvertToDomainObject(domainType, containerName, jsonNode))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable,
                    this.responseDiagnosticsProcessor))
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
        containerName = getContainerName(domainType);
        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));

        this.delete(query, domainType, containerName);
    }

    @Override
    public void deleteContainer(@NonNull String containerName) {
        Assert.hasText(containerName, "containerName should have text.");
        this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .delete()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(response -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    response.getDiagnostics(), null);
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to delete container", throwable,
                    this.responseDiagnosticsProcessor))
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

    @Override
    public CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation<?, ?> information) {

        final CosmosContainerResponse response = createDatabaseIfNotExists()
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

                CosmosAsyncDatabase cosmosAsyncDatabase = this.getCosmosAsyncClient()
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
                            throwable, this.responseDiagnosticsProcessor))
                    .doOnNext(cosmosContainerResponse ->
                        CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                            cosmosContainerResponse.getDiagnostics(), null));
            })
            .block();
        assert response != null;
        return response.getProperties();
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
    public CosmosContainerProperties getContainerProperties(String containerName) {
        containerName = getContainerNameOverride(containerName);
        final CosmosContainerResponse response = this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .read()
            .block();
        assert response != null;
        return response.getProperties();
    }

    @Override
    public CosmosContainerProperties replaceContainerProperties(String containerName,
                                                                CosmosContainerProperties properties) {
        CosmosContainerResponse response = this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
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
        containerName = getContainerNameOverride(containerName);
        Assert.notNull(entity, "entity to be deleted should not be null");
        @SuppressWarnings("unchecked") final Class<T> domainType = (Class<T>) entity.getClass();
        final JsonNode originalItem = mappingCosmosConverter.writeJsonNode(entity);
        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(entity.getClass(), originalItem, options);
        deleteItem(originalItem, containerName, domainType);
    }

    /**
     * Deletes the entities using bulk
     *
     * @param information the CosmosEntityInformation
     * @param entities the Iterable entities to be inserted
     * @param <T> type class of domain type
     * @param <S> type class of domain type
     */
    @Override
    public <S extends T, T> void deleteEntities(CosmosEntityInformation<T, ?> information, Iterable<S> entities) {
        Assert.notNull(entities, "entities to be deleted should not be null");

        String containerName = information.getContainerName();
        Class<T> domainType = information.getJavaType();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        entities.forEach(entity -> {
            JsonNode originalItem = mappingCosmosConverter.writeJsonNode(entity);
            final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
            applyBulkVersioning(domainType, originalItem, options);
            cosmosItemOperations.add(CosmosBulkOperations.getDeleteItemOperation(String.valueOf(information.getId(entity)),
                getPartitionKeyFromValue(information, entity), options));
        });

        // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
        // allows it to start at 1 and increase until it finds the appropriate batch size.
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

        this.getCosmosAsyncClient()
                .getDatabase(this.getDatabaseName())
                .getContainer(containerName)
                .executeBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions)
                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                .onErrorResume(throwable ->
                    CosmosExceptionUtils.exceptionHandler("Failed to delete item(s)", throwable,
                        this.responseDiagnosticsProcessor))
                .flatMap(response -> {
                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                        response.getResponse().getCosmosDiagnostics(), null);
                    return Flux.empty();
                }).blockLast();
    }

    private void deleteById(String containerName, Object id, PartitionKey partitionKey,
                            CosmosItemRequestOptions options) {
        Assert.hasText(containerName, "containerName should not be null, empty or only whitespaces");
        containerName = getContainerNameOverride(containerName);
        String idToDelete = CosmosUtils.getStringIDValue(id);
        LOGGER.debug("execute deleteById in database {} container {}", this.getDatabaseName(),
            containerName);

        if (partitionKey == null) {
            partitionKey = PartitionKey.NONE;
        }

        this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .deleteItem(idToDelete, partitionKey, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(response ->
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    response.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to delete item",
                    throwable, this.responseDiagnosticsProcessor))
            .block();
    }

    @Override
    public <T, ID> Iterable<T> findByIds(Iterable<ID> ids, Class<T> domainType, String containerName) {
        containerName = getContainerNameOverride(containerName);
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
     * Sql API do _NOT_ support DELETE query, we cannot add one DeleteQueryGenerator. Uses bulk if possible.
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
        String finalContainerName = getContainerNameOverride(containerName);

        @SuppressWarnings("unchecked")
        CosmosEntityInformation<T, Object> entityInfo = (CosmosEntityInformation<T, Object>) CosmosEntityInformation.getInstance(domainType);

        final List<JsonNode> results = findItemsAsFlux(query, finalContainerName, domainType).collectList().block();
        assert results != null;

        if (entityInfo.getPartitionKeyFieldName() != null) {
            Flux<CosmosItemOperation> cosmosItemOperationFlux = Flux.fromIterable(results).map(item -> {
                T object = toDomainObject(domainType, item);
                Object id = entityInfo.getId(object);
                String idString = id != null ? id.toString() : "";
                final CosmosBulkItemRequestOptions options = new CosmosBulkItemRequestOptions();
                applyBulkVersioning(domainType, item, options);
                return CosmosBulkOperations.getDeleteItemOperation(idString,
                    getPartitionKeyFromValue(entityInfo, object), options);
            });

            // Default micro batch size is 100 which will be too high for most Spring cases, this configuration
            // allows it to start at 1 and increase until it finds the appropriate batch size.
            CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
            cosmosBulkExecutionOptions.setInitialMicroBatchSize(1);

            this.getCosmosAsyncClient()
                .getDatabase(this.getDatabaseName())
                .getContainer(containerName)
                .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions)
                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                .onErrorResume(throwable ->
                    CosmosExceptionUtils.exceptionHandler("Failed to delete item(s)",
                        throwable, this.responseDiagnosticsProcessor))
                .collectList().block();

            return results.stream()
                .map(jsonNode -> toDomainObject(domainType, jsonNode))
                .collect(Collectors.toList());
        } else {
            return results.stream()
                .map(item -> deleteItem(item, finalContainerName, domainType))
                .collect(Collectors.toList());
        }
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
        containerName = getContainerNameOverride(containerName);
        final SqlQuerySpec querySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final SqlQuerySpec countQuerySpec = new CountQueryGenerator().generateCosmos(query);
        Optional<Object> partitionKeyValue = query.getPartitionKeyValue(domainType);
        return paginationQuery(querySpec, countQuerySpec, query.getPageable(),
            query.getSort(), domainType, containerName, partitionKeyValue);
    }

    @Override
    public <T> Slice<T> sliceQuery(CosmosQuery query, Class<T> domainType, String containerName) {
        containerName = getContainerNameOverride(containerName);
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
        containerName = getContainerNameOverride(containerName);
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
        containerName = getContainerNameOverride(containerName);
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);
        cosmosQueryRequestOptions.setIndexMetricsEnabled(this.indexMetricsEnabled);
        cosmosQueryRequestOptions.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        cosmosQueryRequestOptions.setMaxBufferedItemCount(this.maxBufferedItemCount);
        cosmosQueryRequestOptions.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        partitionKeyValue.ifPresent(o -> {
            LOGGER.debug("Setting partition key {}", o);
            cosmosQueryRequestOptions.setPartitionKey(new PartitionKey(o));
        });

        CosmosAsyncContainer container =
            this.getCosmosAsyncClient().getDatabase(this.getDatabaseName()).getContainer(containerName);

        Flux<FeedResponse<JsonNode>> feedResponseFlux;
        /*
         * The user can pass in an offset with the pageable, if this is done we need to apply
         * the offset to the first page so that we can shift the data and skip the number of
         * offset records. Starting with the 2nd page it picks up where the first page left off
         * so we do not need to apply the offset as the continuation token handles the pages.
         *
         * We only use offset on the first page because of the use of continuation tokens.
         * After we apply the offset to the first page, the continuation token will pick
         * up the second and future pages at the correct index.
         */
        int feedResponseContentSize = pageable.getPageSize();
        String continuationToken = null;
        int offsetForPageWithoutContToken = 0;
        if (pageable instanceof CosmosPageRequest) {
            if (((CosmosPageRequest) pageable).getRequestContinuation() == null) {
                feedResponseContentSize = (int) (feedResponseContentSize
                    + (feedResponseContentSize * pageable.getPageNumber()) + pageable.getOffset());
                offsetForPageWithoutContToken = (pageable.getPageNumber() * pageable.getPageSize())
                    + (int) pageable.getOffset();
            }
            continuationToken = ((CosmosPageRequest) pageable).getRequestContinuation();
        } else {
            feedResponseContentSize = feedResponseContentSize + (feedResponseContentSize * pageable.getPageNumber());
            offsetForPageWithoutContToken = (pageable.getPageNumber() * pageable.getPageSize());
        }

        final List<T> result = new ArrayList<>();
        do {
            feedResponseFlux = container
                .queryItems(querySpec, cosmosQueryRequestOptions, JsonNode.class)
                .byPage(continuationToken, feedResponseContentSize);
            FeedResponse<JsonNode> feedResponse = feedResponseFlux
                .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
                .doOnNext(propertiesFeedResponse ->
                    CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                        propertiesFeedResponse.getCosmosDiagnostics(), propertiesFeedResponse))
                .onErrorResume(throwable ->
                    CosmosExceptionUtils.exceptionHandler("Failed to query items", throwable,
                        this.responseDiagnosticsProcessor))
                .next()
                .block();
            assert feedResponse != null;
            Iterator<JsonNode> it = feedResponse.getResults().iterator();

            for (int index = 0; it.hasNext()
                && index < pageable.getPageSize() + offsetForPageWithoutContToken; index++) {

                final JsonNode jsonNode = it.next();
                if (jsonNode == null) {
                    continue;
                }

                if (index >= offsetForPageWithoutContToken) {
                    maybeEmitEvent(new AfterLoadEvent<>(jsonNode, returnType, containerName));
                    final T entity = mappingCosmosConverter.read(returnType, jsonNode);
                    result.add(entity);
                }
            }

            if (result.size() < pageable.getPageSize()) {
                feedResponseContentSize = pageable.getPageSize() - result.size();
            }
            continuationToken = feedResponse.getContinuationToken();
            offsetForPageWithoutContToken = 0;
        } while (result.size() < pageable.getPageSize() && continuationToken != null);

        final CosmosPageRequest pageRequest = CosmosPageRequest.of(pageable.getOffset(),
            pageable.getPageNumber(),
            pageable.getPageSize(),
            continuationToken,
            sort);

        return new CosmosSliceImpl<>(result, pageRequest, continuationToken != null);
    }

    @Override
    public long count(String containerName) {
        Assert.hasText(containerName, "container name should not be empty");
        containerName = getContainerNameOverride(containerName);
        final CosmosQuery query = new CosmosQuery(Criteria.getInstance(CriteriaType.ALL));
        final Long count = getCountValue(query, containerName);
        assert count != null;
        return count;
    }

    @Override
    public <T> long count(CosmosQuery query, String containerName) {
        containerName = getContainerNameOverride(containerName);
        Assert.hasText(containerName, "container name should not be empty");

        final Long count = getCountValue(query, containerName);
        assert count != null;
        return count;
    }

    @Override
    public <T> long count(SqlQuerySpec querySpec, String containerName) {
        containerName = getContainerNameOverride(containerName);
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
        options.setIndexMetricsEnabled(this.indexMetricsEnabled);
        options.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        options.setMaxBufferedItemCount(this.maxBufferedItemCount);
        options.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        containerName = getContainerNameOverride(containerName);

        return executeQuery(querySpec, containerName, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to get count value", throwable,
                    this.responseDiagnosticsProcessor))
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getCosmosDiagnostics(), response))
            .next()
            .map(r -> r.getResults().get(0).asLong())
            .block();
    }

    private Flux<FeedResponse<JsonNode>> executeQuery(SqlQuerySpec sqlQuerySpec,
                                                      String containerName,
                                                      CosmosQueryRequestOptions options) {
        containerName = getContainerNameOverride(containerName);
        return this.getCosmosAsyncClient().getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, options, JsonNode.class)
            .byPage();
    }

    private <T> Flux<JsonNode> findItemsAsFlux(@NonNull CosmosQuery query,
                                               @NonNull String containerName,
                                               @NonNull Class<T> domainType) {
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        containerName = getContainerNameOverride(containerName);
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
                    cosmosItemFeedResponse.getCosmosDiagnostics(),
                    cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable,
                    this.responseDiagnosticsProcessor));
    }

    private Flux<JsonNode> getJsonNodeFluxFromQuerySpec(
        @NonNull String containerName, SqlQuerySpec sqlQuerySpec) {
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setQueryMetricsEnabled(this.queryMetricsEnabled);
        cosmosQueryRequestOptions.setIndexMetricsEnabled(this.indexMetricsEnabled);
        cosmosQueryRequestOptions.setMaxDegreeOfParallelism(this.maxDegreeOfParallelism);
        cosmosQueryRequestOptions.setMaxBufferedItemCount(this.maxBufferedItemCount);
        cosmosQueryRequestOptions.setResponseContinuationTokenLimitInKb(this.responseContinuationTokenLimitInKb);
        containerName = getContainerNameOverride(containerName);

        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, JsonNode.class)
            .byPage()
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .flatMap(cosmosItemFeedResponse -> {
                CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                    cosmosItemFeedResponse.getCosmosDiagnostics(),
                    cosmosItemFeedResponse);
                return Flux.fromIterable(cosmosItemFeedResponse.getResults());
            })
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to find items", throwable,
                    this.responseDiagnosticsProcessor));
    }

    private <T> Iterable<T> findItems(@NonNull CosmosQuery query,
                                      @NonNull String containerName,
                                      @NonNull Class<T> domainType) {
        String finalContainerName = getContainerNameOverride(containerName);
        return findItemsAsFlux(query, finalContainerName, domainType)
            .map(jsonNode -> emitOnLoadEventAndConvertToDomainObject(domainType, finalContainerName, jsonNode))
            .toIterable();
    }

    private <T> T deleteItem(@NonNull JsonNode jsonNode,
                             String containerName,
                             @NonNull Class<T> domainType) {

        final CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        applyVersioning(domainType, jsonNode, options);
        containerName = getContainerNameOverride(containerName);

        return this.getCosmosAsyncClient()
            .getDatabase(this.getDatabaseName())
            .getContainer(containerName)
            .deleteItem(jsonNode, options)
            .publishOn(CosmosSchedulers.SPRING_DATA_COSMOS_PARALLEL)
            .doOnNext(response -> CosmosUtils.fillAndProcessResponseDiagnostics(this.responseDiagnosticsProcessor,
                response.getDiagnostics(), null))
            .onErrorResume(throwable ->
                CosmosExceptionUtils.exceptionHandler("Failed to delete item", throwable,
                    this.responseDiagnosticsProcessor))
            .flatMap(objectCosmosItemResponse -> Mono.just(toDomainObject(domainType, jsonNode)))
            .block();
    }

    private <T> T emitOnLoadEventAndConvertToDomainObject(@NonNull Class<T> domainType, String containerName, JsonNode responseJsonNode) {
        containerName = getContainerNameOverride(containerName);
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
