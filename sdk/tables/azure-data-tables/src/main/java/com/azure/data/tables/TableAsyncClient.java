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
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.EntityHelper;
import com.azure.data.tables.implementation.TableConstants;
import com.azure.data.tables.implementation.TablesImpl;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.models.Entity;
import com.azure.data.tables.models.QueryParams;
import com.azure.data.tables.models.Table;
import com.azure.data.tables.models.UpdateMode;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableConstants.PARTITION_KEY;
import static com.azure.data.tables.implementation.TableConstants.ROW_KEY;

/**
 * class for the table async client
 */
@ServiceClient(
    builder = TableClientBuilder.class,
    isAsync = true)
public class TableAsyncClient {
    private static final String DELIMITER_CONTINUATION_TOKEN = ";";
    private final ClientLogger logger = new ClientLogger(TableAsyncClient.class);
    private final String tableName;
    private final AzureTableImpl implementation;
    private final TablesImpl tableImplementation;
    private final String accountName;
    private final String tableUrl;
    private final TablesServiceVersion apiVersion;
    private final QueryOptions defaultQueryOptions = new QueryOptions()
        .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

    TableAsyncClient(String tableName, HttpPipeline pipeline, String url, TablesServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {

        try {
            final URI uri = URI.create(url);
            logger.verbose("Table Service URI: {}", uri);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.implementation = new AzureTableImplBuilder()
            .url(url)
            .serializerAdapter(serializerAdapter)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.tableImplementation = implementation.getTables();
        this.tableName = tableName;
        this.accountName = null;
        this.tableUrl = null;
        this.apiVersion = null;

    }

    /**
     * returns the table name associated with the client
     *
     * @return table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * returns the account for this table
     *
     * @return returns the account name
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * returns Url of this service
     *
     * @return Url
     */
    public String getTableUrl() {
        return tableUrl;
    }

    /**
     * returns the version
     *
     * @return the version
     */
    public TablesServiceVersion getApiVersion() {
        return apiVersion;
    }

    /**
     * creates new table with the name of this client
     *
     * @return a table
     */
    public Mono<Table> create() {
        return createWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates a new table with the name of this client
     *
     * @return a table
     */
    public Mono<Response<Table>> createWithResponse() {
        return withContext(context -> createWithResponse(context));
    }

    /**
     * creates a new table with the name of this client
     *
     * @param context the context of the query
     *
     * @return a table
     */
    Mono<Response<Table>> createWithResponse(Context context) {
        return tableImplementation.createWithResponseAsync(new TableProperties().setTableName(tableName), null,
            ResponseFormat.RETURN_CONTENT, null, context).map(response -> {
                Table table = response.getValue() == null ? null : new Table(response.getValue().getTableName());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                table);
            });
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include rowKey and
     * partitionKey
     *
     * @param entity the entity
     *
     * @return the created TableEntity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Entity> createEntity(Entity entity) {
        return createEntityWithResponse(entity).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * insert a TableEntity with the given properties and return that TableEntity. Property map must include rowKey and
     * partitionKey
     *
     * @param entity the entity
     *
     * @return a mono of the response with the TableEntity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Entity>> createEntityWithResponse(Entity entity) {
        return withContext(context -> createEntityWithResponse(entity, context));
    }

    Mono<Response<Entity>> createEntityWithResponse(Entity entity, Context context) {
        return tableImplementation.insertEntityWithResponseAsync(tableName, null, null,
            ResponseFormat.RETURN_CONTENT, entity.getProperties(),
            null, context).map(response -> {

                final Entity createdEntity = deserializeEntity(logger, response.getValue());
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createdEntity);
            });
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param entity entity to upsert
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(Entity entity) {
        return upsertEntityWithResponse(entity, null).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param entity entity to upsert
     * @param updateMode type of upsert
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(Entity entity, UpdateMode updateMode) {
        return upsertEntityWithResponse(entity, updateMode).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * based on Mode it either inserts or merges if exists or inserts or merges if exists
     *
     * @param updateMode type of upsert
     * @param entity entity to upsert
     *
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> upsertEntityWithResponse(Entity entity, UpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, null, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(Entity entity, UpdateMode updateMode, Duration timeout,
        Context context) {
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        if (updateMode == UpdateMode.REPLACE) {
            return tableImplementation.updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, "*",
                entity.getProperties(), null, context).map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
                });
        } else {
            return tableImplementation.mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, "*",
                entity.getProperties(), null, context).map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
                });
        }
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or fails
     * if the entity doesn't exist
     *
     * @param entity the entity to update
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(Entity entity) {
        //TODO: merge or throw an error if it cannot be found
        return Mono.empty();
    }

    /**
     * updates the entity
     *
     * @param entity the entity to update
     * @param updateMode which type of mode to execute
     *
     * @return void
     */
    public Mono<Void> updateEntity(Entity entity, UpdateMode updateMode) {
        return updateEntity(entity, false, updateMode);
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or fails
     * if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(Entity entity, boolean ifUnchanged, UpdateMode updateMode) {
        return updateEntityWithResponse(entity, ifUnchanged, updateMode).flatMap(response ->
            Mono.justOrEmpty(response.getValue()));
    }

    /**
     * if UpdateMode is MERGE, merges or fails if the entity doesn't exist. If UpdateMode is REPLACE replaces or fails
     * if the entity doesn't exist
     *
     * @param updateMode which type of update to execute
     * @param entity the entity to update
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     *
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateEntityWithResponse(Entity entity, boolean ifUnchanged, UpdateMode updateMode) {
        return withContext(context -> updateEntityWithResponse(entity, ifUnchanged, updateMode, null, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(Entity entity, boolean ifUnchanged, UpdateMode updateMode,
        Duration timeout, Context context) {
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (updateMode == null || updateMode == UpdateMode.MERGE) {
            if (ifUnchanged) {
                return tableImplementation.mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), entity.getProperties(), null,
                    context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return tableImplementation.mergeEntityWithResponseAsync(tableName,
                            entity.getPartitionKey(), entity.getRowKey(), timeoutInt, null,
                            "*", entity.getProperties(), null, context);
                    }).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null);
                    });
            }
        } else {
            if (ifUnchanged) {
                return tableImplementation.updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), entity.getProperties(),
                    null, context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return tableImplementation.updateEntityWithResponseAsync(tableName,
                            entity.getPartitionKey(), entity.getRowKey(),
                            timeoutInt, null, "*", entity.getProperties(), null,
                            context);
                    }).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null);
                    });
            }
        }
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(Entity entity) {
        return deleteEntity(entity, false);
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(Entity entity, boolean ifUnchanged) {
        return deleteEntityWithResponse(entity, ifUnchanged).then();
    }

    /**
     * deletes the given entity
     *
     * @param entity entity to delete
     * @param ifUnchanged if the eTag of the entity must match the entity in the service or not
     *
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEntityWithResponse(Entity entity, boolean ifUnchanged) {
        return withContext(context -> deleteEntityWithResponse(entity, ifUnchanged, null, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(Entity entity, boolean ifUnchanged, Duration timeout,
        Context context) {
        String matchParam = ifUnchanged ? entity.getETag() : "*";
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        context = context == null ? Context.NONE : context;
        return tableImplementation.deleteEntityWithResponseAsync(tableName, entity.getPartitionKey(),
            entity.getRowKey(),
            matchParam, timeoutInt, null, null, context).map(response -> {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
            });
    }

    /**
     * Queries and returns entities in the given table using the odata query options
     *
     * @return a paged flux of all the entity which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Entity> listEntities() {
        return listEntities(new QueryParams());
    }

    /**
     * Queries and returns entities in the given table using the odata query options
     *
     * @param queryParams the odata query object
     *
     * @return a paged flux of all the entity which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Entity> listEntities(QueryParams queryParams) {
        return new PagedFlux<>(
            () -> withContext(context -> listFirstPageEntities(context, queryParams)),
            token -> withContext(context -> listNextPageEntities(token, context, queryParams)));
    } //802

    PagedFlux<Entity> listTables(QueryParams queryParams, Context context) {

        return new PagedFlux<>(
            () -> listFirstPageEntities(context, queryParams),
            token -> listNextPageEntities(token, context, queryParams));
    } //802

    private Mono<PagedResponse<Entity>> listFirstPageEntities(Context context, QueryParams queryParams) {
        try {
            return listTables(null, null, context, queryParams);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<Entity>> listNextPageEntities(String token, Context context, QueryParams queryParams) {
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
            return listTables(nextPartitionKey, nextRowKey, context, queryParams);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<Entity>> listTables(String nextPartitionKey, String nextRowKey, Context context,
        QueryParams queryParams) {
        QueryOptions queryOptions = new QueryOptions()
            .setFilter(queryParams.getFilter())
            .setTop(queryParams.getTop())
            .setSelect(queryParams.getSelect())
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

                final List<Entity> entities = entityResponseValue.stream()
                    .map(entityMap -> deserializeEntity(logger, entityMap))
                    .collect(Collectors.toList());

                return Mono.just(new EntityPaged(response, entities,
                    response.getDeserializedHeaders().getXMsContinuationNextPartitionKey(),
                    response.getDeserializedHeaders().getXMsContinuationNextRowKey()));

            });
    } //1836

    private static class EntityPaged implements PagedResponse<Entity> {
        private final Response<TableEntityQueryResponse> httpResponse;
        private final IterableStream<Entity> entityStream;
        private final String continuationToken;

        EntityPaged(Response<TableEntityQueryResponse> httpResponse, List<Entity> entityList, String nextPartitionKey,
            String nextRowKey) {
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
        public IterableStream<Entity> getElements() {
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
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     *
     * @return a mono of the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Entity> getEntity(String partitionKey, String rowKey) {
        return getEntityWithResponse(partitionKey, rowKey).flatMap(response ->
            Mono.justOrEmpty(response.getValue()));
    }

    /**
     * gets the entity which fits the given criteria
     *
     * @param partitionKey the partition key of the entity
     * @param rowKey the row key of the entity
     *
     * @return a mono of the response with the table entity
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Entity>> getEntityWithResponse(String partitionKey, String rowKey) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, defaultQueryOptions, context));
    }

    Mono<Response<Entity>> getEntityWithResponse(String partitionKey, String rowKey, QueryOptions queryOptions,
        Context context) {

        return tableImplementation.queryEntitiesWithPartitionAndRowKeyWithResponseAsync(tableName, partitionKey,
            rowKey, null, null, queryOptions, context)
            .handle((response, sink) -> {
                final TableEntityQueryResponse entityQueryResponse = response.getValue();
                if (entityQueryResponse == null) {
                    logger.info("TableEntityQueryResponse is null. Table: {}, partition key: {}, row key: {}.",
                        tableName, partitionKey, rowKey);

                    sink.complete();
                    return;
                }
                final List<Map<String, Object>> matchingEntities = entityQueryResponse.getValue();
                if (matchingEntities == null || matchingEntities.isEmpty()) {
                    logger.info("There was no matching entity. Table: {}, partition key: {}, row key: {}.",
                        tableName, partitionKey, rowKey);

                    sink.complete();
                    return;
                }

                if (matchingEntities.size() > 1) {
                    logger.warning("There were multiple matching entities. Table: {}, partition key: {}, row key: {}.",
                        tableName, partitionKey, rowKey);
                }

                // Deserialize the first entity.
                // TODO: Potentially update logic to deserialize them all.
                final Entity entity = deserializeEntity(logger, matchingEntities.get(0));
                sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    entity));
            });
    }

    /**
     * Given a Map, creates the corresponding entity.
     *
     * @param properties Properties representing the entity.
     *
     * @return The Entity represented by this map.
     * @throws IllegalArgumentException if the Map is missing a row key or partition key.
     * @throws NullPointerException if 'properties' is null.
     */
    private static Entity deserializeEntity(ClientLogger logger, Map<String, Object> properties) {
        final Object partitionKeyValue = properties.get(PARTITION_KEY);
        if (!(partitionKeyValue instanceof String) || ((String) partitionKeyValue).isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' does not exist in property map or is an empty value.", PARTITION_KEY)));
        }

        final Object rowKeyValue = properties.get(ROW_KEY);
        if (!(rowKeyValue instanceof String) || ((String) rowKeyValue).isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' does not exist in property map or is an empty value.", ROW_KEY)));
        }

        final Entity entity = new Entity((String) partitionKeyValue, (String) rowKeyValue);
        properties.forEach((key, value) -> {
            if (key.equals(TableConstants.ETAG_KEY)) {
                EntityHelper.setETag(entity, String.valueOf(value));
            }

            entity.getProperties().putIfAbsent(key, value);
        });

        return entity;
    }
}
