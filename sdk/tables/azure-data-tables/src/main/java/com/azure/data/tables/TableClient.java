// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicies;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableSignedIdentifier;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionResponse;
import com.azure.data.tables.models.TableTransactionFailedException;
import com.azure.data.tables.models.TableTransactionResult;
import com.azure.data.tables.sas.TableSasSignatureValues;

import java.time.Duration;
import java.util.List;

import static com.azure.data.tables.implementation.TableUtils.blockWithOptionalTimeout;

/**
 * Provides a synchronous service client for accessing a table in the Azure Tables service.
 *
 * <p>The client encapsulates the URL for the table within the Tables service endpoint, the name of the table, and the
 * credentials for accessing the storage or CosmosDB table API account. It provides methods to create and delete the
 * table itself, as well as methods to create, upsert, update, delete, list, and get entities within the table. These
 * methods invoke REST API operations to make the requests and obtain the results that are returned.</p>
 *
 * <p>Instances of this client are obtained by calling the {@link TableClientBuilder#buildClient()} method on a
 * {@link TableClientBuilder} object.</p>
 *
 * <p><strong>Samples to construct a sync client</strong></p>
 * {@codesnippet com.azure.data.tables.tableClient.instantiation}
 *
 * @see TableClientBuilder
 */
@ServiceClient(builder = TableClientBuilder.class)
public final class TableClient {
    final TableAsyncClient client;

    TableClient(TableAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return this.client.getTableName();
    }

    /**
     * Gets the name of the account containing the table.
     *
     * @return The name of the account containing the table.
     */
    public String getAccountName() {
        return this.client.getAccountName();
    }

    /**
     * Gets the endpoint for this table.
     *
     * @return The endpoint for this table.
     */
    public String getTableEndpoint() {
        return this.client.getTableEndpoint();
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return this.client.getServiceVersion();
    }

    /**
     * Generates a service SAS for the table using the specified {@link TableSasSignatureValues}.
     *
     * <p><strong>Note:</strong> The client must be authenticated via {@link AzureNamedKeyCredential}.</p>
     * <p>See {@link TableSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * @param tableSasSignatureValues {@link TableSasSignatureValues}.
     *
     * @return A {@code String} representing the SAS query parameters.
     *
     * @throws IllegalStateException If this {@link TableClient} is not authenticated with an
     * {@link AzureNamedKeyCredential}.
     */
    public String generateSas(TableSasSignatureValues tableSasSignatureValues) {
        return client.generateSas(tableSasSignatureValues);
    }

    /**
     * Creates the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the created table.</p>
     * {@codesnippet com.azure.data.tables.tableClient.createTable}
     *
     * @return A {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableItem createTable() {
        return client.createTable().block();
    }

    /**
     * Creates the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the {@link Response HTTP response} and the created table.</p>
     * {@codesnippet com.azure.data.tables.tableClient.createTableWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} containing a {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableItem> createTableWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.createTableWithResponse(context), timeout);
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * {@codesnippet com.azure.data.tables.tableClient.deleteTable}
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable() {
        client.deleteTable().block();
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table. Prints out the details of the {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.deleteTableWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTableWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.deleteTableWithResponse(context), timeout);
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the created
     * {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.createEntity#TableEntity}
     *
     * @param entity The {@link TableEntity entity} to insert.
     *
     * @throws TableServiceException If an {@link TableEntity entity} with the same partition key and row key already
     * exists within the table.
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity entity) {
        client.createEntity(entity).block();
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the
     * {@link Response HTTP response} and the created {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.createEntityWithResponse#TableEntity-Duration-Context}
     *
     * @param entity The {@link TableEntity entity} to insert.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws TableServiceException If an {@link TableEntity entity} with the same partition key and row key already
     * exists within the table.
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createEntityWithResponse(TableEntity entity, Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.createEntityWithResponse(entity, context), timeout);
    }

    /**
     * Inserts an {@link TableEntity entity} into the table if it does not exist, or merges the
     * {@link TableEntity entity} with the existing {@link TableEntity entity} otherwise.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Upserts an {@link TableEntity entity} into the table. Prints out the details of the upserted
     * {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.upsertEntity#TableEntity}
     *
     * @param entity The {@link TableEntity entity} to upsert.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity) {
        client.upsertEntity(entity).block();
    }

    /**
     * Inserts an {@link TableEntity entity} into the table if it does not exist, or updates the existing
     * {@link TableEntity entity} using the specified {@link TableEntityUpdateMode update mode} otherwise. The default
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}.
     *
     * <p>When the {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}, the provided
     * {@link TableEntity entity}'s properties will be merged into the existing {@link TableEntity entity}. When the
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#REPLACE REPLACE}, the provided
     * {@link TableEntity entity}'s properties will completely replace those in the existing {@link TableEntity
     * entity}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Upserts an {@link TableEntity entity} into the table with the specified
     * {@link TableEntityUpdateMode update mode} if said {@link TableEntity entity} already exists. Prints out the
     * details of the {@link Response HTTP response} and the upserted {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode-Duration-Context}
     *
     * @param entity The {@link TableEntity entity} to upsert.
     * @param updateMode The type of update to perform if the {@link TableEntity entity} already exits.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> upsertEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                   Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.upsertEntityWithResponse(entity, updateMode, context), timeout);
    }

    /**
     * Updates an existing {@link TableEntity entity} by merging the provided {@link TableEntity entity} with the
     * existing {@link TableEntity entity}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table. Prints out the details of the updated
     * {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.updateEntity#TableEntity}
     *
     * @param entity The {@link TableEntity entity} to update.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity) {
        client.updateEntity(entity).block();
    }

    /**
     * Updates an existing {@link TableEntity entity} using the specified {@link TableEntityUpdateMode update mode}.
     * The default {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}.
     *
     * <p>When the {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}, the provided
     * {@link TableEntity entity}'s properties will be merged into the existing {@link TableEntity entity}. When the
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#REPLACE REPLACE}, the provided
     * {@link TableEntity entity}'s properties will completely replace those in the existing {@link TableEntity entity}.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table with the specified
     * {@link TableEntityUpdateMode update mode}. Prints out the details of the updated {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.updateEntity#TableEntity-TableEntityUpdateMode}
     *
     * @param entity The {@link TableEntity entity} to update.
     * @param updateMode The type of update to perform.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity, TableEntityUpdateMode updateMode) {
        client.updateEntity(entity, updateMode).block();
    }

    /**
     * Updates an existing {@link TableEntity entity} using the specified {@link TableEntityUpdateMode update mode}.
     * The default {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}.
     *
     * <p>When the {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}, the provided
     * {@link TableEntity entity}'s properties will be merged into the existing {@link TableEntity entity}. When the
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#REPLACE REPLACE}, the provided
     * {@link TableEntity entity}'s properties will completely replace those in the existing {@link TableEntity entity}.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table with the specified {@link TableEntityUpdateMode update
     * mode}
     * if the {@code ETags} on both {@link TableEntity entities} match. Prints out the details of the
     * {@link Response HTTP response} updated {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean-Duration-Context}
     *
     * @param entity The {@link TableEntity entity} to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the ETag of the provided {@link TableEntity entity} must match the ETag of the
     * {@link TableEntity entity} in the Table service. If the values do not match, the update will not occur and an
     * exception will be thrown.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table, or if {@code ifUnchanged} is {@code true} and the existing {@link TableEntity entity}'s ETag
     * does not match that of the provided {@link TableEntity entity}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                   boolean ifUnchanged, Duration timeout, Context context) {
        return blockWithOptionalTimeout(
            client.updateEntityWithResponse(entity, updateMode, ifUnchanged, context), timeout);
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes an {@link TableEntity entity} on the table. Prints out the entity's {@code partitionKey} and
     * {@code rowKey}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.deleteEntity#String-String}
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The row key of the {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey) {
        client.deleteEntity(partitionKey, rowKey).block();
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the deleted
     * {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.deleteEntity#TableEntity}
     *
     * @param entity The {@link TableEntity entity} to delete.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(TableEntity entity) {
        client.deleteEntity(entity).block();
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the
     * {@link Response HTTP response} and the deleted {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.deleteEntityWithResponse#TableEntity-Duration-Context}
     *
     * @param entity The table {@link TableEntity entity} to delete.
     * @param ifUnchanged When true, the ETag of the provided {@link TableEntity entity} must match the ETag of the
     * {@link TableEntity entity} in the Table service. If the values do not match, the update will not occur and an
     * exception will be thrown.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged, Duration timeout,
                                                   Context context) {
        return blockWithOptionalTimeout(client.deleteEntityWithResponse(entity.getPartitionKey(),
            entity.getRowKey(), entity.getETag(), ifUnchanged, context), timeout);
    }

    /**
     * Lists all {@link TableEntity entities} within the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link TableEntity entities} on the table. Prints out the details of the
     * retrieved {@link TableEntity entities}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.listEntities}
     *
     * @return A {@link PagedIterable} containing all {@link TableEntity entities} within the table.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities() {
        return new PagedIterable<>(client.listEntities());
    }

    /**
     * Lists {@link TableEntity entities} using the parameters in the provided options.
     *
     * <p>If the {@code filter} parameter in the options is set, only {@link TableEntity entities} matching the filter
     * will be returned. If the {@code select} parameter is set, only the properties included in the select parameter
     * will be returned for each {@link TableEntity entity}. If the {@code top} parameter is set, the maximum number of
     * returned {@link TableEntity entities} per page will be limited to that value.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link TableEntity entities} on the table. Prints out the details of the
     * {@link Response HTTP response} and all the retrieved {@link TableEntity entities}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.listEntities#ListEntitiesOptions-Duration-Context}
     *
     * @param options The {@code filter}, {@code select}, and {@code top} OData query options to apply to this
     * operation.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return A {@link PagedIterable} containing matching {@link TableEntity entities} within the table.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities(ListEntitiesOptions options, Duration timeout, Context context) {
        return new PagedIterable<>(client.listEntities(options, context, timeout));
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the retrieved
     * {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.getEntity#String-String}
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The partition key of the {@link TableEntity entity}.
     *
     * @return The {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty.
     * @throws TableServiceException If no {@link TableEntity entity} with the provided {@code partitionKey} and
     * {@code rowKey} exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableEntity getEntity(String partitionKey, String rowKey) {
        return client.getEntity(partitionKey, rowKey).block();
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the {@link Response HTTP response}
     * retrieved {@link TableEntity entity}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.getEntityWithResponse#String-String-ListEntitiesOptions-Duration-Context}
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The partition key of the {@link TableEntity entity}.
     * @param select A list of properties to select on the {@link TableEntity entity}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response} containing the {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty, or if the {@code select} OData query option is malformed.
     * @throws TableServiceException If no {@link TableEntity entity} with the provided {@code partitionKey} and
     * {@code rowKey} exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableEntity> getEntityWithResponse(String partitionKey, String rowKey, List<String> select,
                                                       Duration timeout, Context context) {
        return blockWithOptionalTimeout(
            client.getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, context), timeout);
    }

    /**
     * Retrieves details about any stored {@link TableAccessPolicies access policies} specified on the table that may
     * be used with Shared Access Signatures.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a table's {@link TableAccessPolicies access policies}. Prints out the details of the retrieved
     * {@link TableAccessPolicies access policies}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.getAccessPolicies}
     *
     * @return The table's {@link TableAccessPolicies access policies}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableAccessPolicies getAccessPolicies() {
        return client.getAccessPolicies().block();
    }

    /**
     * Retrieves details about any stored {@link TableAccessPolicies access policies} specified on the table that may be
     * used with Shared Access Signatures.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a table's {@link TableAccessPolicies access policies}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link TableAccessPolicies access policies}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.getAccessPoliciesWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return An {@link Response HTTP response} containing the table's {@link TableAccessPolicies access policies}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableAccessPolicies> getAccessPoliciesWithResponse(Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.getAccessPoliciesWithResponse(context), timeout);
    }

    /**
     * Sets stored {@link TableAccessPolicies access policies} for the table that may be used with Shared Access
     * Signatures.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets stored {@link TableAccessPolicies access policies} on a table.</p>
     * {@codesnippet com.azure.data.tables.tableClient.setAccessPolicies#List}
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setAccessPolicies(List<TableSignedIdentifier> tableSignedIdentifiers) {
        client.setAccessPolicies(tableSignedIdentifiers).block();
    }

    /**
     * Sets stored {@link TableAccessPolicies access policies} for the table that may be used with Shared Access
     * Signatures.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets stored {@link TableAccessPolicies access policies} on a table. Prints out details of the
     * {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.setAccessPoliciesWithResponse#List-Duration-Context}
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers,
                                                        Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.setAccessPoliciesWithResponse(tableSignedIdentifiers, context), timeout);
    }

    /**
     * Executes all {@link TableTransactionAction actions} within the list inside a transaction. When the call
     * completes, either all {@link TableTransactionAction actions} in the transaction will succeed, or if a failure
     * occurs, all {@link TableTransactionAction actions} in the transaction will be rolled back.
     * {@link TableTransactionAction Actions} are executed sequantially. Each {@link TableTransactionAction action}
     * must operate on a distinct row key. Attempting to pass multiple {@link TableTransactionAction actions} that
     * share the same row key will cause an error.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Submits a transaction that contains multiple {@link TableTransactionAction actions} to be applied to
     * {@link TableEntity entities} on a table. Prints out details of each {@link TableTransactionAction action}'s
     * {@link Response HTTP response}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.submitTransaction#List}
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * {@codesnippet com.azure.data.tables.tableAsyncClient.submitTransactionWithError#List}
     *
     * @param transactionActions A {@link List} of {@link TableTransactionAction actions} to perform on
     * {@link TableEntity entities} in a table.
     *
     * @return A {@link List} of {@link TableTransactionActionResponse sub-responses} that correspond to each
     * {@link TableTransactionResult action} in the transaction.
     *
     * @throws IllegalArgumentException If no {@link TableTransactionAction actions} have been added to the list.
     * @throws TableTransactionFailedException If any {@link TableTransactionResult action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableTransactionResult submitTransaction(List<TableTransactionAction> transactionActions) {
        return client.submitTransaction(transactionActions).block();
    }

    /**
     * Executes all {@link TableTransactionAction actions} within the list inside a transaction. When the call
     * completes, either all {@link TableTransactionAction actions} in the transaction will succeed, or if a failure
     * occurs, all {@link TableTransactionAction actions} in the transaction will be rolled back.
     * {@link TableTransactionAction Actions} are executed sequantially. Each {@link TableTransactionAction action}
     * must operate on a distinct row key. Attempting to pass multiple {@link TableTransactionAction actions} that
     * share the same row key will cause an error.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Submits a transaction that contains multiple {@link TableTransactionAction actions} to be applied to
     * {@link TableEntity entities} on a table. Prints out details of the {@link Response HTTP response} for the
     * operation, as well as each {@link TableTransactionAction action}'s corresponding {@link Response HTTP
     * response}.</p>
     * {@codesnippet com.azure.data.tables.tableClient.submitTransactionWithResponse#List-Duration-Context}
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * {@codesnippet com.azure.data.tables.tableClient.submitTransactionWithResponseWithError#List-Duration-Context}
     *
     * @param transactionActions A {@link List} of {@link TableTransactionAction transaction actions} to perform on
     * {@link TableEntity entities} in a table.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return An {@link Response HTTP response} produced for the transaction itself. The response's value will contain
     * a {@link List} of {@link TableTransactionActionResponse sub-responses} that correspond to each
     * {@link TableTransactionAction action} in the transaction.
     *
     * @throws IllegalArgumentException If no {@link TableTransactionAction actions} have been added to the list.
     * @throws TableTransactionFailedException If any {@link TableTransactionAction action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableTransactionResult> submitTransactionWithResponse(List<TableTransactionAction> transactionActions, Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.submitTransactionWithResponse(transactionActions, context), timeout);
    }
}
