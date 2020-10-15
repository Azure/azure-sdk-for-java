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
 * Provides an asynchronous service client for accessing the Azure Tables service.
 *
 * The client encapsulates the URL for the Tables service endpoint and the credentials for accessing the storage or
 * CosmosDB table API account. It provides methods to create, delete, and list tables within the account. These methods
 * invoke REST API operations to make the requests and obtain the results that are returned.
 *
 * Instances of this client are obtained by calling the {@link TableServiceClientBuilder#buildAsyncClient()} method on a
 * {@link TableServiceClientBuilder} object.
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
     * Gets the name of the account containing the table.
     *
     * @return The name of the account containing the table.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the absolute URL for the Tables service endpoint.
     *
     * @return The absolute URL for the Tables service endpoint.
     */
    public String getServiceUrl() {
        return implementation.getUrl();
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
     * Gets a {@link TableAsyncClient} instance for the provided table in the account.
     *
     * @param tableName The name of the table.
     * @return A {@link TableAsyncClient} instance for the provided table in the account.
     * @throws NullPointerException if {@code tableName} is {@code null} or empty.
     */
    public TableAsyncClient getTableClient(String tableName) {
        return new TableClientBuilder()
            .pipeline(this.implementation.getHttpPipeline())
            .serviceVersion(this.getApiVersion())
            .endpoint(this.getServiceUrl())
            .tableName(tableName)
            .buildAsyncClient();
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     * @return An empty reactive result.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createTable(String tableName) {
        return createTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     * @return A reactive result containing the HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createTableWithResponse(String tableName) {
        return withContext(context -> createTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> createTableWithResponse(String tableName, Context context) {
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
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     * @return An empty reactive result.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createTableIfNotExists(String tableName) {
        return createTableIfNotExistsWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     * @return A reactive result containing the HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
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
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     * @return An empty reactive result.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if no table with the provided name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTable(String tableName) {
        return deleteTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     * @return A reactive result containing the HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if no table with the provided name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse(String tableName) {
        return withContext(context -> deleteTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> deleteTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        return implementation.getTables().deleteWithResponseAsync(tableName, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Lists all tables within the account.
     *
     * @return A paged reactive result containing all tables within the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables() {
        return listTables(new ListTablesOptions());
    }

    /**
     * Lists tables using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only tables matching the filter will be returned. If the `top`
     * parameter is set, the number of returned tables will be limited to that value.
     *
     * @param options The `filter` and `top` OData query options to apply to this operation.
     *
     * @return A paged reactive result containing matching tables within the account.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables(ListTablesOptions options) {

        return new PagedFlux<>(
            () -> withContext(context -> listTablesFirstPage(context, options)),
            token -> withContext(context -> listTablesNextPage(token, context, options)));
    }

    private Mono<PagedResponse<TableItem>> listTablesFirstPage(Context context, ListTablesOptions options) {
        try {
            return listTables(null, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private Mono<PagedResponse<TableItem>> listTablesNextPage(String token, Context context,
                                                              ListTablesOptions options) {
        try {
            return listTables(token, context, options);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private Mono<PagedResponse<TableItem>> listTables(String nextTableName, Context context,
                                                      ListTablesOptions options) {
        context = context == null ? Context.NONE : context;
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
    }

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
