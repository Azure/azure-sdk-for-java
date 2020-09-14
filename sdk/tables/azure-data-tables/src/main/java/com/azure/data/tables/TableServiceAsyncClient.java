// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
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
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableQueryResponse;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
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
    private final String accountName;

    TableServiceAsyncClient(HttpPipeline pipeline, String url, TablesServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {

        try {
            final URI uri = URI.create(url);
            this.accountName = uri.getHost().split("\\.", 2)[0];
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
     * returns the account for this service
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
    public String getServiceUrl() {
        return implementation.getUrl();
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
     * retrieves the async table client for the provided table or creates one if it doesn't exist
     *
     * @param tableName the tableName of the table
     * @return associated TableAsyncClient
     */
    public TableAsyncClient getTableClient(String tableName) {
        return new TableAsyncClient(tableName, implementation);
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param tableName the name of the table to create
     * @return mono void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createTable(String tableName) {
        return createTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param tableName the name of the table to create
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createTableWithResponse(String tableName) {
        return withContext(context -> createTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> createTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return implementation.getTables().createWithResponseAsync(properties,
                null,
                ResponseFormat.RETURN_NO_CONTENT, null, context)
                .map(response -> {
                    return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), null);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * creates the table with the given name if it does not exist, otherwise no action is taken.
     *
     * @param tableName the name of the table to create
     * @return mono void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createTableIfNotExists(String tableName) {
        return createTableIfNotExistsWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * creates the table with the given name if it does not exist, otherwise no action is taken.
     *
     * @param tableName the name of the table to create
     * @return a response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createTableIfNotExistsWithResponse(String tableName) {
        return withContext(context -> createTableIfNotExistsWithResponse(tableName, context));
    }

    Mono<Response<Void>> createTableIfNotExistsWithResponse(String tableName, Context context) {
        return createTableWithResponse(tableName, context).onErrorResume(e -> e instanceof TableServiceErrorException
                && ((TableServiceErrorException) e).getResponse() != null
                && ((TableServiceErrorException) e).getResponse().getStatusCode() == 409,
            e -> {
                HttpResponse response = ((TableServiceErrorException) e).getResponse();
                return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
            });
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
     * query all the tables under the storage account
     *
     * @return a flux of the tables under the storage account
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables() {
        return listTables(new ListTablesOptions());
    }

    /**
     * query all the tables under the storage account and return the tables that fit the query options
     *
     * @param options the odata query object
     * @return a flux of the tables that met this criteria
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables(ListTablesOptions options) {

        return new PagedFlux<>(
            () -> withContext(context -> listTablesFirstPage(context, options)),
            token -> withContext(context -> listTablesNextPage(token, context, options)));
    } //802

    PagedFlux<TableItem> listTables(ListTablesOptions options, Context context) {

        return new PagedFlux<>(
            () -> listTablesFirstPage(context, options),
            token -> listTablesNextPage(token, context, options));
    } //802

    private Mono<PagedResponse<TableItem>> listTablesFirstPage(Context context, ListTablesOptions options) {
        try {
            return listTables(null, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<TableItem>> listTablesNextPage(String token, Context context,
                                                              ListTablesOptions options) {
        try {
            return listTables(token, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    } //1459

    private Mono<PagedResponse<TableItem>> listTables(String nextTableName, Context context,
                                                      ListTablesOptions options) {
        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
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
                final List<TableItem> tables = tableResponsePropertiesList.stream()
                    .map(ModelHelper::createItem).collect(Collectors.toList());

                return Mono.just(new TablePaged(response, tables,
                    response.getDeserializedHeaders().getXMsContinuationNextTableName()));

            });
    } //1836


    private static class TablePaged implements PagedResponse<TableItem> {
        private final Response<TableQueryResponse> httpResponse;
        private final IterableStream<TableItem> tableStream;
        private final String continuationToken;

        TablePaged(Response<TableQueryResponse> httpResponse, List<TableItem> tableList, String continuationToken) {
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
        public IterableStream<TableItem> getElements() {
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
