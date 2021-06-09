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
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.TableAccountSasGenerator;
import com.azure.data.tables.implementation.TableSasUtils;
import com.azure.data.tables.implementation.TableUtils;
import com.azure.data.tables.implementation.models.CorsRule;
import com.azure.data.tables.implementation.models.GeoReplication;
import com.azure.data.tables.implementation.models.Logging;
import com.azure.data.tables.implementation.models.Metrics;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.RetentionPolicy;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableQueryResponse;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceStats;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceCorsRule;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableServiceGeoReplication;
import com.azure.data.tables.models.TableServiceGeoReplicationStatus;
import com.azure.data.tables.models.TableServiceLogging;
import com.azure.data.tables.models.TableServiceMetrics;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceRetentionPolicy;
import com.azure.data.tables.models.TableServiceStatistics;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableUtils.swallowExceptionForStatusCode;

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
public final class TableServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(TableServiceAsyncClient.class);
    private final AzureTableImpl implementation;
    private final String accountName;
    private final HttpPipeline pipeline;

    TableServiceAsyncClient(HttpPipeline pipeline, String url, TableServiceVersion serviceVersion,
        SerializerAdapter serializerAdapter) {

        try {
            final URI uri = URI.create(url);
            this.accountName = uri.getHost().split("\\.", 2)[0];

            logger.verbose("Table Service URI: {}", uri);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.implementation = new AzureTableImplBuilder()
            .serializerAdapter(serializerAdapter)
            .url(url)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.pipeline = implementation.getHttpPipeline();
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
     * Gets the endpoint for the Tables service.
     *
     * @return The endpoint for the Tables service.
     */
    public String getServiceEndpoint() {
        return implementation.getUrl();
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
    AzureTableImpl getImplementation() {
        return this.implementation;
    }

    /**
     * Gets this client's {@link ClientLogger}.
     *
     * @return This client's {@link ClientLogger}.
     */
    ClientLogger getLogger() {
        return this.logger;
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return TableServiceVersion.fromString(implementation.getVersion());
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified
     * {@link TableAccountSasSignatureValues}.
     *
     * <p>Note : The client must be authenticated via {@link AzureNamedKeyCredential}.
     * <p>See {@link TableAccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * @param tableAccountSasSignatureValues {@link TableAccountSasSignatureValues}.
     *
     * @return A {@link String} representing the SAS query parameters.
     *
     * @throws IllegalStateException If this {@link TableClient} is not authenticated with an
     * {@link AzureNamedKeyCredential}.
     */
    public String generateAccountSas(TableAccountSasSignatureValues tableAccountSasSignatureValues) {
        AzureNamedKeyCredential azureNamedKeyCredential = TableSasUtils.extractNamedKeyCredential(getHttpPipeline());

        if (azureNamedKeyCredential == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot generate a SAS token with a client that"
                + " is not authenticated with an AzureNamedKeyCredential."));
        }

        return new TableAccountSasGenerator(tableAccountSasSignatureValues, azureNamedKeyCredential).getSas();
    }

    /**
     * Gets a {@link TableAsyncClient} instance for the provided table in the account.
     *
     * @param tableName The name of the table.
     *
     * @return A {@link TableAsyncClient} instance for the provided table in the account.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    public TableAsyncClient getTableClient(String tableName) {
        return new TableClientBuilder()
            .pipeline(this.implementation.getHttpPipeline())
            .serviceVersion(this.getServiceVersion())
            .endpoint(this.getServiceEndpoint())
            .tableName(tableName)
            .buildAsyncClient();
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     *
     * @return A reactive result containing a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableAsyncClient> createTable(String tableName) {
        return createTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     *
     * @return A reactive result containing the HTTP response and a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableAsyncClient>> createTableWithResponse(String tableName) {
        return withContext(context -> createTableWithResponse(tableName, context));
    }

    Mono<Response<TableAsyncClient>> createTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return implementation.getTables().createWithResponseAsync(properties, null,
                ResponseFormat.RETURN_NO_CONTENT, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, getTableClient(tableName)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     *
     * @return A reactive result containing a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableAsyncClient> createTableIfNotExists(String tableName) {
        return createTableIfNotExistsWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     *
     * @return A reactive result containing the HTTP response and a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableAsyncClient>> createTableIfNotExistsWithResponse(String tableName) {
        return withContext(context -> createTableIfNotExistsWithResponse(tableName, context));
    }

    Mono<Response<TableAsyncClient>> createTableIfNotExistsWithResponse(String tableName, Context context) {
        return createTableWithResponse(tableName, context).onErrorResume(e -> e instanceof TableServiceException
                && ((TableServiceException) e).getResponse() != null
                && ((TableServiceException) e).getResponse().getStatusCode() == 409,
            e -> {
                HttpResponse response = ((TableServiceException) e).getResponse();
                return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), null));
            });
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     *
     * @return An empty reactive result.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTable(String tableName) {
        return deleteTableWithResponse(tableName).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     *
     * @return A reactive result containing the HTTP response.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse(String tableName) {
        return withContext(context -> deleteTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> deleteTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return implementation.getTables().deleteWithResponseAsync(tableName, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(TableServiceException.class, e -> swallowExceptionForStatusCode(404, e, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * If the 'filter' parameter in the options is set, only tables matching the filter will be returned. If the 'top'
     * parameter is set, the number of returned tables will be limited to that value.
     *
     * @param options The 'filter' and 'top' OData query options to apply to this operation.
     *
     * @return A paged reactive result containing matching tables within the account.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables(ListTablesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listTablesFirstPage(context, options)),
            token -> withContext(context -> listTablesNextPage(token, context, options)));
    }

    /**
     * Lists tables using the parameters in the provided options.
     *
     * If the 'filter' parameter in the options is set, only tables matching the filter will be returned. If the 'top'
     * parameter is set, the number of returned tables will be limited to that value.
     *
     * @param options The 'filter' and 'top' OData query options to apply to this operation.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A paged reactive result containing matching tables within the account.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    PagedFlux<TableItem> listTables(ListTablesOptions options, Context context) {
        return new PagedFlux<>(
            () -> listTablesFirstPage(context, options),
            token -> listTablesNextPage(token, context, options));
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

        try {
            return implementation.getTables().queryWithResponseAsync(null, nextTableName, queryOptions, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
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
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

    /**
     * Gets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @return A reactive result containing the {@link TableServiceProperties properties} of an account's Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceProperties> getProperties() {
        return this.getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @return A reactive result containing the HTTP response and the {@link TableServiceProperties properties} of an
     * account's Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableServiceProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<TableServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.implementation.getServices().getPropertiesWithResponseAsync(null, null, context)
                .map(response -> new SimpleResponse<>(response, toTableServiceProperties(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private TableServiceProperties toTableServiceProperties(
        com.azure.data.tables.implementation.models.TableServiceProperties tableServiceProperties) {

        return new TableServiceProperties()
            .setLogging(toTableServiceLogging(tableServiceProperties.getLogging()))
            .setHourMetrics(toTableServiceMetrics(tableServiceProperties.getHourMetrics()))
            .setMinuteMetrics(toTableServiceMetrics(tableServiceProperties.getMinuteMetrics()))
            .setCorsRules(tableServiceProperties.getCors().stream()
                .map(this::toTablesServiceCorsRule)
                .collect(Collectors.toList()));
    }

    private TableServiceLogging toTableServiceLogging(Logging logging) {
        return new TableServiceLogging()
            .setAnalyticsVersion(logging.getVersion())
            .setDeleteLogged(logging.isDelete())
            .setReadLogged(logging.isRead())
            .setWriteLogged(logging.isWrite())
            .setRetentionPolicy(toTableServiceRetentionPolicy(logging.getRetentionPolicy()));
    }

    private TableServiceRetentionPolicy toTableServiceRetentionPolicy(RetentionPolicy retentionPolicy) {
        return new TableServiceRetentionPolicy()
            .setEnabled(retentionPolicy.isEnabled())
            .setDaysToRetain(retentionPolicy.getDays());
    }

    private TableServiceMetrics toTableServiceMetrics(Metrics metrics) {
        return new TableServiceMetrics()
            .setVersion(metrics.getVersion())
            .setEnabled(metrics.isEnabled())
            .setIncludeApis(metrics.isIncludeAPIs())
            .setRetentionPolicy(toTableServiceRetentionPolicy(metrics.getRetentionPolicy()));
    }

    private TableServiceCorsRule toTablesServiceCorsRule(CorsRule corsRule) {
        return new TableServiceCorsRule()
            .setAllowedOrigins(corsRule.getAllowedOrigins())
            .setAllowedMethods(corsRule.getAllowedMethods())
            .setAllowedHeaders(corsRule.getAllowedHeaders())
            .setExposedHeaders(corsRule.getExposedHeaders())
            .setMaxAgeInSeconds(corsRule.getMaxAgeInSeconds());
    }

    /**
     * Sets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     *
     * @return An empty reactive result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setProperties(TableServiceProperties tableServiceProperties) {
        return this.setPropertiesWithResponse(tableServiceProperties).flatMap(FluxUtil::toMono);
    }


    /**
     * Sets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     *
     * @return A reactive result containing the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setPropertiesWithResponse(TableServiceProperties tableServiceProperties) {
        return withContext(context -> this.setPropertiesWithResponse(tableServiceProperties, context));
    }

    Mono<Response<Void>> setPropertiesWithResponse(TableServiceProperties tableServiceProperties, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return
                this.implementation.getServices().setPropertiesWithResponseAsync(
                    toImplTableServiceProperties(tableServiceProperties), null, null, context)
                    .map(response -> new SimpleResponse<>(response, null));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private com.azure.data.tables.implementation.models.TableServiceProperties toImplTableServiceProperties(
        TableServiceProperties tableServiceProperties) {

        return new com.azure.data.tables.implementation.models.TableServiceProperties()
            .setLogging(toLogging(tableServiceProperties.getLogging()))
            .setHourMetrics(toMetrics(tableServiceProperties.getHourMetrics()))
            .setMinuteMetrics(toMetrics(tableServiceProperties.getMinuteMetrics()))
            .setCors(tableServiceProperties.getCorsRules().stream()
                .map(this::toCorsRule)
                .collect(Collectors.toList()));
    }

    private Logging toLogging(TableServiceLogging tableServiceLogging) {
        return new Logging()
            .setVersion(tableServiceLogging.getAnalyticsVersion())
            .setDelete(tableServiceLogging.isDeleteLogged())
            .setRead(tableServiceLogging.isReadLogged())
            .setWrite(tableServiceLogging.isWriteLogged())
            .setRetentionPolicy(toRetentionPolicy(tableServiceLogging.getRetentionPolicy()));
    }

    private RetentionPolicy toRetentionPolicy(TableServiceRetentionPolicy tableServiceRetentionPolicy) {
        return new RetentionPolicy()
            .setEnabled(tableServiceRetentionPolicy.isEnabled())
            .setDays(tableServiceRetentionPolicy.getDaysToRetain());
    }

    private Metrics toMetrics(TableServiceMetrics tableServiceMetrics) {
        return new Metrics()
            .setVersion(tableServiceMetrics.getVersion())
            .setEnabled(tableServiceMetrics.isEnabled())
            .setIncludeAPIs(tableServiceMetrics.isIncludeApis())
            .setRetentionPolicy(toRetentionPolicy(tableServiceMetrics.getTableServiceRetentionPolicy()));
    }

    private CorsRule toCorsRule(TableServiceCorsRule corsRule) {
        return new CorsRule()
            .setAllowedOrigins(corsRule.getAllowedOrigins())
            .setAllowedMethods(corsRule.getAllowedMethods())
            .setAllowedHeaders(corsRule.getAllowedHeaders())
            .setExposedHeaders(corsRule.getExposedHeaders())
            .setMaxAgeInSeconds(corsRule.getMaxAgeInSeconds());
    }

    /**
     * Retrieves statistics related to replication for the Table service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * @return A reactive result containing statistics for the Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceStatistics> getStatistics() {
        return this.getStatisticsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves statistics related to replication for the Table service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * @return A reactive result containing the HTTP response and statistics for the Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableServiceStatistics>> getStatisticsWithResponse() {
        return withContext(this::getStatisticsWithResponse);
    }

    Mono<Response<TableServiceStatistics>> getStatisticsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.implementation.getServices().getStatisticsWithResponseAsync(null, null, context)
                .map(response -> new SimpleResponse<>(response, toTableServiceStatistics(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private TableServiceStatistics toTableServiceStatistics(TableServiceStats tableServiceStats) {
        return new TableServiceStatistics(toTableServiceGeoReplication(tableServiceStats.getGeoReplication()));
    }

    private TableServiceGeoReplication toTableServiceGeoReplication(GeoReplication geoReplication) {
        return new TableServiceGeoReplication(
            TableServiceGeoReplicationStatus.fromString(geoReplication.getStatus().toString()),
            geoReplication.getLastSyncTime());
    }
}
