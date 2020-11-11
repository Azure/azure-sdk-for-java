// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;

import java.time.Duration;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * Provides a synchronous service client for accessing the Azure Tables service.
 *
 * The client encapsulates the URL for the Tables service endpoint and the credentials for accessing the storage or
 * CosmosDB table API account. It provides methods to create, delete, and list tables within the account. These methods
 * invoke REST API operations to make the requests and obtain the results that are returned.
 *
 * Instances of this client are obtained by calling the {@link TableServiceClientBuilder#buildClient()} method on a
 * {@link TableServiceClientBuilder} object.
 */
@ServiceClient(builder = TableServiceClientBuilder.class)
public class TableServiceClient {
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
     * Gets the absolute URL for the Tables service endpoint.
     *
     * @return The absolute URL for the Tables service endpoint.
     */
    public String getServiceUrl() {
        return client.getServiceUrl();
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TablesServiceVersion getApiVersion() {
        return client.getApiVersion();
    }

    /**
     * Gets a {@link TableClient} instance for the provided table in the account.
     *
     * @param tableName The name of the table.
     * @return A {@link TableClient} instance for the provided table in the account.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     */
    public TableClient getTableClient(String tableName) {
        return new TableClient(client.getTableClient(tableName));
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createTable(String tableName) {
        client.createTable(tableName).block();
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     * @param timeout Duration to wait for the operation to complete.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createTable(String tableName, Duration timeout) {
        blockWithOptionalTimeout(client.createTable(tableName), timeout);
    }

    /**
     * Creates a table within the Tables service.
     *
     * @param tableName The name of the table to create.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if a table with the same name already exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createTableWithResponse(String tableName, Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.createTableWithResponse(tableName, context), timeout);
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createTableIfNotExists(String tableName) {
        client.createTableIfNotExists(tableName).block();
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     * @param timeout Duration to wait for the operation to complete.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createTableIfNotExists(String tableName, Duration timeout) {
        blockWithOptionalTimeout(client.createTableIfNotExists(tableName), timeout);
    }

    /**
     * Creates a table within the Tables service if the table does not already exist.
     *
     * @param tableName The name of the table to create.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createTableIfNotExistsWithResponse(String tableName, Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.createTableIfNotExistsWithResponse(tableName, context), timeout);
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if no table with the provided name exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable(String tableName) {
        client.deleteTable(tableName).block();
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     * @param timeout Duration to wait for the operation to complete.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if no table with the provided name exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable(String tableName, Duration timeout) {
        blockWithOptionalTimeout(client.deleteTable(tableName), timeout);
    }

    /**
     * Deletes a table within the Tables service.
     *
     * @param tableName The name of the table to delete.
     * @param timeout Duration to wait for the operation to complete.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The HTTP response.
     * @throws IllegalArgumentException if {@code tableName} is {@code null} or empty.
     * @throws TableServiceErrorException if no table with the provided name exists within the service.
     * @throws RuntimeException if the provided timeout expires.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTableWithResponse(String tableName, Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.deleteTableWithResponse(tableName, context), timeout);
    }

    /**
     * Lists all tables within the account.
     *
     * @return A paged iterable containing all tables within the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableItem> listTables() {
        return new PagedIterable<>(client.listTables());
    }

    /**
     * Lists tables using the parameters in the provided options.
     *
     * If the `filter` parameter in the options is set, only tables matching the filter will be returned. If the `top`
     * parameter is set, the number of returned tables will be limited to that value.
     *
     * @param options The `filter` and `top` OData query options to apply to this operation.
     *
     * @return A paged iterable containing matching tables within the account.
     * @throws IllegalArgumentException if one or more of the OData query options in {@code options} is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableItem> listTables(ListTablesOptions options) {
        return new PagedIterable<>(client.listTables(options));
    }

}
