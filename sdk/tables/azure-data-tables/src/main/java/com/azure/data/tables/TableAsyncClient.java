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
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableQueryParams;
import com.azure.data.tables.models.AzureTable;
import com.azure.data.tables.models.TableUpdateMode;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableConstants.ETAG;
import static com.azure.data.tables.implementation.TableConstants.ETAG_KEY;
import static com.azure.data.tables.implementation.TableConstants.ODATA_METADATA_KEY;
import static com.azure.data.tables.implementation.TableConstants.PARTITION_KEY;
import static com.azure.data.tables.implementation.TableConstants.ROW_KEY;
import static com.azure.data.tables.implementation.TableConstants.TIMESTAMP;

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
    public Mono<AzureTable> create() {
        return createWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates a new table with the name of this client
     *
     * @return a table
     */
    public Mono<Response<AzureTable>> createWithResponse() {
        return withContext(context -> createWithResponse(context));
    }

    /**
     * creates a new table with the name of this client
     *
     * @param context the context of the query
     *
     * @return a table
     */
    Mono<Response<AzureTable>> createWithResponse(Context context) {
        return tableImplementation.createWithResponseAsync(new TableProperties().setTableName(tableName), null,
            ResponseFormat.RETURN_CONTENT, null, context).map(response -> {
                AzureTable table = response.getValue() == null ? null : new AzureTable(response.getValue()
                    .getTableName());
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
    public Mono<TableEntity> createEntity(TableEntity entity) {
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
    public Mono<Response<TableEntity>> createEntityWithResponse(TableEntity entity) {
        return withContext(context -> createEntityWithResponse(entity, context));
    }

    Mono<Response<TableEntity>> createEntityWithResponse(TableEntity entity, Context context) {
        Map<String, Object> properties = addPropertyTyping(entity.getProperties());
        return tableImplementation.insertEntityWithResponseAsync(tableName, null, null,
            ResponseFormat.RETURN_CONTENT, properties,
            null, context).map(response -> {

                final TableEntity createdEntity = deserializeEntity(logger, response.getValue());
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
    public Mono<Void> upsertEntity(TableEntity entity) {
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
    public Mono<Void> upsertEntity(TableEntity entity, TableUpdateMode updateMode) {
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
    public Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableUpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, null, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableUpdateMode updateMode, Duration timeout,
        Context context) {
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        Map<String, Object> properties = addPropertyTyping(entity.getProperties());
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (updateMode == TableUpdateMode.REPLACE) {
            return tableImplementation.updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, "*",
                properties, null, context).map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
                });
        } else {
            return tableImplementation.mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, "*",
                properties, null, context).map(response -> {
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
    public Mono<Void> updateEntity(TableEntity entity) {
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
    public Mono<Void> updateEntity(TableEntity entity, TableUpdateMode updateMode) {
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
    public Mono<Void> updateEntity(TableEntity entity, boolean ifUnchanged, TableUpdateMode updateMode) {
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
    public Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, boolean ifUnchanged,
        TableUpdateMode updateMode) {
        return withContext(context -> updateEntityWithResponse(entity, ifUnchanged, updateMode, null, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, boolean ifUnchanged, TableUpdateMode updateMode,
        Duration timeout, Context context) {
        Map<String, Object> properties = addPropertyTyping(entity.getProperties());
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (updateMode == null || updateMode == TableUpdateMode.MERGE) {
            if (ifUnchanged) {
                return tableImplementation.mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), properties, null,
                    context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return tableImplementation.mergeEntityWithResponseAsync(tableName,
                            entity.getPartitionKey(), entity.getRowKey(), timeoutInt, null,
                            "*", properties, null, context);
                    }).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null);
                    });
            }
        } else {
            if (ifUnchanged) {
                return tableImplementation.updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), properties,
                    null, context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return tableImplementation.updateEntityWithResponseAsync(tableName,
                            entity.getPartitionKey(), entity.getRowKey(),
                            timeoutInt, null, "*", properties, null,
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
    public Mono<Void> deleteEntity(TableEntity entity) {
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
    public Mono<Void> deleteEntity(TableEntity entity, boolean ifUnchanged) {
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
    public Mono<Response<Void>> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged) {
        return withContext(context -> deleteEntityWithResponse(entity, ifUnchanged, null, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged, Duration timeout,
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
    public PagedFlux<TableEntity> listEntities() {
        return listEntities(new TableQueryParams());
    }

    /**
     * Queries and returns entities in the given table using the odata query options
     *
     * @param queryParams the odata query object
     *
     * @return a paged flux of all the entity which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities(TableQueryParams queryParams) {
        return new PagedFlux<>(
            () -> withContext(context -> listFirstPageEntities(context, queryParams)),
            token -> withContext(context -> listNextPageEntities(token, context, queryParams)));
    } //802

    PagedFlux<TableEntity> listTables(TableQueryParams queryParams, Context context) {

        return new PagedFlux<>(
            () -> listFirstPageEntities(context, queryParams),
            token -> listNextPageEntities(token, context, queryParams));
    } //802

    private Mono<PagedResponse<TableEntity>> listFirstPageEntities(Context context, TableQueryParams queryParams) {
        try {
            return listTables(null, null, context, queryParams);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<TableEntity>> listNextPageEntities(String token, Context context,
        TableQueryParams queryParams) {
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

    private Mono<PagedResponse<TableEntity>> listTables(String nextPartitionKey, String nextRowKey, Context context,
        TableQueryParams queryParams) {
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

                final List<TableEntity> entities = entityResponseValue.stream()
                    .map(entityMap -> deserializeEntity(logger, entityMap))
                    .collect(Collectors.toList());

                return Mono.just(new EntityPaged(response, entities,
                    response.getDeserializedHeaders().getXMsContinuationNextPartitionKey(),
                    response.getDeserializedHeaders().getXMsContinuationNextRowKey()));

            });
    } //1836

    private static class EntityPaged implements PagedResponse<TableEntity> {
        private final Response<TableEntityQueryResponse> httpResponse;
        private final IterableStream<TableEntity> entityStream;
        private final String continuationToken;

        EntityPaged(Response<TableEntityQueryResponse> httpResponse, List<TableEntity> entityList,
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
        public IterableStream<TableEntity> getElements() {
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
    public Mono<TableEntity> getEntity(String partitionKey, String rowKey) {
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
    public Mono<Response<TableEntity>> getEntityWithResponse(String partitionKey, String rowKey) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, defaultQueryOptions, context));
    }

    Mono<Response<TableEntity>> getEntityWithResponse(String partitionKey, String rowKey, QueryOptions queryOptions,
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
                final TableEntity entity = deserializeEntity(logger, matchingEntities.get(0));
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
    private static TableEntity deserializeEntity(ClientLogger logger, Map<String, Object> properties) {
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

        final TableEntity entity = new TableEntity((String) partitionKeyValue, (String) rowKeyValue);
        properties.forEach((key, value) -> {
            if (key.equals(TableConstants.ETAG_KEY)) {
                EntityHelper.setETag(entity, String.valueOf(value));
            }

            entity.getProperties().putIfAbsent(key, value);
        });

        return entity;
    }

    private Map<String, Object> addPropertyTyping(Map<String, Object> properties) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!entry.getKey().equals(PARTITION_KEY) && !entry.getKey().equals(ROW_KEY)
                && !entry.getKey().equals(TIMESTAMP) && !entry.getKey().equals(ETAG)
                && !entry.getKey().equals(ETAG_KEY) && !entry.getKey().equals(ODATA_METADATA_KEY)) {
                String key = entry.getKey().concat(ODataConstants.ODATA_TYPE_SUFFIX);
                String value = getEntityProperty(entry.getValue().getClass()).toString();
                result.put(key, value);
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private EdmType getEntityProperty(final Class<?> type) {

        if (type.equals(byte[].class)) {
            return EdmType.BINARY;
        } else if (type.equals(Byte[].class)) {
            return EdmType.BINARY;
        } else if (type.equals(String.class)) {
            return EdmType.STRING;
        } else if (type.equals(boolean.class)) {
            return EdmType.BOOLEAN;
        } else if (type.equals(Boolean.class)) {
            return EdmType.BOOLEAN;
        } else if (type.equals(Date.class)) {
            return EdmType.DATE_TIME;
        } else if (type.equals(double.class)) {
            return EdmType.DOUBLE;
        } else if (type.equals(Double.class)) {
            return EdmType.DOUBLE;
        } else if (type.equals(UUID.class)) {
            return EdmType.GUID;
        } else if (type.equals(int.class)) {
            return EdmType.INT32;
        } else if (type.equals(Integer.class)) {
            return EdmType.INT32;
        } else if (type.equals(long.class)) {
            return EdmType.INT64;
        } else if (type.equals(Long.class)) {
            return EdmType.INT64;
        } else {
            throw logger.logExceptionAsError(new RuntimeException("unable to parse"));
        }
    }
}
