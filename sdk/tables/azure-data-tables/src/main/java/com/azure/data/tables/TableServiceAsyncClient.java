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
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableQueryResponse;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.QueryParams;
import com.azure.data.tables.models.Table;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * async client for account operations
 */
@ServiceClient(builder = TableServiceClientBuilder.class, isAsync = true)
public class TableServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(TableServiceAsyncClient.class);
    private final AzureTableImpl implementation;

    TableServiceAsyncClient(HttpPipeline pipeline, String url, TablesServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {

        try {
            final URI uri = URI.create(url);
            logger.verbose("Table Service URI: {}", uri);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.implementation = new AzureTableImplBuilder()
            .serializerAdapter(serializerAdapter)
            .url(url)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
    }

    /**
     * retrieves the async table client for the provided table or creates one if it doesn't exist
     *
     * @param tableName the tableName of the table
     * @return associated TableAsyncClient
     */
    public TableAsyncClient getTableAsyncClient(String tableName) {
        return null; //return new TableAsyncClient(implementation.getTables(), tableName);
    }

    /**
     * gets a given table by name
     *
     * @param name the name of the table
     * @return associated azure table object
     */
    public Table getTable(String name) {
        return null; //TODO: idk how to do this one
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param tableName the name of the table to create
     * @return the azure table object for the created table
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Table> createTable(String tableName) {
        return createTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param tableName the name of the table to create
     * @return a response wth the azure table object for the created table
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Table>> createTableWithResponse(String tableName) {
        return withContext(context -> createTableWithResponse(tableName, context));
    }

    Mono<Response<Table>> createTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return implementation.getTables().createWithResponseAsync(properties,
                null,
                ResponseFormat.RETURN_CONTENT, null, context)
                .map(response -> {
                    final Table table = new Table(response.getValue().getTableName());
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), table);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param tableName the name of the table to delete
     * @return mono void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTable(String tableName) {
        return deleteTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param tableName the name of the table to delete
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse(String tableName) {
        return withContext(context -> deleteTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> deleteTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        return implementation.getTables().deleteWithResponseAsync(tableName, null, context).map(response -> {
            return new SimpleResponse<>(response, null);
        });
    }

    /**
     * query all the tables under the storage account and returns the tables that fit the query params
     *
     * @return a flux of the tables that met this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Table> listTables() {
        return listTables(new QueryParams());
    }

    /**
     * query all the tables under the storage account and returns the tables that fit the query params
     *
     * @param queryParams the odata query object
     * @return a flux of the tables that met this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Table> listTables(QueryParams queryParams) {

        return new PagedFlux<>(
            () -> withContext(context -> listTablesFirstPage(context, queryParams)),
            token -> withContext(context -> listTablesNextPage(token, context, queryParams)));
    } //802

    PagedFlux<Table> listTables(QueryParams queryParams, Context context) {

        return new PagedFlux<>(
            () -> listTablesFirstPage(context, queryParams),
            token -> listTablesNextPage(token, context, queryParams));
    } //802

    private Mono<PagedResponse<Table>> listTablesFirstPage(Context context, QueryParams queryParams) {
        try {
            return listTables(null, context, queryParams);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<Table>> listTablesNextPage(String token, Context context, QueryParams queryParams) {
        try {
            return listTables(token, context, queryParams);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<Table>> listTables(String nextTableName, Context context, QueryParams queryParams) {
        QueryOptions queryOptions = new QueryOptions()
            .setFilter(queryParams.getFilter())
            .setTop(queryParams.getTop())
            .setSelect(queryParams.getSelect())
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        return implementation.getTables().queryWithResponseAsync(null, nextTableName, queryOptions, context)
            .flatMap(response -> {
                TableQueryResponse tableQueryResponse = response.getValue();
                if (tableQueryResponse == null) {
                    return Mono.empty();
                }
                List<TableResponseProperties> tableResponsePropertiesList = tableQueryResponse.getValue();
                if (tableResponsePropertiesList == null) {
                    return Mono.empty();
                }
                final List<Table> tables = tableResponsePropertiesList.stream()
                    .map(e -> {
                        Table table = new Table(e.getTableName());
                        return table;
                    }).collect(Collectors.toList());

                return Mono.just(new TablePaged(response, tables,
                    response.getDeserializedHeaders().getXMsContinuationNextTableName()));

            });
    } //1836


    private static class TablePaged implements PagedResponse<Table> {
        private final Response<TableQueryResponse> httpResponse;
        private final IterableStream<Table> tableStream;
        private final String continuationToken;

        TablePaged(Response<TableQueryResponse> httpResponse, List<Table> tableList, String continuationToken) {
            this.httpResponse = httpResponse;
            this.tableStream = IterableStream.of(tableList);
            this.continuationToken = continuationToken;
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
        public IterableStream<Table> getElements() {
            return tableStream;
        }

        @Override
        public String getContinuationToken() {
            return continuationToken;
        }

        @Override
        public void close() {
        }
    }
}
