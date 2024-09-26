// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
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
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceStatistics;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableUtils.applyOptionalTimeout;
import static com.azure.data.tables.implementation.TableUtils.swallowExceptionForStatusCode;

/**
 *
 * Provides an asynchronous service client for accessing the Azure Tables service.
 *
 * <h2>Overview</h2>
 *
 * <p>The client encapsulates the URL for the Tables service endpoint and the credentials for accessing the storage or
 * CosmosDB table API account. It provides methods to create, delete, and list tables within the account. These methods
 * invoke REST API operations to make the requests and obtain the results that are returned.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The building and authenticating of instances of this client are handled by {@link TableServiceClientBuilder} instances. The sample below
 * shows how to authenticate and build a TableServiceAsyncClient using a connection string.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.instantiation.connectionstring -->
 * <pre>
 * TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.instantiation -->
 *
 * <p>See {@link TableServiceClientBuilder} documentation for more information on constructing and authenticating a client.</p>
 *
 * <p>The following code samples show the various ways you can interact with the tables service using this client.</p>
 *
 * <hr/>
 *
 * <h3>Create a Table</h3>
 *
 * <p>The {@link #createTable(String) createTable} method can be used to create a new table within an Azure Storage or Azure Cosmos account.
 * It returns a TableClient for the newly created table.</p>
 *
 * <p>The following sample creates a table with the name "myTable".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.createTable#String -->
 * <pre>
 * tableServiceAsyncClient.createTable&#40;&quot;myTable&quot;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;tableAsyncClient -&gt;
 *         System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableAsyncClient.getTableName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.createTable#String -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}.</em>
 *
 * <hr/>
 *
 * <h3>Delete a Table</h3>
 *
 * <p>The {@link #deleteTable(String) deleteTable} method can be used to delete a table within an Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following sample deletes the table with the name "myTable".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.deleteTable#String -->
 * <pre>
 * tableServiceAsyncClient.deleteTable&#40;&quot;myTable&quot;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;unused -&gt;
 *         System.out.printf&#40;&quot;Table with name '%s' was deleted.&quot;, &quot;myTable&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.deleteTable#String -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Get a {@link TableServiceAsyncClient}</h3>
 *
 * <p>The {@link #getTableClient(String) getTableClient} method can be used to retrieve a {@link TableAsyncClient} for a table within an Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following sample gets a {@link TableServiceAsyncClient} using the table name "myTable".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getTableClient#String -->
 * <pre>
 * TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient&#40;&quot;myTable&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Table with name '%s' was retrieved.&quot;, tableAsyncClient.getTableName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.getTableClient#String -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>List Tables</h3>
 *
 * <p>The {@link #listTables() listTables} method can be used to list all the tables in an Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following samples list the tables in the Table service account.</p>
 *
 * <p>Without filtering, returning all tables:</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.listTables -->
 * <pre>
 * tableServiceAsyncClient.listTables&#40;&#41;.subscribe&#40;tableItem -&gt;
 *     System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.listTables -->
 *
 * <p>With filtering:</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions -->
 * <pre>
 * tableServiceAsyncClient.listTables&#40;new ListTablesOptions&#40;&#41;.setFilter&#40;&quot;TableName eq 'myTable'&quot;&#41;&#41;.
 *     subscribe&#40;tableItem -&gt; System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Get Table Properties</h3>
 *
 * <p>The {@link #getProperties() getProperties} method can be used to get the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin Resource Sharing) rules.
 * This operation is only supported on Azure Storage endpoints.</p>
 *
 * <p>The following sample gets the properties of the Table service account.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getProperties -->
 * <pre>
 * tableServiceAsyncClient.getProperties&#40;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;properties -&gt; System.out.print&#40;&quot;Retrieved service properties successfully.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.getProperties -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Set Table Properties</h3>
 *
 * <p>The {@link #setProperties(TableServiceProperties) setProperties} method can be used to set the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin Resource Sharing) rules.
 * This operation is only supported on Azure Storage endpoints.</p>
 *
 * <p>The following sample sets the properties of the Table service account.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties -->
 * <pre>
 * TableServiceProperties properties = new TableServiceProperties&#40;&#41;
 *     .setHourMetrics&#40;new TableServiceMetrics&#40;&#41;
 *         .setVersion&#40;&quot;1.0&quot;&#41;
 *         .setEnabled&#40;true&#41;&#41;
 *     .setLogging&#40;new TableServiceLogging&#40;&#41;
 *         .setAnalyticsVersion&#40;&quot;1.0&quot;&#41;
 *         .setReadLogged&#40;true&#41;
 *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
 *             .setEnabled&#40;true&#41;
 *             .setDaysToRetain&#40;5&#41;&#41;&#41;;
 *
 * tableServiceAsyncClient.setProperties&#40;properties&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;unused -&gt; System.out.print&#40;&quot;Set service properties successfully.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Get Table Statistics</h3>
 *
 * <p>The {@link #getStatistics() getStatistics} method can be used to retrieve statistics related to replication for the account's Table service. It is only available on the secondary location endpoint when read-access geo-redundant replication is enabled for the account.
 * This operation is only supported on Azure Storage endpoints.</p>
 *
 * <p>The following sample gets the statistics of the Table service account.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getStatistics -->
 * <pre>
 * tableServiceAsyncClient.getStatistics&#40;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;statistics -&gt; System.out.print&#40;&quot;Retrieved service statistics successfully.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableServiceAsyncClient.getStatistics -->
 *
 * <em><strong>Note: </strong>for synchronous sample, refer to {@link TableServiceClient synchronous client}</em>
 *
 * @see TableServiceClientBuilder
 * @see com.azure.data.tables
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
     * Gets a {@link TableAsyncClient} instance for the table in the account with the provided {@code tableName}. The
     * resulting {@link TableAsyncClient} will use the same {@link HttpPipeline pipeline} and
     * {@link TableServiceVersion service version} as this {@link TableServiceAsyncClient}.
     *
     * @param tableName The name of the table.
     *
     * @return A {@link TableAsyncClient} instance for the table in the account with the provided {@code tableName}.
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
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.createTable#String -->
     * <pre>
     * tableServiceAsyncClient.createTable&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;tableAsyncClient -&gt;
     *         System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableAsyncClient.getTableName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.createTable#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link Mono} containing a {@link TableAsyncClient} for the created table.
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
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the {@link Response HTTP response} and the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.createTableWithResponse#String -->
     * <pre>
     * tableServiceAsyncClient.createTableWithResponse&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getTableName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.createTableWithResponse#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains a
     * {@link TableAsyncClient} for the created table.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableAsyncClient>> createTableWithResponse(String tableName) {
        return withContext(context -> createTableWithResponse(tableName, context));
    }

    Mono<Response<TableAsyncClient>> createTableWithResponse(String tableName, Context context) {
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return implementation.getTables()
                .createWithResponseAsync(properties, null, ResponseFormat.RETURN_NO_CONTENT, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, getTableClient(tableName)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table if it does not already exist. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExists#String -->
     * <pre>
     * tableServiceAsyncClient.createTableIfNotExists&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;tableAsyncClient -&gt;
     *         System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableAsyncClient.getTableName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExists#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link Mono} containing a {@link TableAsyncClient} for the created table.
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
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table if it does not already exist. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExistsWithResponse#String -->
     * <pre>
     * tableServiceAsyncClient.createTableIfNotExistsWithResponse&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getTableName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExistsWithResponse#String -->
     *
     * @param tableName The name of the table to create.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains a
     * {@link TableAsyncClient} for the created table.
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
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.deleteTable#String -->
     * <pre>
     * tableServiceAsyncClient.deleteTable&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table with name '%s' was deleted.&quot;, &quot;myTable&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.deleteTable#String -->
     *
     * @param tableName The name of the table to delete.
     *
     * @return An empty {@link Mono}.
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
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.deleteTableWithResponse#String -->
     * <pre>
     * tableServiceAsyncClient.deleteTableWithResponse&#40;&quot;myTable&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was deleted.&quot;,
     *             response.getStatusCode&#40;&#41;, &quot;myTable&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.deleteTableWithResponse#String -->
     *
     * @param tableName The name of the table to delete.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse(String tableName) {
        return withContext(context -> deleteTableWithResponse(tableName, context));
    }

    Mono<Response<Void>> deleteTableWithResponse(String tableName, Context context) {
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
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables. Prints out the details of the retrieved tables.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.listTables -->
     * <pre>
     * tableServiceAsyncClient.listTables&#40;&#41;.subscribe&#40;tableItem -&gt;
     *     System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.listTables -->
     *
     * @return A {@link PagedFlux} containing all tables within the account.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableItem> listTables() {
        return listTables(new ListTablesOptions());
    }

    /**
     * Lists tables using the parameters in the provided options.
     *
     * If the {@code filter} parameter in the options is set, only tables matching the filter will be returned. If the
     * {@code top} parameter is set, the maximum number of returned tables per page will be limited to that value.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables that match the filter. Prints out the details of the retrieved tables.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions -->
     * <pre>
     * tableServiceAsyncClient.listTables&#40;new ListTablesOptions&#40;&#41;.setFilter&#40;&quot;TableName eq 'myTable'&quot;&#41;&#41;.
     *     subscribe&#40;tableItem -&gt; System.out.printf&#40;&quot;Retrieved table with name '%s'.%n&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions -->
     *
     * @param options The {@code filter} and {@code top} OData query options to apply to this operation.
     *
     * @return A {@link PagedFlux} containing matching tables within the account.
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

    PagedFlux<TableItem> listTables(ListTablesOptions options, Context context, Duration timeout) {
        return new PagedFlux<>(
            () -> applyOptionalTimeout(listTablesFirstPage(context, options), timeout),
            token -> applyOptionalTimeout(listTablesNextPage(token, context, options), timeout));
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
                        .map(TableItemAccessHelper::createItem).collect(Collectors.toList());

                    return Mono.just(new TablePaged(response, tables,
                        response.getDeserializedHeaders().getXMsContinuationNextTableName()));

                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the properties of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getProperties -->
     * <pre>
     * tableServiceAsyncClient.getProperties&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;properties -&gt; System.out.print&#40;&quot;Retrieved service properties successfully.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.getProperties -->
     *
     * @return A {@link Mono} containing the {@link TableServiceProperties properties} of the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceProperties> getProperties() {
        return this.getPropertiesWithResponse().flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * tableServiceAsyncClient.getPropertiesWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Retrieved service properties successfully with status code: %d.&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.getPropertiesWithResponse -->
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains the
     * {@link TableServiceProperties properties} of the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableServiceProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<TableServiceProperties>> getPropertiesWithResponse(Context context) {
        try {
            return this.implementation.getServices().getPropertiesWithResponseAsync(null, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, TableUtils.toTableServiceProperties(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Sets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the properties of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties -->
     * <pre>
     * TableServiceProperties properties = new TableServiceProperties&#40;&#41;
     *     .setHourMetrics&#40;new TableServiceMetrics&#40;&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setEnabled&#40;true&#41;&#41;
     *     .setLogging&#40;new TableServiceLogging&#40;&#41;
     *         .setAnalyticsVersion&#40;&quot;1.0&quot;&#41;
     *         .setReadLogged&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;;
     *
     * tableServiceAsyncClient.setProperties&#40;properties&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt; System.out.print&#40;&quot;Set service properties successfully.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties -->
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     *
     * @return An empty {@link Mono}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setProperties(TableServiceProperties tableServiceProperties) {
        return this.setPropertiesWithResponse(tableServiceProperties).flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.setPropertiesWithResponse#TableServiceProperties -->
     * <pre>
     * TableServiceProperties myProperties = new TableServiceProperties&#40;&#41;
     *     .setHourMetrics&#40;new TableServiceMetrics&#40;&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setEnabled&#40;true&#41;&#41;
     *     .setLogging&#40;new TableServiceLogging&#40;&#41;
     *         .setAnalyticsVersion&#40;&quot;1.0&quot;&#41;
     *         .setReadLogged&#40;true&#41;
     *         .setRetentionPolicy&#40;new TableServiceRetentionPolicy&#40;&#41;
     *             .setEnabled&#40;true&#41;
     *             .setDaysToRetain&#40;5&#41;&#41;&#41;;
     *
     * tableServiceAsyncClient.setPropertiesWithResponse&#40;myProperties&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Retrieved service properties successfully with status code: %d.&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.setPropertiesWithResponse#TableServiceProperties -->
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setPropertiesWithResponse(TableServiceProperties tableServiceProperties) {
        return withContext(context -> this.setPropertiesWithResponse(tableServiceProperties, context));
    }

    Mono<Response<Void>> setPropertiesWithResponse(TableServiceProperties tableServiceProperties, Context context) {
        try {
            return
                this.implementation.getServices()
                    .setPropertiesWithResponseAsync(TableUtils.toImplTableServiceProperties(tableServiceProperties), null, null,
                        context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response -> new SimpleResponse<>(response, null));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }


    /**
     * Retrieves statistics related to replication for the account's Table service. It is only available on the
     * secondary location endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the replication statistics of the account's Table service.</p>
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getStatistics -->
     * <pre>
     * tableServiceAsyncClient.getStatistics&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;statistics -&gt; System.out.print&#40;&quot;Retrieved service statistics successfully.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.getStatistics -->
     *
     * @return A {@link Mono} containing {@link TableServiceStatistics statistics} for the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceStatistics> getStatistics() {
        return this.getStatisticsWithResponse().flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.data.tables.tableServiceAsyncClient.getStatisticsWithResponse -->
     * <pre>
     * tableServiceAsyncClient.getStatisticsWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Retrieved service statistics successfully with status code: %d.&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableServiceAsyncClient.getStatisticsWithResponse -->
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains
     * {@link TableServiceStatistics statistics} for the account's Table service.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableServiceStatistics>> getStatisticsWithResponse() {
        return withContext(this::getStatisticsWithResponse);
    }

    Mono<Response<TableServiceStatistics>> getStatisticsWithResponse(Context context) {
        try {
            return this.implementation.getServices().getStatisticsWithResponseAsync(null, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, TableUtils.toTableServiceStatistics(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

}
