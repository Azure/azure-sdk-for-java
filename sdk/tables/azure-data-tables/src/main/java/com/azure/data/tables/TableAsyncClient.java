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
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.implementation.models.TableProperties;
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
    private final String accountName;
    private final String tableUrl;
    private final QueryOptions defaultQueryOptions = new QueryOptions()
        .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

    TableAsyncClient(String tableName, AzureTableImpl implementation) {
        try {
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

    TableAsyncClient(String tableName, HttpPipeline pipeline, String url, TablesServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {
        this(tableName, new AzureTableImplBuilder()
            .url(url)
            .serializerAdapter(serializerAdapter)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient()
        );
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
     * returns Url of this table
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
        return TablesServiceVersion.valueOf(implementation.getVersion());
    }

    /**
     * creates new table with the name of this client
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> create() {
        return createWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates a new table with the name of this client
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse() {
        return withContext(context -> createWithResponse(context));
    }

    /**
     * creates a new table with the name of this client
     *
     * @param context the context of the query
     *
     * @return An HTTP response
     */
    Mono<Response<Void>> createWithResponse(Context context) {
        return implementation.getTables().createWithResponseAsync(new TableProperties().setTableName(tableName), null,
            ResponseFormat.RETURN_NO_CONTENT, null, context).map(response -> {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
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
    public Mono<Void> createEntity(TableEntity entity) {
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
    public Mono<Response<Void>> createEntityWithResponse(TableEntity entity) {
        return withContext(context -> createEntityWithResponse(entity, context));
    }

    Mono<Response<Void>> createEntityWithResponse(TableEntity entity, Context context) {
        return implementation.getTables().insertEntityWithResponseAsync(tableName, null, null,
            ResponseFormat.RETURN_NO_CONTENT, entity.getProperties(),
            null, context).map(response -> {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
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
    public Mono<Void> upsertEntity(TableEntity entity, UpdateMode updateMode) {
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
    public Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, null, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, UpdateMode updateMode, Duration timeout,
                                                  Context context) {
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (entity == null) {
            return monoError(logger, new NullPointerException("TableEntity cannot be null"));
        }
        if (updateMode == UpdateMode.REPLACE) {
            return implementation.getTables().updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                entity.getRowKey(), timeoutInt, null, "*",
                entity.getProperties(), null, context).map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null);
                });
        } else {
            return implementation.getTables().mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity, UpdateMode updateMode) {
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
    public Mono<Void> updateEntity(TableEntity entity, boolean ifUnchanged, UpdateMode updateMode) {
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
                                                         UpdateMode updateMode) {
        return withContext(context -> updateEntityWithResponse(entity, ifUnchanged, updateMode, null, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, boolean ifUnchanged, UpdateMode updateMode,
                                                  Duration timeout, Context context) {
        Integer timeoutInt = timeout == null ? null : (int) timeout.getSeconds();
        if (updateMode == null || updateMode == UpdateMode.MERGE) {
            if (ifUnchanged) {
                return implementation.getTables().mergeEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), entity.getProperties(), null,
                    context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return implementation.getTables().mergeEntityWithResponseAsync(tableName,
                            entity.getPartitionKey(), entity.getRowKey(), timeoutInt, null,
                            "*", entity.getProperties(), null, context);
                    }).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null);
                    });
            }
        } else {
            if (ifUnchanged) {
                return implementation.getTables().updateEntityWithResponseAsync(tableName, entity.getPartitionKey(),
                    entity.getRowKey(), timeoutInt, null, entity.getETag(), entity.getProperties(),
                    null, context).map(response -> {
                        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                    });
            } else {
                return getEntity(entity.getPartitionKey(), entity.getRowKey())
                    .flatMap(entityReturned -> {
                        return implementation.getTables().updateEntityWithResponseAsync(tableName,
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
     * deletes the table with the name of this client
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * deletes the table with the name of this client
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(context -> deleteWithResponse(context));
    }

    /**
     * deletes the table with the name of this client
     *
     * @param context the context of the query
     *
     * @return a table
     */
    Mono<Response<Void>> deleteWithResponse(Context context) {
        return implementation.getTables().deleteWithResponseAsync(tableName, null, context).map(response -> {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                null);
        });
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey) {
        return deleteEntity(partitionKey, rowKey, null);
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @param eTag the eTag of the entity, the delete will only occur if this matches the entity in the service
     *
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey, String eTag) {
        return deleteEntityWithResponse(partitionKey, rowKey, eTag).then();
    }

    /**
     * deletes the given entity
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     * @param eTag the eTag of the entity, the delete will only occur if this matches the entity in the service
     *
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag) {
        return withContext(context -> deleteEntityWithResponse(partitionKey, rowKey, eTag, null, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, Duration timeout,
                                                  Context context) {
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
     * Queries and returns all entities in the given table
     *
     * @return a paged flux of all the entities in the table
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities() {
        return listEntities(new ListEntitiesOptions());
    }

    /**
     * Queries and returns entities in the given table using the odata query options
     *
     * @param options the odata query object
     *
     * @return a paged flux of all the entities which fit this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listEntitiesFirstPage(context, options)),
            token -> withContext(context -> listEntitiesNextPage(token, context, options)));
    } //802

    PagedFlux<TableEntity> listEntities(ListEntitiesOptions options, Context context) {

        return new PagedFlux<>(
            () -> listEntitiesFirstPage(context, options),
            token -> listEntitiesNextPage(token, context, options));
    } //802

    private Mono<PagedResponse<TableEntity>> listEntitiesFirstPage(Context context, ListEntitiesOptions options) {
        try {
            return listEntities(null, null, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<TableEntity>> listEntitiesNextPage(String token, Context context,
                                                                  ListEntitiesOptions options) {
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
            return listEntities(nextPartitionKey, nextRowKey, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<TableEntity>> listEntities(String nextPartitionKey, String nextRowKey, Context context,
                                                          ListEntitiesOptions options) {
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

                final List<TableEntity> entities = entityResponseValue.stream()
                    .map(ModelHelper::createEntity)
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

        return implementation.getTables().queryEntitiesWithPartitionAndRowKeyWithResponseAsync(tableName, partitionKey,
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
                final TableEntity entity = ModelHelper.createEntity(matchingEntities.get(0));
                sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    entity));
            });
    }
}
