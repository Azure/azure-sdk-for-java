// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.TableAccountSasGenerator;
import com.azure.data.tables.implementation.TableItemAccessHelper;
import com.azure.data.tables.implementation.TablePaged;
import com.azure.data.tables.implementation.TableSasUtils;
import com.azure.data.tables.implementation.TableUtils;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableQueryResponse;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceStats;
import com.azure.data.tables.implementation.models.TablesQueryHeaders;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceStatistics;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Provides a synchronous service client for accessing the Azure Tables service.
 *
 * <p>The client encapsulates the URL for the Tables service endpoint and the credentials for accessing the storage or
 * CosmosDB table API account. It provides methods to create, delete, and list tables within the account. These methods
 * invoke REST API operations to make the requests and obtain the results that are returned.</p>
 *
 * <p>Instances of this client are obtained by calling the {@link TableServiceClientBuilder#buildClient()} method on a
 * {@link TableServiceClientBuilder} object.</p>
 *
 * <p><strong>Samples to construct a sync client</strong></p>
 * <!-- src_embed com.azure.data.tables.tableServiceClient.instantiation -->
 * <pre>
 * TableServiceClient tableServiceClient = new TableServiceClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;myvault.azure.net&#47;&quot;&#41;
 *     .credential&#40;new AzureNamedKeyCredential&#40;&quot;name&quot;, &quot;key&quot;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceClient.instantiation -->
 *
 * @see TableServiceClientBuilder
 */
@ServiceClient(builder = TableServiceClientBuilder.class)
public final class TableServiceClient {

    private static final ExecutorService THREAD_POOL = TableUtils.getThreadPoolWithShutdownHook();
    private final ClientLogger logger = new ClientLogger(TableServiceClient.class);
    private final AzureTableImpl implementation;
    private final String accountName;
    private final HttpPipeline pipeline;

    TableServiceClient(HttpPipeline pipeline, String url, TableServiceVersion serviceVersion,
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
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return TableServiceVersion.fromString(implementation.getVersion());
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
     * Generates an account SAS for the Azure Storage account using the specified
     * {@link TableAccountSasSignatureValues}.
     *
     * <p><strong>Note:</strong> The client must be authenticated via {@link AzureNamedKeyCredential}.</p>
     * <p>See {@link TableAccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * @param tableAccountSasSignatureValues {@link TableAccountSasSignatureValues}.
     *
     * @return A {@code String} representing the SAS query parameters.
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
     * Gets a {@link TableClient} instance for the table in the account with the provided {@code tableName}. The
     * resulting {@link TableClient} will use the same {@link HttpPipeline pipeline} and
     * {@link TableServiceVersion service version} as this {@link TableServiceClient}.
     *
     * @param tableName The name of the table.
     *
     * @return A {@link TableClient} instance for the table in the account with the provided {@code tableName}.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    public TableClient getTableClient(String tableName) {
        return new TableClientBuilder()
            .pipeline(this.implementation.getHttpPipeline())
            .serviceVersion(this.getServiceVersion())
            .endpoint(this.getServiceEndpoint())
            .tableName(tableName)
            .buildClient();

    }

    /**
     * Creates a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.createTable#String -->
     * <pre>
     * TableClient tableClient = tableServiceClient.createTable&#40;&quot;myTable&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableClient.getTableName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.createTable#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableClient createTable(String tableName) {
        return createTableWithResponse(tableName, null, null).getValue();
    }

    /**
     * Creates a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the {@link Response HTTP response} and the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.createTableWithResponse#String-Duration-Context -->
     * <pre>
     * Response&lt;TableClient&gt; response = tableServiceClient.createTableWithResponse&#40;&quot;myTable&quot;, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getTableName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.createTableWithResponse#String-Duration-Context -->
     *
     * @param tableName The name of the table to create.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} containing a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableClient> createTableWithResponse(String tableName, Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<TableClient>> callable = () -> createTableWithResponse(tableName, context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(ex));
        }
    }

    Response<TableClient> createTableWithResponse(String tableName, Context context) {
        context = TableUtils.setContext(context, true);
        final TableProperties properties = new TableProperties().setTableName(tableName);

        return new SimpleResponse<>(implementation.getTables()
            .createWithResponse(properties, null, ResponseFormat.RETURN_NO_CONTENT, null, context),
            getTableClient(tableName));
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table if it does not already exist. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.createTableIfNotExists#String -->
     * <pre>
     * TableClient tableClient = tableServiceClient.createTableIfNotExists&#40;&quot;myTable&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableClient.getTableName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.createTableIfNotExists#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableClient createTableIfNotExists(String tableName) {
        return createTableIfNotExistsWithResponse(tableName, null, null).getValue();
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table if it does not already exist. Prints out the details of the {@link Response HTTP response}
     * and the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.createTableIfNotExistsWithResponse#String-Duration-Context -->
     * <pre>
     * Response&lt;TableClient&gt; response =
     *     tableServiceClient.createTableIfNotExistsWithResponse&#40;&quot;myTable&quot;, Duration.ofSeconds&#40;5&#41;,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getTableName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.createTableIfNotExistsWithResponse#String-Duration-Context -->
     *
     * @param tableName The name of the table to create.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} containing a {@link TableClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableClient> createTableIfNotExistsWithResponse(String tableName, Duration timeout,
                                                                    Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<TableClient>> callable = () -> createTableIfNotExistsWithResponse(tableName, context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception e) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(e));
        }
    }

    Response<TableClient> createTableIfNotExistsWithResponse(String tableName, Context context) throws Exception {
        try {
            return createTableWithResponse(tableName, null, null);
        } catch (Exception e) {
            if (e instanceof TableServiceException
                && ((TableServiceException) e).getResponse() != null
                && ((TableServiceException) e).getResponse().getStatusCode() == 409) {
                HttpResponse response = ((TableServiceException) e).getResponse();
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), null);
            }

            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Deletes a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.deleteTable#String -->
     * <pre>
     * String tableName = &quot;myTable&quot;;
     *
     * tableServiceClient.deleteTable&#40;tableName&#41;;
     *
     * System.out.printf&#40;&quot;Table with name '%s' was deleted.&quot;, tableName&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.deleteTable#String -->
     *
     * @param tableName The name of the table to delete.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable(String tableName) {
        deleteTableWithResponse(tableName, null, null);
    }

    /**
     * Deletes a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table. Prints out the details of the {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.deleteTableWithResponse#String-Duration-Context -->
     * <pre>
     * String myTableName = &quot;myTable&quot;;
     *
     * Response&lt;Void&gt; response = tableServiceClient.deleteTableWithResponse&#40;myTableName, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was deleted.&quot;,
     *     response.getStatusCode&#40;&#41;, myTableName&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.deleteTableWithResponse#String-Duration-Context -->
     *
     * @param tableName The name of the table to delete.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTableWithResponse(String tableName, Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<Void>> callable = () -> deleteTableWithResponse(tableName, context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception e) {
            Exception exception = (Exception) TableUtils.mapThrowableToTableServiceException(e);
            if (exception instanceof TableServiceException
                && ((TableServiceException) exception).getResponse().getStatusCode() == 404) {
                HttpResponse httpResponse = ((TableServiceException) exception).getResponse();
                return new SimpleResponse<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                    httpResponse.getHeaders(), null);
            }

            throw logger.logExceptionAsError(new RuntimeException(exception));
        }
    }

    Response<Void> deleteTableWithResponse(String tableName, Context context) {
        context = TableUtils.setContext(context, true);
        return new SimpleResponse<>(
            implementation.getTables().deleteWithResponse(tableName, null, context), null);
    }

    /**
     * Lists all tables within the account.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables. Prints out the details of the retrieved tables.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.listTables -->
     * <pre>
     * PagedIterable&lt;TableItem&gt; tableItems = tableServiceClient.listTables&#40;&#41;;
     *
     * tableItems.forEach&#40;tableItem -&gt;
     *     System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.listTables -->
     *
     * @return A {@link PagedIterable} containing all tables within the account.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableItem> listTables() {
        return listTables(new ListTablesOptions(), null, null);
    }

    /**
     * If the {@code filter} parameter in the options is set, only tables matching the filter will be returned. If the
     * {@code top} parameter is set, the maximum number of returned tables per page will be limited to that value.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables that match the filter. Prints out the details of the retrieved tables.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.listTables#ListTablesOptions-Duration-Context -->
     * <pre>
     * ListTablesOptions options = new ListTablesOptions&#40;&#41;.setFilter&#40;&quot;TableName eq 'myTable'&quot;&#41;;
     *
     * PagedIterable&lt;TableItem&gt; retrievedTableItems = tableServiceClient.listTables&#40;options, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * retrievedTableItems.forEach&#40;tableItem -&gt;
     *     System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.listTables#ListTablesOptions-Duration-Context -->
     *
     * @param options The {@code filter} and {@code top} OData query options to apply to this operation.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return A {@link PagedIterable} containing matching tables within the account.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableItem> listTables(ListTablesOptions options, Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<PagedIterable<TableItem>> callable = () -> listTables(options, context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception e) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(e));
        }
    }

    private PagedIterable<TableItem> listTables(ListTablesOptions options, Context context) {
        return new PagedIterable<TableItem>(
            () -> listTablesFirstPage(context, options),
            token -> listTablesNextPage(token, context, options)
        );
    }

    private PagedResponse<TableItem> listTablesFirstPage(Context context, ListTablesOptions options) {
        return listTables(null, context, options);
    }

    private PagedResponse<TableItem> listTablesNextPage(String token, Context context, ListTablesOptions options) {
        return listTables(token, context, options);
    }

    private PagedResponse<TableItem> listTables(String nextTableName, Context context, ListTablesOptions options) {
        context = TableUtils.setContext(context, true);
        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        ResponseBase<TablesQueryHeaders, TableQueryResponse> response =
            implementation.getTables().queryWithResponse(null, nextTableName, queryOptions, context);
        TableQueryResponse tableQueryResponse = response.getValue();

        if (tableQueryResponse == null) {
            return null;
        }

        List<TableResponseProperties> tableResponsePropertiesList = tableQueryResponse.getValue();

        if (tableResponsePropertiesList == null) {
            return null;
        }

        final List<TableItem> tables = tableResponsePropertiesList.stream()
            .map(TableItemAccessHelper::createItem).collect(Collectors.toList());

        return new TablePaged(response, tables, response.getDeserializedHeaders().getXMsContinuationNextTableName());
    }


    /**
     * Gets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the properties of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.getProperties -->
     * <pre>
     * TableServiceProperties properties = tableServiceClient.getProperties&#40;&#41;;
     *
     * System.out.print&#40;&quot;Retrieved service properties successfully.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.getProperties -->
     *
     * @return The {@link TableServiceProperties properties} of the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableServiceProperties getProperties() {
        return getPropertiesWithResponse(null, null).getValue();
    }

    /**
     * Gets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the properties of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.getPropertiesWithResponse#Duration-Context -->
     * <pre>
     * Response&lt;TableServiceProperties&gt; response =
     *     tableServiceClient.getPropertiesWithResponse&#40;Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved service properties successfully with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.getPropertiesWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} and the {@link TableServiceProperties properties} of the account's
     * Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<TableServiceProperties>> callable = () -> getPropertiesWithResponse(context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(ex));
        }
    }

    Response<TableServiceProperties> getPropertiesWithResponse(Context context) {
        context = TableUtils.setContext(context, true);
        Response<com.azure.data.tables.implementation.models.TableServiceProperties> response =
            this.implementation.getServices().getPropertiesWithResponse(null, null, context);
        return new SimpleResponse<>(response, TableUtils.toTableServiceProperties(response.getValue()));
    }

    /**
     * Sets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the properties of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.setProperties#TableServiceProperties -->
     * <pre>
     * TableServiceProperties properties = new TableServiceProperties&#40;&#41;
     *     .setHourMetrics&#40;new TableServiceMetrics&#40;&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;
     *     .setLogging&#40;new TableServiceLogging&#40;&#41;
     *         .setAnalyticsVersion&#40;&quot;1.0&quot;&#41;
     *         .setReadLogged&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;;
     *
     * tableServiceClient.setProperties&#40;properties&#41;;
     *
     * System.out.print&#40;&quot;Set service properties successfully.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.setProperties#TableServiceProperties -->
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setProperties(TableServiceProperties tableServiceProperties) {
        setPropertiesWithResponse(tableServiceProperties, null, null);
    }

    /**
     * Sets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the properties of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.setPropertiesWithResponse#TableServiceProperties-Duration-Context -->
     * <pre>
     * TableServiceProperties myProperties = new TableServiceProperties&#40;&#41;
     *     .setHourMetrics&#40;new TableServiceMetrics&#40;&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;
     *     .setLogging&#40;new TableServiceLogging&#40;&#41;
     *         .setAnalyticsVersion&#40;&quot;1.0&quot;&#41;
     *         .setReadLogged&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;;
     *
     * Response&lt;Void&gt; response = tableServiceClient.setPropertiesWithResponse&#40;myProperties, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved service properties successfully with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.setPropertiesWithResponse#TableServiceProperties-Duration-Context -->
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(TableServiceProperties tableServiceProperties, Duration timeout,
                                                    Context context) {

        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<Void>> callable = () -> setPropertiesWithResponse(tableServiceProperties, context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception e) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(e));
        }
    }

    Response<Void> setPropertiesWithResponse(TableServiceProperties tableServiceProperties, Context context) {
        context = TableUtils.setContext(context, true);
        return new SimpleResponse<>(this.implementation.getServices()
            .setPropertiesWithResponse(TableUtils.toImplTableServiceProperties(tableServiceProperties), null,
                null, context), null);
    }

    /**
     * Retrieves statistics related to replication for the account's Table service. It is only available on the
     * secondary location endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the replication statistics of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.getStatistics -->
     * <pre>
     * TableServiceStatistics statistics = tableServiceClient.getStatistics&#40;&#41;;
     *
     * System.out.print&#40;&quot;Retrieved service statistics successfully.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.getStatistics -->
     *
     * @return {@link TableServiceStatistics Statistics} for the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableServiceStatistics getStatistics() {
        return getStatisticsWithResponse(null, null).getValue();
    }

    /**
     * Retrieves statistics related to replication for the account's Table service. It is only available on the
     * secondary location endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the replication statistics of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceClient.getStatisticsWithResponse#Duration-Context -->
     * <pre>
     * Response&lt;TableServiceStatistics&gt; response = tableServiceClient.getStatisticsWithResponse&#40;Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved service statistics successfully with status code: %d.&quot;,
     *     response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceClient.getStatisticsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return An {@link Response HTTP response} containing {@link TableServiceStatistics statistics} for the
     * account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<TableServiceStatistics>> callable = () -> getStatisticsWithResponse(context);
        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception e) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(e));
        }
    }


    Response<TableServiceStatistics> getStatisticsWithResponse(Context context) {
        context = TableUtils.setContext(context, true);
        Response<TableServiceStats> response = this.implementation.getServices().getStatisticsWithResponse(
            null, null, context);
        return new SimpleResponse<>(response, TableUtils.toTableServiceStatistics(response.getValue()));
    }

}
