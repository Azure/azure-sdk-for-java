// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Provides an asynchronous service client for accessing a table in the Azure Tables service.
 *
 * The client encapsulates the URL for the table within the Tables service endpoint, the name of the table, and the
 * credentials for accessing the storage or CosmosDB table API account. It provides methods to create and delete the
 * table itself, as well as methods to create, upsert, update, delete, list, and get entities within the table. These
 * methods invoke REST API operations to make the requests and obtain the results that are returned.
 *
 * Instances of this client are obtained by calling the {@link TableClientBuilder#buildAsyncClient()} method on a
 * {@link TableClientBuilder} object.
 */
@ServiceClient(builder = TableClientBuilder.class, isAsync = true)
public final class TableAsyncClient {
    private static final String DELIMITER_CONTINUATION_TOKEN = ";";
    private final ClientLogger logger = new ClientLogger(TableAsyncClient.class);
    private final String tableName;
    private final AzureTableImpl implementation;
    private final SerializerAdapter serializerAdapter;
    private final String accountName;
    private final String tableUrl;

    private TableAsyncClient(String tableName, AzureTableImpl implementation, SerializerAdapter serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
        try {
            if (tableName == null || tableName.isEmpty()) {
                throw new IllegalArgumentException("'tableName' must be provided to create a TableClient");
            }
            final URI uri = URI.create(implementation.getUrl());
            this.accountName = uri.getHost().split("\\.", 2)[0];
            this.tableUrl = uri.resolve("/" + tableName).toString();
            logger.verbose("Table Service URI: {}", uri);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.implementation = implementation;
        this.tableName = tableName;
    }

    TableAsyncClient(String tableName, HttpPipeline pipeline, String serviceUrl, TablesServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {
        this(tableName, new AzureTableImplBuilder()
            .url(serviceUrl)
            .serializerAdapter(serializerAdapter)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient(),
            serializerAdapter
        );
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the name of the account containing the table.
     *
     * @return The name of the account containing the table.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the absolute URL for this table.
     *
     * @return The absolute URL for this table.
     */
    public String getTableUrl() {
        return tableUrl;
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TablesServiceVersion getApiVersion() {
        return TablesServiceVersion.fromString(implementation.getVersion());
    }

    /**
     * Creates a new {@link TableAsyncBatch} object. Batch objects allow you to enqueue multiple create, update, upsert,
     * and/or delete operations on entities that share the same partition key. When the batch is executed, all of the
     * operations will be performed as part of a single transaction. As a result, either all operations in the batch
     * will succeed, or if a failure occurs, all operations in the batch will be rolled back. Each operation in a batch
     * must operate on a distinct row key. Attempting to add multiple operations to a batch that share the same row key
     * will cause an exception to be thrown.
     *
     * @param partitionKey The partition key shared by all operations in the batch.
     *
     * @return An object representing the batch, to which operations can be added.
     * @throws IllegalArgumentException if the provided partition key is {@code null} or empty.
     */
    public TableAsyncBatch createBatch(String partitionKey) {
        if (CoreUtils.isNullOrEmpty(partitionKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The partition key must not be null or empty."));
        }
        return new TableAsyncBatch(partitionKey, this);
    }

    AzureTableImpl getImplementation() {
        return implementation;
    }

    /**
     * Creates the table within the Tables service.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> create() {
        return createWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates the table within the Tables service.
     *
     * @return A reactive result containing the HTTP response.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse() {
        return withContext(context -> createWithResponse(context));
    }

    Mono<Response<Void>> createWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return implementation.getTables().createWithResponseAsync(properties, null,
                ResponseFormat.RETURN_NO_CONTENT, null, context)
                .map(response -> new SimpleResponse<>(response, null));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if an entity with the same partition key and row key already exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createEntity(TableEntity entity) {
        return createEntityWithResponse(entity).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an entity into the table.
     *
     * @param entity The entity to insert.
     *
     * @return A reactive result containing the HTTP response.
     * @throws TableServiceErrorException if an entity with the same partition key and row key already exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createEntityWithResponse(TableEntity entity) {
        return withContext(context -> createEntityWithResponse(entity, null, context));
    }

    Mono<Response<Void>> createEntityWithResponse(TableEntity entity, Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        EntityHelper.setPropertiesFromGetters(entity, logger);
        return implementation.getTables().insertEntityWithResponseAsync(tableName, timeoutInt, null,
            ResponseFormat.RETURN_NO_CONTENT, entity.getProperties(),
            null, context).map(response -> {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
            });
    }

    /**
     * Inserts an entity into the table if it does not exist, or merges the entity with the existing entity otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the provided entity's properties will be merged into the existing entity.
     *
     * @param entity The entity to upsert.
     *
     * @return An empty reactive result.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(TableEntity entity) {
        return upsertEntityWithResponse(entity, null).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     *
     * @return An empty reactive result.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(TableEntity entity, UpdateMode updateMode) {
        return upsertEntityWithResponse(entity, updateMode).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will
     * be inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     *
     * @return A reactive result containing the HTTP response.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, null, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode, Duration timeout,
                                                  Context context) {
        context = context == null ? Context.NONE : context;
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        EntityHelper.setPropertiesFromGetters(entity, logger);
        if (updateMode == UpdateMode.REPLACE) {
            return implementation.getTables().updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, null, entity.getProperties(), null, context).map(response ->
                new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        } else {
            return implementation.getTables().mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, null, entity.getProperties(), null, context).map(response ->
                new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        }
    }

    /**
     * Updates an existing entity by merging the provided entity with the existing entity.
     *
     * @param entity The entity to update.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity) {
        return updateEntity(entity, null);
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity, UpdateMode updateMode) {
        return updateEntity(entity, updateMode, false);
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the eTag of the provided entity must match the eTag of the entity in the Table
     *                    service. If the values do not match, the update will not occur and an exception will be
     *                    thrown.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table,
     *                                    or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does
     *                                    not match that of the provided entity.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
        return updateEntityWithResponse(entity, updateMode, ifUnchanged).flatMap(response ->
            Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Updates an existing entity using the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the eTag of the provided entity must match the eTag of the entity in the Table
     *                    service. If the values do not match, the update will not occur and an exception will be
     *                    thrown.
     *
     * @return A reactive result containing the HTTP response.
     * @throws TableServiceErrorException if no entity with the same partition key and row key exists within the table,
     *                                    or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does
     *                                    not match that of the provided entity.
     * @throws IllegalArgumentException if the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, UpdateMode updateMode,
                                                         boolean ifUnchanged) {
        return withContext(context -> updateEntityWithResponse(entity, updateMode, ifUnchanged, null, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged,
                                                  Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        String eTag = ifUnchanged ? entity.getETag() : "*";
        EntityHelper.setPropertiesFromGetters(entity, logger);
        if (updateMode == UpdateMode.REPLACE) {
            return implementation.getTables().updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, eTag, entity.getProperties(), null, context).map(response ->
                new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        } else {
            return implementation.getTables().mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, eTag, entity.getProperties(), null, context).map(response ->
                new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        }
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no table with this name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @return A reactive result containing the response.
     * @throws TableServiceErrorException if no table with this name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(context -> deleteWithResponse(context));
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return implementation.getTables().deleteWithResponseAsync(tableName, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey) {
        return deleteEntity(partitionKey, rowKey, null);
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     *             the delete will not occur and an exception will be thrown.
     *
     * @return An empty reactive result.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if {@code eTag} is not {@code null} and the existing entity's eTag
     *                                    does not match that of the provided entity.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey, String eTag) {
        return deleteEntityWithResponse(partitionKey, rowKey, eTag).then();
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
     * @param eTag The value to compare with the eTag of the entity in the Tables service. If the values do not match,
     *             the delete will not occur and an exception will be thrown.
     *
     * @return A reactive result containing the response.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table, or if {@code eTag} is not {@code null} and the existing entity's eTag
     *                                    does not match that of the provided entity.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag) {
        return withContext(context -> deleteEntityWithResponse(partitionKey, rowKey, eTag, null, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, Duration timeout,
                                                  Context context) {
        context = context == null ? Context.NONE : context;
        String matchParam = eTag == null ? "*" : eTag;
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        context = context == null ? Context.NONE : context;
        return implementation.getTables().deleteEntityWithResponseAsync(tableName, partitionKey, rowKey, matchParam,
            timeoutInt, null, null, context).map(response -> {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
            });
    }

    /**
     * Lists all entities within the table.
     *
     * @return A paged reactive result containing all entities within the table.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities() {
        return listEntities(new ListEntitiesOptions());
    }

    /**
     * Lists entities using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only entities matching the filter will be returned. If the
     * `select` parameter is set, only the properties included in the select parameter will be returned for each entity.
     * If the `top` parameter is set, the number of returned entities will be limited to that value.
     *
     * @param options The `filter`, `select`, and `top` OData query options to apply to this operation.
     *
     * @return A paged reactive result containing matching entities within the table.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listEntitiesFirstPage(context, options, TableEntity.class)),
            token -> withContext(context -> listEntitiesNextPage(token, context, options, TableEntity.class)));
    }

    /**
     * Lists all entities within the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A paged reactive result containing all entities within the table.
     * @throws IllegalArgumentException if an instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedFlux<T> listEntities(Class<T> resultType) {
        return listEntities(new ListEntitiesOptions(), resultType);
    }

    /**
     * Lists entities using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only entities matching the filter will be returned. If the
     * `select` parameter is set, only the properties included in the select parameter will be returned for each entity.
     * If the `top` parameter is set, the number of returned entities will be limited to that value.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param options The `filter`, `select`, and `top` OData query options to apply to this operation.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A paged reactive result containing matching entities within the table.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed, or if
     *                                  an instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T extends TableEntity> PagedFlux<T> listEntities(ListEntitiesOptions options, Class<T> resultType) {
        return new PagedFlux<>(
            () -> withContext(context -> listEntitiesFirstPage(context, options, resultType)),
            token -> withContext(context -> listEntitiesNextPage(token, context, options, resultType)));
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesFirstPage(Context context,
                                                                                 ListEntitiesOptions options,
                                                                                 Class<T> resultType) {
        try {
            return listEntities(null, null, context, options, resultType);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesNextPage(String token, Context context,
                                                                                ListEntitiesOptions options,
                                                                                Class<T> resultType) {
        if (token == null) {
            return Mono.empty();
        }
        try {
            String[] split = token.split(DELIMITER_CONTINUATION_TOKEN, 2);
            if (split.length != 2) {
                return monoError(logger, new RuntimeException(
                    "Split done incorrectly, must have partition and row key: " + token));
            }
            String nextPartitionKey = split[0];
            String nextRowKey = split[1];
            return listEntities(nextPartitionKey, nextRowKey, context, options, resultType);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntities(String nextPartitionKey, String nextRowKey,
                                                                        Context context, ListEntitiesOptions options,
                                                                        Class<T> resultType) {
        context = context == null ? Context.NONE : context;
        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setSelect(options.getSelect())
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        return implementation.getTables().queryEntitiesWithResponseAsync(tableName, null, null,
            nextPartitionKey, nextRowKey, queryOptions, context)
            .flatMap(response -> {
                final TableEntityQueryResponse tablesQueryEntityResponse = response.getValue();
                if (tablesQueryEntityResponse == null) {
                    return Mono.empty();
                }

                final List<Map<String, Object>> entityResponseValue = tablesQueryEntityResponse.getValue();
                if (entityResponseValue == null) {
                    return Mono.empty();
                }

                final List<T> entities = entityResponseValue.stream()
                    .map(ModelHelper::createEntity)
                    .map(e -> EntityHelper.convertToSubclass(e, resultType, logger))
                    .collect(Collectors.toList());

                return Mono.just(new EntityPaged<>(response, entities,
                    response.getDeserializedHeaders().getXMsContinuationNextPartitionKey(),
                    response.getDeserializedHeaders().getXMsContinuationNextRowKey()));

            });
    }

    private static class EntityPaged<T extends TableEntity> implements PagedResponse<T> {
        private final Response<TableEntityQueryResponse> httpResponse;
        private final IterableStream<T> entityStream;
        private final String continuationToken;

        EntityPaged(Response<TableEntityQueryResponse> httpResponse, List<T> entityList,
                    String nextPartitionKey, String nextRowKey) {
            if (nextPartitionKey == null || nextRowKey == null) {
                this.continuationToken = null;
            } else {
                this.continuationToken = String.join(DELIMITER_CONTINUATION_TOKEN, nextPartitionKey, nextRowKey);
            }
            this.httpResponse = httpResponse;
            this.entityStream = IterableStream.of(entityList);
        }

        @Override
        public int getStatusCode() {
            return httpResponse.getStatusCode();
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpResponse.getHeaders();
        }

        @Override
        public HttpRequest getRequest() {
            return httpResponse.getRequest();
        }

        @Override
        public IterableStream<T> getElements() {
            return entityStream;
        }

        @Override
        public String getContinuationToken() {
            return continuationToken;
        }

        @Override
        public void close() {
        }
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     *
     * @return A reactive result containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableEntity> getEntity(String partitionKey, String rowKey) {
        return getEntityWithResponse(partitionKey, rowKey, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     *
     * @return A reactive result containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if the
     *                                  {@code select} OData query option is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableEntity> getEntity(String partitionKey, String rowKey, String select) {
        return getEntityWithResponse(partitionKey, rowKey, select).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A reactive result containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if an
     *                                  instance of the provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> Mono<T> getEntity(String partitionKey, String rowKey, Class<T> resultType) {
        return getEntityWithResponse(partitionKey, rowKey, null, resultType).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A reactive result containing the entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, if the
     *                                  {@code select} OData query option is malformed, or if an instance of the
     *                                  provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> Mono<T> getEntity(String partitionKey, String rowKey, String select,
                                                     Class<T> resultType) {
        return getEntityWithResponse(partitionKey, rowKey, select, resultType).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a single entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     *
     * @return A reactive result containing the response and entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, or if the
     *                                  {@code select} OData query option is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableEntity>> getEntityWithResponse(String partitionKey, String rowKey, String select) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, null,
            context));
    }

    /**
     * Gets a single entity from the table.
     *
     * @param <T> The type of the result value, which must be a subclass of TableEntity.
     * @param partitionKey The partition key of the entity.
     * @param rowKey The partition key of the entity.
     * @param select An OData `select` expression to limit the set of properties included in the returned entity.
     * @param resultType The type of the result value, which must be a subclass of TableEntity.
     *
     * @return A reactive result containing the response and entity.
     * @throws TableServiceErrorException if no entity with the provided partition key and row key exists within the
     *                                    table.
     * @throws IllegalArgumentException if the provided partition key or row key are {@code null} or empty, if the
     *                                  {@code select} OData query option is malformed, or if an instance of the
     *                                  provided {@code resultType} can't be created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T extends TableEntity> Mono<Response<T>> getEntityWithResponse(String partitionKey, String rowKey,
                                                                           String select, Class<T> resultType) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, select, resultType, null, context));
    }

    <T extends TableEntity> Mono<Response<T>> getEntityWithResponse(String partitionKey, String rowKey, String select,
                                                                    Class<T> resultType, Duration timeout,
                                                                    Context context) {
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        if (select != null) {
            queryOptions.setSelect(select);
        }

        return implementation.getTables().queryEntityWithPartitionAndRowKeyWithResponseAsync(tableName, partitionKey,
            rowKey, timeoutInt, null, queryOptions, context)
            .handle((response, sink) -> {
                final Map<String, Object> matchingEntity = response.getValue();

                if (matchingEntity == null || matchingEntity.isEmpty()) {
                    logger.info("There was no matching entity. Table: {}, partition key: {}, row key: {}.",
                        tableName, partitionKey, rowKey);

                    sink.complete();
                    return;
                }

                // Deserialize the first entity.
                final TableEntity entity = ModelHelper.createEntity(matchingEntity);
                sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    EntityHelper.convertToSubclass(entity, resultType, logger)));
            });
    }
}
