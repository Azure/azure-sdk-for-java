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
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.TableUtils;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceStatistics;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.data.tables.implementation.TableUtils.blockWithOptionalTimeout;

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
 * {@codesnippet com.azure.data.tables.tableServiceClient.instantiation}
 *
 * @see TableServiceClientBuilder
 */
@ServiceClient(builder = TableServiceClientBuilder.class)
public final class TableServiceClient {
    private final TableServiceAsyncClient client;

    TableServiceClient(TableServiceAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the name of the account containing the table.
     *
     * @return The name of the account containing the table.
     */
    public String getAccountName() {
        return client.getAccountName();
    }

    /**
     * Gets the endpoint for the Tables service.
     *
     * @return The endpoint for the Tables service.
     */
    public String getServiceEndpoint() {
        return client.getServiceEndpoint();
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return client.getServiceVersion();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return This client's {@link HttpPipeline}.
     */
    HttpPipeline getHttpPipeline() {
        return client.getHttpPipeline();
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
        return client.generateAccountSas(tableAccountSasSignatureValues);
    }

    /**
     * Gets a {@link TableClient} instance for the table in the account with the provided {@code tableName}.
     *
     * @param tableName The name of the table.
     *
     * @return A {@link TableClient} instance for the table in the account with the provided {@code tableName}.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    public TableClient getTableClient(String tableName) {
        return new TableClient(client.getTableClient(tableName));
    }

    /**
     * Creates a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the created table.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.createTable#String}
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
     * {@codesnippet com.azure.data.tables.tableServiceClient.createTableWithResponse#String-Duration-Context}
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
        return blockWithOptionalTimeout(createTableWithResponse(tableName, context), timeout);
    }

    Mono<Response<TableClient>> createTableWithResponse(String tableName, Context context) {
        context = context == null ? Context.NONE : context;
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return client.getImplementation().getTables().createWithResponseAsync(properties, null,
                ResponseFormat.RETURN_NO_CONTENT, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, getTableClient(tableName)));
        } catch (RuntimeException ex) {
            return monoError(client.getLogger(), ex);
        }
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table if it does not already exist. Prints out the details of the created table.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.createTableIfNotExists#String}
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
     * {@codesnippet com.azure.data.tables.tableServiceClient.createTableIfNotExistsWithResponse#String-Duration-Context}
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
        return blockWithOptionalTimeout(createTableIfNotExistsWithResponse(tableName, context), timeout);
    }

    Mono<Response<TableClient>> createTableIfNotExistsWithResponse(String tableName, Context context) {
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
     * {@codesnippet com.azure.data.tables.tableServiceClient.deleteTable#String}
     *
     * @param tableName The name of the table to delete.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable(String tableName) {
        client.deleteTable(tableName).block();
    }

    /**
     * Deletes a table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table. Prints out the details of the {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.deleteTableWithResponse#String-Duration-Context}
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
        return blockWithOptionalTimeout(client.deleteTableWithResponse(tableName, context), timeout);
    }

    /**
     * Lists all tables within the account.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables. Prints out the details of the retrieved tables.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.listTables}
     *
     * @return A {@link PagedIterable} containing all tables within the account.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableItem> listTables() {
        return new PagedIterable<>(client.listTables());
    }

    /**
     * If the {@code filter} parameter in the options is set, only tables matching the filter will be returned. If the
     * {@code top} parameter is set, the maximum number of returned tables per page will be limited to that value.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all tables that match the filter. Prints out the details of the retrieved tables.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.listTables#ListTablesOptions-Duration-Context}
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
        return new PagedIterable<>(client.listTables(options, context, timeout));
    }

    /**
     * Gets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the properties of the account's Table service.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.getProperties}
     *
     * @return The {@link TableServiceProperties properties} of the account's Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableServiceProperties getProperties() {
        return client.getProperties().block();
    }

    /**
     * Gets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the properties of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.getPropertiesWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} and the {@link TableServiceProperties properties} of the account's
     * Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.getPropertiesWithResponse(context), timeout);
    }

    /**
     * Sets the properties of the account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the properties of the account's Table service.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.setProperties#TableServiceProperties}
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setProperties(TableServiceProperties tableServiceProperties) {
        client.setProperties(tableServiceProperties).block();
    }

    /**
     * Sets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the properties of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.setPropertiesWithResponse#TableServiceProperties-Duration-Context}
     *
     * @param tableServiceProperties The {@link TableServiceProperties} to set.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(TableServiceProperties tableServiceProperties, Duration timeout,
                                                    Context context) {
        return blockWithOptionalTimeout(client.setPropertiesWithResponse(tableServiceProperties, context), timeout);
    }

    /**
     * Retrieves statistics related to replication for the account's Table service. It is only available on the
     * secondary location endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the replication statistics of the account's Table service.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.getStatistics}
     *
     * @return {@link TableServiceStatistics Statistics} for the account's Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableServiceStatistics getStatistics() {
        return client.getStatistics().block();
    }

    /**
     * Retrieves statistics related to replication for the account's Table service. It is only available on the
     * secondary location endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the replication statistics of the account's Table service. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableServiceClient.getStatisticsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return An {@link Response HTTP response} containing {@link TableServiceStatistics statistics} for the
     * account's Table service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.getStatisticsWithResponse(context), timeout);
    }
}
