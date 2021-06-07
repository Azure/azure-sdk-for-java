// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.TableSasGenerator;
import com.azure.data.tables.implementation.TableSasUtils;
import com.azure.data.tables.implementation.TableUtils;
import com.azure.data.tables.implementation.TransactionalBatchImpl;
import com.azure.data.tables.implementation.models.AccessPolicy;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.SignedIdentifier;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceError;
import com.azure.data.tables.implementation.models.TransactionalBatchAction;
import com.azure.data.tables.implementation.models.TransactionalBatchChangeSet;
import com.azure.data.tables.implementation.models.TransactionalBatchRequestBody;
import com.azure.data.tables.implementation.models.TransactionalBatchResponse;
import com.azure.data.tables.implementation.models.TransactionalBatchSubRequest;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicy;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableSignedIdentifier;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionResponse;
import com.azure.data.tables.models.TableTransactionFailedException;
import com.azure.data.tables.models.TableTransactionResult;
import com.azure.data.tables.sas.TableSasSignatureValues;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;
import static com.azure.core.util.FluxUtil.fluxContext;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableUtils.swallowExceptionForStatusCode;
import static com.azure.data.tables.implementation.TableUtils.toTableServiceError;

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
    private final AzureTableImpl tablesImplementation;
    private final TransactionalBatchImpl transactionalBatchImplementation;
    private final String accountName;
    private final String tableEndpoint;
    private final HttpPipeline pipeline;
    private final TableAsyncClient transactionalBatchClient;

    TableAsyncClient(String tableName, HttpPipeline pipeline, String serviceUrl, TableServiceVersion serviceVersion,
                     SerializerAdapter tablesSerializer, SerializerAdapter transactionalBatchSerializer) {
        try {
            if (tableName == null) {
                throw new NullPointerException("'tableName' must not be null to create a TableClient.");
            }

            if (tableName.isEmpty()) {
                throw new IllegalArgumentException("'tableName' must not be empty to create a TableClient.");
            }

            final URI uri = URI.create(serviceUrl);
            this.accountName = uri.getHost().split("\\.", 2)[0];
            this.tableEndpoint = uri.resolve("/" + tableName).toString();

            logger.verbose("Table Service URI: {}", uri);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.tablesImplementation = new AzureTableImplBuilder()
            .url(serviceUrl)
            .serializerAdapter(tablesSerializer)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.transactionalBatchImplementation =
            new TransactionalBatchImpl(tablesImplementation, transactionalBatchSerializer);
        this.tableName = tableName;
        this.pipeline = tablesImplementation.getHttpPipeline();
        this.transactionalBatchClient = new TableAsyncClient(this, serviceVersion, tablesSerializer);
    }

    // Create a hollow client to be used for obtaining the body of a transaction operation to submit.
    TableAsyncClient(TableAsyncClient client, ServiceVersion serviceVersion, SerializerAdapter tablesSerializer) {
        this.accountName = client.getAccountName();
        this.tableEndpoint = client.getTableEndpoint();
        this.pipeline = BuilderHelper.buildNullClientPipeline();
        this.tablesImplementation = new AzureTableImplBuilder()
            .url(client.getTablesImplementation().getUrl())
            .serializerAdapter(tablesSerializer)
            .pipeline(this.pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.tableName = client.getTableName();
        // A batch prep client does not need its own batch prep client nor batch implementation.
        this.transactionalBatchImplementation = null;
        this.transactionalBatchClient = null;
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
     * Gets the endpoint for this table.
     *
     * @return The endpoint for this table.
     */
    public String getTableEndpoint() {
        return tableEndpoint;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return This client's {@link HttpPipeline}.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the {@link AzureTableImpl} powering this client.
     *
     * @return This client's {@link AzureTableImpl}.
     */
    AzureTableImpl getTablesImplementation() {
        return tablesImplementation;
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return TableServiceVersion.fromString(tablesImplementation.getVersion());
    }

    /**
     * Generates a service SAS for the table using the specified {@link TableSasSignatureValues}.
     *
     * <p>Note : The client must be authenticated via {@link AzureNamedKeyCredential}
     * <p>See {@link TableSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * @param tableSasSignatureValues {@link TableSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     *
     * @throws IllegalStateException If this {@link TableAsyncClient} is not authenticated with an
     * {@link AzureNamedKeyCredential}.
     */
    public String generateSas(TableSasSignatureValues tableSasSignatureValues) {
        AzureNamedKeyCredential azureNamedKeyCredential = TableSasUtils.extractNamedKeyCredential(getHttpPipeline());

        if (azureNamedKeyCredential == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot generate a SAS token with a client that"
                + " is not authenticated with an AzureNamedKeyCredential."));
        }

        return new TableSasGenerator(tableSasSignatureValues, getTableName(), azureNamedKeyCredential).getSas();
    }

    /**
     * Creates the table within the Tables service.
     *
     * @return A reactive result containing a {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableItem> createTable() {
        return createTableWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates the table within the Tables service.
     *
     * @return A reactive result containing the HTTP response and a {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableItem>> createTableWithResponse() {
        return withContext(this::createTableWithResponse);
    }

    Mono<Response<TableItem>> createTableWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return tablesImplementation.getTables().createWithResponseAsync(properties, null,
                ResponseFormat.RETURN_NO_CONTENT, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response ->
                    new SimpleResponse<>(response,
                        ModelHelper.createItem(new TableResponseProperties().setTableName(tableName))));
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
     *
     * @throws TableServiceException If an entity with the same partition key and row key already exists within the
     * table.
     * @throws IllegalArgumentException If the provided entity is invalid.
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
     *
     * @throws TableServiceException If an entity with the same partition key and row key already exists within the
     * table.
     * @throws IllegalArgumentException If the provided entity is invalid.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createEntityWithResponse(TableEntity entity) {
        return withContext(context -> createEntityWithResponse(entity, context));
    }

    Mono<Response<Void>> createEntityWithResponse(TableEntity entity, Context context) {
        context = context == null ? Context.NONE : context;

        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            return tablesImplementation.getTables().insertEntityWithResponseAsync(tableName, null, null,
                ResponseFormat.RETURN_NO_CONTENT, entity.getProperties(), null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response ->
                    new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Inserts an entity into the table if it does not exist, or merges the entity with the existing entity otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will be
     * inserted. Otherwise, the provided entity's properties will be merged into the existing entity.
     *
     * @param entity The entity to upsert.
     *
     * @return An empty reactive result.
     *
     * @throws IllegalArgumentException If the provided entity is invalid.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(TableEntity entity) {
        return upsertEntityWithResponse(entity, null).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an entity into the table if it does not exist, or updates the existing entity using the specified update
     * mode otherwise.
     *
     * If no entity exists within the table having the same partition key and row key as the provided entity, it will be
     * inserted. Otherwise, the existing entity will be updated according to the specified update mode.
     *
     * When the update mode is 'MERGE', the provided entity's properties will be merged into the existing entity. When
     * the update mode is 'REPLACE', the provided entity's properties will completely replace those in the existing
     * entity.
     *
     * @param entity The entity to upsert.
     * @param updateMode The type of update to perform if the entity already exits.
     *
     * @return A reactive result containing the HTTP response.
     *
     * @throws IllegalArgumentException If the provided entity is invalid.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                  Context context) {
        context = context == null ? Context.NONE : context;

        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables()
                    .updateEntityWithResponseAsync(tableName, entity.getPartitionKey(), entity.getRowKey(), null,
                        null, null, entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            } else {
                return tablesImplementation.getTables()
                    .mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(), entity.getRowKey(), null, null,
                        null, entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates an existing entity by merging the provided entity with the existing entity.
     *
     * @param entity The entity to update.
     *
     * @return An empty reactive result.
     *
     * @throws IllegalArgumentException If the provided entity is invalid.
     * @throws TableServiceException If no entity with the same partition key and row key exists within the table.
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
     *
     * @throws IllegalArgumentException If the provided entity is invalid.
     * @throws TableServiceException If no entity with the same partition key and row key exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity, TableEntityUpdateMode updateMode) {
        return updateEntityWithResponse(entity, updateMode, false)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
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
     * @param ifUnchanged When true, the ETag of the provided entity must match the ETag of the entity in the Table
     * service. If the values do not match, the update will not occur and an exception will be thrown.
     *
     * @return A reactive result containing the HTTP response.
     *
     * @throws IllegalArgumentException If the provided entity is invalid.
     * @throws TableServiceException If no entity with the same partition key and row key exists within the table,
     * or if {@code ifUnchanged} is {@code true} and the existing entity's eTag does not match that of the provided
     * entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                         boolean ifUnchanged) {
        return withContext(context -> updateEntityWithResponse(entity, updateMode, ifUnchanged, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                  boolean ifUnchanged, Context context) {
        context = context == null ? Context.NONE : context;

        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        String eTag = ifUnchanged ? entity.getETag() : "*";
        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables()
                    .updateEntityWithResponseAsync(tableName, entity.getPartitionKey(), entity.getRowKey(), null,
                        null, eTag, entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            } else {
                return tablesImplementation.getTables()
                    .mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(), entity.getRowKey(), null, null,
                        eTag, entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @return An empty reactive result.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTable() {
        return deleteTableWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes the table within the Tables service.
     *
     * @return A reactive result containing the response.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse() {
        return withContext(this::deleteTableWithResponse);
    }

    Mono<Response<Void>> deleteTableWithResponse(Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return tablesImplementation.getTables().deleteWithResponseAsync(tableName, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(TableServiceException.class, e -> swallowExceptionForStatusCode(404, e, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an entity from the table.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
     *
     * @return An empty reactive result.
     *
     * @throws IllegalArgumentException If the provided partition key or row key are {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey) {
        return deleteEntityWithResponse(partitionKey, rowKey, null, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an entity from the table.
     *
     * @param entity The table entity to delete.
     *
     * @return A reactive result containing an HTTP response.
     *
     * @throws IllegalArgumentException If the provided partition key or row key are {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(TableEntity entity) {
        return deleteEntityWithResponse(entity, false).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an entity from the table.
     *
     * @param entity The table entity to delete.
     * @param ifUnchanged When true, the ETag of the provided entity must match the ETag of the entity in the Table
     * service. If the values do not match, the update will not occur and an exception will be thrown.
     *
     * @return A reactive result containing an HTTP response.
     *
     * @throws IllegalArgumentException If the provided partition key or row key are {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged) {
        return withContext(context -> deleteEntityWithResponse(entity.getPartitionKey(), entity.getRowKey(),
            entity.getETag(), ifUnchanged, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, boolean ifUnchanged,
                                                  Context context) {
        context = context == null ? Context.NONE : context;
        eTag = ifUnchanged ? eTag : "*";

        if (isNullOrEmpty(partitionKey) || isNullOrEmpty(rowKey)) {
            return monoError(logger, new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null."));
        }

        try {
            return tablesImplementation.getTables().deleteEntityWithResponseAsync(tableName, partitionKey, rowKey, eTag,
                null, null, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(TableServiceException.class, e -> swallowExceptionForStatusCode(404, e, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all entities within the table.
     *
     * @return A paged reactive result containing all entities within the table.
     *
     * @throws TableServiceException If the request is rejected by the service.
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
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listEntitiesFirstPage(context, options, TableEntity.class)),
            token -> withContext(context -> listEntitiesNextPage(token, context, options, TableEntity.class)));
    }

    /**
     * Lists entities using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only entities matching the filter will be returned. If the
     * `select` parameter is set, only the properties included in the select parameter will be returned for each entity.
     * If the `top` parameter is set, the number of returned entities will be limited to that value.
     *
     * @param options The `filter`, `select`, and `top` OData query options to apply to this operation.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A paged reactive result containing matching entities within the table.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    PagedFlux<TableEntity> listEntities(ListEntitiesOptions options, Context context) {
        return new PagedFlux<>(
            () -> listEntitiesFirstPage(context, options, TableEntity.class),
            token -> listEntitiesNextPage(token, context, options, TableEntity.class));
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesFirstPage(Context context,
                                                                                 ListEntitiesOptions options,
                                                                                 Class<T> resultType) {
        return listEntities(null, null, context, options, resultType);
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesNextPage(String token, Context context,
                                                                                ListEntitiesOptions options,
                                                                                Class<T> resultType) {
        if (token == null) {
            return Mono.empty();
        }

        String[] split = token.split(DELIMITER_CONTINUATION_TOKEN, 2);

        if (split.length != 2) {
            return monoError(logger, new RuntimeException(
                "Split done incorrectly, must have partition and row key: " + token));
        }

        String nextPartitionKey = split[0];
        String nextRowKey = split[1];

        return listEntities(nextPartitionKey, nextRowKey, context, options, resultType);
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntities(String nextPartitionKey, String nextRowKey,
                                                                        Context context, ListEntitiesOptions options,
                                                                        Class<T> resultType) {
        context = context == null ? Context.NONE : context;
        String select = null;

        if (options.getSelect() != null) {
            select = String.join(",", options.getSelect());
        }

        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setSelect(select)
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        try {
            return tablesImplementation.getTables().queryEntitiesWithResponseAsync(tableName, null, null,
                nextPartitionKey, nextRowKey, queryOptions, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
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
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     *
     * @throws IllegalArgumentException If the provided partition key or row key are {@code null} or empty.
     * @throws TableServiceException If no entity with the provided partition key and row key exists within the
     * table.
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
     * @param select A list of properties to select on the entity.
     *
     * @return A reactive result containing the response and entity.
     *
     * @throws IllegalArgumentException If the provided partition key or row key are {@code null} or empty, or if the
     * {@code select} OData query option is malformed.
     * @throws TableServiceException If no entity with the provided partition key and row key exists within the
     * table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableEntity>> getEntityWithResponse(String partitionKey, String rowKey, List<String> select) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, context));
    }

    <T extends TableEntity> Mono<Response<T>> getEntityWithResponse(String partitionKey, String rowKey,
                                                                    List<String> select, Class<T> resultType,
                                                                    Context context) {
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        if (select != null) {
            queryOptions.setSelect(String.join(",", select));
        }

        if (isNullOrEmpty(partitionKey) || isNullOrEmpty(rowKey)) {
            return monoError(logger, new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null."));
        }

        try {
            return tablesImplementation.getTables().queryEntityWithPartitionAndRowKeyWithResponseAsync(tableName,
                partitionKey, rowKey, null, null, queryOptions, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
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
                    sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), EntityHelper.convertToSubclass(entity, resultType, logger)));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves details about any stored access policies specified on the table that may be used with Shared Access
     * Signatures.
     *
     * @return A paged reactive result containing the HTTP response and the table's
     * {@link TableSignedIdentifier access policies}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableSignedIdentifier> listAccessPolicies() {
        return (PagedFlux<TableSignedIdentifier>) fluxContext(this::listAccessPolicies);
    }

    PagedFlux<TableSignedIdentifier> listAccessPolicies(Context context) {
        context = context == null ? Context.NONE : context;

        try {
            Context finalContext = context;
            Function<String, Mono<PagedResponse<TableSignedIdentifier>>> retriever =
                marker ->
                    tablesImplementation.getTables()
                        .getAccessPolicyWithResponseAsync(tableName, null, null, finalContext)
                        .map(response -> new PagedResponseBase<>(response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            response.getValue().stream()
                                .map(this::toTableSignedIdentifier)
                                .collect(Collectors.toList()),
                            null,
                            response.getDeserializedHeaders()));

            return new PagedFlux<>(() -> retriever.apply(null), retriever);
        } catch (RuntimeException e) {
            return pagedFluxError(logger, e);
        }
    }

    private TableSignedIdentifier toTableSignedIdentifier(SignedIdentifier signedIdentifier) {
        return new TableSignedIdentifier()
            .setId(signedIdentifier.getId())
            .setAccessPolicy(toTableAccessPolicy(signedIdentifier.getAccessPolicy()));
    }

    private TableAccessPolicy toTableAccessPolicy(AccessPolicy accessPolicy) {
        return new TableAccessPolicy()
            .setExpiresOn(accessPolicy.getExpiry())
            .setStartsOn(accessPolicy.getStart())
            .setPermissions(accessPolicy.getPermission());
    }

    /**
     * Sets stored access policies for the table that may be used with Shared Access Signatures.
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     *
     * @return An empty reactive result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setAccessPolicies(List<TableSignedIdentifier> tableSignedIdentifiers) {
        return this.setAccessPoliciesWithResponse(tableSignedIdentifiers).flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves details about any stored access policies specified on the table that may be used with Shared Access
     * Signatures.
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     *
     * @return A reactive result containing the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers) {
        return withContext(context -> this.setAccessPoliciesWithResponse(tableSignedIdentifiers, context));
    }

    Mono<Response<Void>> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers,
                                                       Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return tablesImplementation.getTables()
                .setAccessPolicyWithResponseAsync(tableName, null, null,
                    tableSignedIdentifiers.stream().map(this::toSignedIdentifier).collect(Collectors.toList()), context)
                .map(response -> new SimpleResponse<>(response, response.getValue()));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private SignedIdentifier toSignedIdentifier(TableSignedIdentifier tableSignedIdentifier) {
        return new SignedIdentifier()
            .setId(tableSignedIdentifier.getId())
            .setAccessPolicy(toAccessPolicy(tableSignedIdentifier.getAccessPolicy()));
    }

    private AccessPolicy toAccessPolicy(TableAccessPolicy tableAccessPolicy) {
        return new AccessPolicy()
            .setExpiry(tableAccessPolicy.getExpiresOn())
            .setStart(tableAccessPolicy.getStartsOn())
            .setPermission(tableAccessPolicy.getPermissions());
    }

    /**
     * Executes all operations within the list inside a transaction. When the call completes, either all operations in
     * the transaction will succeed, or if a failure occurs, all operations in the transaction will be rolled back.
     * Each operation must operate on a distinct row key. Attempting to pass multiple operations that share the same
     * row key will cause an error.
     *
     * @param transactionActions A list of {@link TableTransactionAction transaction actions} to perform on entities
     * in a table.
     *
     * @return A reactive result containing a list of {@link TableTransactionActionResponse sub-responses} that
     * correspond to each operation in the transaction
     *
     * @throws IllegalStateException If no operations have been added to the list.
     * @throws TableTransactionFailedException if any operation within the transaction fails. See the documentation
     * for the client methods in {@link TableClient} to understand the conditions that may cause a given operation to
     * fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableTransactionResult> submitTransaction(List<TableTransactionAction> transactionActions) {
        return submitTransactionWithResponse(transactionActions)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Executes all operations within the list inside a transaction. When the call completes, either all operations in
     * the transaction will succeed, or if a failure occurs, all operations in the transaction will be rolled back.
     * Each operation must operate on a distinct row key. Attempting to pass multiple operations that share the same
     * row key will cause an error.
     *
     * @param transactionActions A list of {@link TableTransactionAction transaction actions} to perform on entities
     * in a table.
     *
     * @return A reactive result containing the HTTP response produced for the transaction itself. The response's
     * value will contain a list of {@link TableTransactionActionResponse sub-responses} that correspond to each
     * operation in the transaction.
     *
     * @throws IllegalStateException If no operations have been added to the list.
     * @throws TableTransactionFailedException if any operation within the transaction fails. See the documentation
     * for the client methods in {@link TableClient} to understand the conditions that may cause a given operation to
     * fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableTransactionResult>> submitTransactionWithResponse(List<TableTransactionAction> transactionActions) {
        return withContext(context -> submitTransactionWithResponse(transactionActions, context));
    }

    Mono<Response<TableTransactionResult>> submitTransactionWithResponse(List<TableTransactionAction> transactionActions, Context context) {
        Context finalContext = context == null ? Context.NONE : context;

        if (transactionActions.size() == 0) {
            throw logger.logExceptionAsError(
                new IllegalStateException("A transaction must contain at least one operation."));
        }

        final List<TransactionalBatchAction> operations = new ArrayList<>();

        for (TableTransactionAction transactionAction : transactionActions) {
            switch (transactionAction.getActionType()) {
                case CREATE:
                    operations.add(new TransactionalBatchAction.CreateEntity(transactionAction.getEntity()));

                    break;
                case UPSERT_MERGE:
                    operations.add(new TransactionalBatchAction.UpsertEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.MERGE));

                    break;
                case UPSERT_REPLACE:
                    operations.add(new TransactionalBatchAction.UpsertEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.REPLACE));

                    break;
                case UPDATE_MERGE:
                    operations.add(new TransactionalBatchAction.UpdateEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.MERGE, transactionAction.getIfUnchanged()));

                    break;
                case UPDATE_REPLACE:
                    operations.add(new TransactionalBatchAction.UpdateEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.REPLACE, transactionAction.getIfUnchanged()));

                    break;
                case DELETE:
                    operations.add(
                        new TransactionalBatchAction.DeleteEntity(transactionAction.getEntity(),
                            transactionAction.getIfUnchanged()));

                    break;
                default:
                    break;
            }
        }

        return Flux.fromIterable(operations)
            .flatMapSequential(op -> op.prepareRequest(transactionalBatchClient).zipWith(Mono.just(op)))
            .collect(TransactionalBatchRequestBody::new, (body, pair) ->
                body.addChangeOperation(new TransactionalBatchSubRequest(pair.getT2(), pair.getT1())))
            .flatMap(body ->
                transactionalBatchImplementation.submitTransactionalBatchWithRestResponseAsync(body, null,
                    finalContext).zipWith(Mono.just(body)))
            .flatMap(pair -> parseResponse(pair.getT2(), pair.getT1()))
            .map(response ->
                new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    new TableTransactionResult(transactionActions, response.getValue())));
    }

    private Mono<Response<List<TableTransactionActionResponse>>> parseResponse(TransactionalBatchRequestBody requestBody,
                                                                               TransactionalBatchResponse response) {
        TableServiceError error = null;
        String errorMessage = null;
        TransactionalBatchChangeSet changes = null;
        TransactionalBatchAction failedAction = null;
        Integer failedIndex = null;

        if (requestBody.getContents().get(0) instanceof TransactionalBatchChangeSet) {
            changes = (TransactionalBatchChangeSet) requestBody.getContents().get(0);
        }

        for (int i = 0; i < response.getValue().length; i++) {
            TableTransactionActionResponse subResponse = response.getValue()[i];

            // Attempt to attach a sub-request to each batch sub-response
            if (changes != null && changes.getContents().get(i) != null) {
                ModelHelper.updateTableTransactionActionResponse(subResponse,
                    changes.getContents().get(i).getHttpRequest());
            }

            // If one sub-response was an error, we need to throw even though the service responded with 202
            if (subResponse.getStatusCode() >= 400 && error == null && errorMessage == null) {
                if (subResponse.getValue() instanceof TableServiceError) {
                    error = (TableServiceError) subResponse.getValue();

                    // Make a best effort to locate the failed operation and include it in the message
                    if (changes != null && error.getOdataError() != null
                        && error.getOdataError().getMessage() != null
                        && error.getOdataError().getMessage().getValue() != null) {

                        String message = error.getOdataError().getMessage().getValue();

                        try {
                            failedIndex = Integer.parseInt(message.substring(0, message.indexOf(":")));
                            failedAction = changes.getContents().get(failedIndex).getOperation();
                        } catch (NumberFormatException e) {
                            // Unable to parse failed operation from batch error message - this just means
                            // the service did not indicate which request was the one that failed. Since
                            // this is optional, just swallow the exception.
                        }
                    }
                } else if (subResponse.getValue() instanceof String) {
                    errorMessage = "The service returned the following data for the failed operation: "
                        + subResponse.getValue();
                } else {
                    errorMessage =
                        "The service returned the following status code for the failed operation: "
                            + subResponse.getStatusCode();
                }
            }
        }

        if (error != null || errorMessage != null) {
            String message = "An operation within the batch failed, the transaction has been rolled back.";

            if (failedAction != null) {
                message += " The failed operation was: " + failedAction;
            } else if (errorMessage != null) {
                message += " " + errorMessage;
            }

            return monoError(logger,
                new TableTransactionFailedException(message, null, toTableServiceError(error), failedIndex));
        } else {
            return Mono.just(new SimpleResponse<>(response, Arrays.asList(response.getValue())));
        }
    }
}
