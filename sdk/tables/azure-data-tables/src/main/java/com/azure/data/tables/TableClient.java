// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.EntityPaged;
import com.azure.data.tables.implementation.TableEntityAccessHelper;
import com.azure.data.tables.implementation.TableItemAccessHelper;
import com.azure.data.tables.implementation.TableSasGenerator;
import com.azure.data.tables.implementation.TableSasUtils;
import com.azure.data.tables.implementation.TableTransactionActionResponseAccessHelper;
import com.azure.data.tables.implementation.TableUtils;
import com.azure.data.tables.implementation.TransactionalBatchImpl;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.SignedIdentifier;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceError;
import com.azure.data.tables.implementation.models.TablesGetAccessPolicyHeaders;
import com.azure.data.tables.implementation.models.TablesQueryEntitiesHeaders;
import com.azure.data.tables.implementation.models.TablesQueryEntityWithPartitionAndRowKeyHeaders;
import com.azure.data.tables.implementation.models.TablesSetAccessPolicyHeaders;
import com.azure.data.tables.implementation.models.TransactionalBatchAction;
import com.azure.data.tables.implementation.models.TransactionalBatchChangeSet;
import com.azure.data.tables.implementation.models.TransactionalBatchRequestBody;
import com.azure.data.tables.implementation.models.TransactionalBatchSubRequest;
import com.azure.data.tables.implementation.models.TransactionalBatchSubmitBatchHeaders;
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

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.data.tables.implementation.TableUtils.mapThrowableToTableServiceException;
import static com.azure.data.tables.implementation.TableUtils.toTableServiceError;

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
 * <!-- src_embed com.azure.data.tables.tableClient.instantiation -->
 * <pre>
 * TableClient tableClient = new TableClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;myaccount.core.windows.net&#47;&quot;&#41;
 *     .credential&#40;new AzureNamedKeyCredential&#40;&quot;name&quot;, &quot;key&quot;&#41;&#41;
 *     .tableName&#40;&quot;myTable&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableClient.instantiation -->
 *
 * @see TableClientBuilder
 */
@ServiceClient(builder = TableClientBuilder.class)
public final class TableClient {

    private static final ExecutorService THREAD_POOL = TableUtils.getThreadPoolWithShutdownHook();
    private final ClientLogger logger = new ClientLogger(TableClient.class);
    private final String tableName;
    private final AzureTableImpl tablesImplementation;
    private final TransactionalBatchImpl transactionalBatchImplementation;
    private final String accountName;
    private final String tableEndpoint;
    private final HttpPipeline pipeline;
    private final TableClient transactionalBatchClient;

    TableClient(String tableName, HttpPipeline pipeline, String serviceUrl, TableServiceVersion serviceVersion,
                SerializerAdapter tablesSerializer, SerializerAdapter transactionalBatchSerializer) {
        try {
            if (tableName == null) {
                throw new NullPointerException(("'tableName' must not be null to create TableClient."));
            }

            if (tableName.isEmpty()) {
                throw new IllegalArgumentException("'tableName' must not be empty to create a TableClient.");
            }

            final URI uri = URI.create(serviceUrl);
            this.accountName = uri.getHost().split("\\.", 2)[0];
            this.tableEndpoint = uri.resolve("/" + tableName).toString();
            logger.verbose("Table Service URI: {}", uri);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }

        this.tablesImplementation = new AzureTableImplBuilder()
            .url(serviceUrl)
            .serializerAdapter(tablesSerializer)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.transactionalBatchImplementation =
            new TransactionalBatchImpl(tablesImplementation, transactionalBatchSerializer);
        this.tableName = tableName;
        this.pipeline = tablesImplementation.getHttpPipeline();
        this.transactionalBatchClient = new TableClient(this, serviceVersion, tablesSerializer);
    }

    TableClient(TableClient client, ServiceVersion serviceVersion, SerializerAdapter tablesSerializer) {
        this.accountName = client.getAccountName();
        this.tableEndpoint = client.getTableEndpoint();
        this.pipeline = BuilderHelper.buildNullClientPipeline();
        this.tablesImplementation = new AzureTableImplBuilder()
            .url(client.getTablesImplementation().getUrl())
            .serializerAdapter(tablesSerializer)
            .pipeline(this.pipeline)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.tableName = client.getTableName();
        this.transactionalBatchImplementation = null;
        this.transactionalBatchClient = null;
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return tableName;
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
     * Gets the endpoint for this table.
     *
     * @return The endpoint for this table.
     */
    public String getTableEndpoint() {
        return tableEndpoint;
    }

    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the {@link AzureTableImpl} powering this client.
     *
     * @return This client's {@link AzureTableImpl}.
     */
    AzureTableImpl getTablesImplementation() {
        return tablesImplementation;
    }

    /**
     * Gets the REST API version used by this client.
     *
     * @return The REST API version used by this client.
     */
    public TableServiceVersion getServiceVersion() {
        return TableServiceVersion.fromString(tablesImplementation.getVersion());
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
        AzureNamedKeyCredential azureNamedKeyCredential = TableSasUtils.extractNamedKeyCredential(getHttpPipeline());

        if (azureNamedKeyCredential == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot generate a SAS token with a client that"
                + " is not authenticated with an AzureNamedKeyCredential."));
        }

        return new TableSasGenerator(tableSasSignatureValues, getTableName(), azureNamedKeyCredential).getSas();
    }

    /**
     * Creates the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.createTable -->
     * <pre>
     * TableItem tableItem = tableClient.createTable&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableItem.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.createTable -->
     *
     * @return A {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableItem createTable() {
        return createTableWithResponse(null, null).getValue();
    }

    /**
     * Creates the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the {@link Response HTTP response} and the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.createTableWithResponse#Duration-Context -->
     * <pre>
     * Response&lt;TableItem&gt; response = tableClient.createTableWithResponse&#40;Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.createTableWithResponse#Duration-Context -->
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
        Context contextValue = TableUtils.setContext(context, true);
        final TableProperties properties = new TableProperties().setTableName(tableName);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Callable<Response<TableItem>> callable = () ->
            new SimpleResponse<>(tablesImplementation.getTables().createWithResponse(properties,
            null,
            ResponseFormat.RETURN_NO_CONTENT, null, contextValue),
                TableItemAccessHelper.createItem(new TableResponseProperties().setTableName(tableName)));

        try {
            Response<TableItem> response = timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
            return response;
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) TableUtils.mapThrowableToTableServiceException(ex));
        }
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.deleteTable -->
     * <pre>
     * tableClient.deleteTable&#40;&#41;;
     *
     * System.out.print&#40;&quot;Table was deleted.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.deleteTable -->
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTable() {
        deleteTableWithResponse(null, null);
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table. Prints out the details of the {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.deleteTableWithResponse#Duration-Context -->
     * <pre>
     * Response&lt;Void&gt; response = tableClient.deleteTableWithResponse&#40;Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Table was deleted successfully with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.deleteTableWithResponse#Duration-Context -->
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
        Context contextValue = TableUtils.setContext(context, true);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        Callable<Response<Void>> callable = () ->
            new SimpleResponse<>(tablesImplementation.getTables().deleteWithResponse(
            tableName, null, contextValue),
            null);

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            Throwable except = mapThrowableToTableServiceException(ex);
            return swallow404Exception(except);
        }
    }

    private Response<Void> swallow404Exception(Throwable ex) {
        if (ex instanceof TableServiceException
            &&
            ((TableServiceException) ex).getResponse().getStatusCode() == 404) {
            return new SimpleResponse<>(
                ((TableServiceException) ex).getResponse().getRequest(),
                ((TableServiceException) ex).getResponse().getStatusCode(),
                ((TableServiceException) ex).getResponse().getHeaders(),
                null);
        } else {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the created
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.createEntity#TableEntity -->
     * <pre>
     * String partitionKey = &quot;partitionKey&quot;;
     * String rowKey = &quot;rowKey&quot;;
     *
     * TableEntity tableEntity = new TableEntity&#40;partitionKey, rowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableClient.createEntity&#40;tableEntity&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was created.&quot;, partitionKey, rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.createEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to insert.
     *
     * @throws TableServiceException If an {@link TableEntity entity} with the same partition key and row key already
     * exists within the table.
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEntity(TableEntity entity) {
        createEntityWithResponse(entity, null, null);
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the
     * {@link Response HTTP response} and the created {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.createEntityWithResponse#TableEntity-Duration-Context -->
     * <pre>
     * String myPartitionKey = &quot;partitionKey&quot;;
     * String myRowKey = &quot;rowKey&quot;;
     *
     * TableEntity myTableEntity = new TableEntity&#40;myPartitionKey, myRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Void&gt; response = tableClient.createEntityWithResponse&#40;myTableEntity, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and row key&quot;
     *     + &quot; '%s' was created.&quot;, response.getStatusCode&#40;&#41;, myPartitionKey, myRowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.createEntityWithResponse#TableEntity-Duration-Context -->
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
        Context contextValue = TableUtils.setContext(context, true);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        if (entity == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'entity' cannot be null."));
        }

        EntityHelper.setPropertiesFromGetters(entity, logger);
        Callable<Response<Void>> callable = () -> {
            Response<Map<String, Object>> response = tablesImplementation.getTables().insertEntityWithResponse(
                tableName, null, null, ResponseFormat.RETURN_NO_CONTENT,
                entity.getProperties(), null, contextValue);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    /**
     * Inserts an {@link TableEntity entity} into the table if it does not exist, or merges the
     * {@link TableEntity entity} with the existing {@link TableEntity entity} otherwise.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Upserts an {@link TableEntity entity} into the table. Prints out the details of the upserted
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.upsertEntity#TableEntity -->
     * <pre>
     * String partitionKey = &quot;partitionKey&quot;;
     * String rowKey = &quot;rowKey&quot;;
     *
     * TableEntity tableEntity = new TableEntity&#40;partitionKey, rowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableClient.upsertEntity&#40;tableEntity&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was updated&#47;created.&quot;, partitionKey,
     *     rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.upsertEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to upsert.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upsertEntity(TableEntity entity) {
        upsertEntityWithResponse(entity, null, null, null);
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
     * <!-- src_embed com.azure.data.tables.tableClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode-Duration-Context -->
     * <pre>
     * String myPartitionKey = &quot;partitionKey&quot;;
     * String myRowKey = &quot;rowKey&quot;;
     *
     * TableEntity myTableEntity = new TableEntity&#40;myPartitionKey, myRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Void&gt; response = tableClient.upsertEntityWithResponse&#40;myTableEntity, TableEntityUpdateMode.REPLACE,
     *     Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and row key&quot;
     *     + &quot; '%s' was updated&#47;created.&quot;, response.getStatusCode&#40;&#41;, partitionKey, rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode-Duration-Context -->
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
        Context contextValue = TableUtils.setContext(context, true);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        if (entity == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'entity' cannot be null."));
        }

        String partitionKey = TableUtils.escapeSingleQuotes(entity.getPartitionKey());
        String rowKey = TableUtils.escapeSingleQuotes(entity.getRowKey());

        EntityHelper.setPropertiesFromGetters(entity, logger);

        Callable<Response<Void>> callable = () -> {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables().updateEntityWithResponse(
                    tableName, partitionKey, rowKey, null, null, null,
                    entity.getProperties(), null, contextValue);
            } else {
                return tablesImplementation.getTables().mergeEntityWithResponse(
                    tableName, partitionKey, rowKey, null, null, null,
                    entity.getProperties(), null, contextValue);
            }
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    /**
     * Updates an existing {@link TableEntity entity} by merging the provided {@link TableEntity entity} with the
     * existing {@link TableEntity entity}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table. Prints out the details of the updated
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.updateEntity#TableEntity -->
     * <pre>
     * String partitionKey = &quot;partitionKey&quot;;
     * String rowKey = &quot;rowKey&quot;;
     *
     * TableEntity tableEntity = new TableEntity&#40;partitionKey, rowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableClient.updateEntity&#40;tableEntity&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was updated&#47;created.&quot;, partitionKey,
     *     rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.updateEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to update.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateEntity(TableEntity entity) {
        updateEntity(entity, null);
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
     * <!-- src_embed com.azure.data.tables.tableClient.updateEntity#TableEntity-TableEntityUpdateMode -->
     * <pre>
     * String myPartitionKey = &quot;partitionKey&quot;;
     * String myRowKey = &quot;rowKey&quot;;
     *
     * TableEntity myTableEntity = new TableEntity&#40;myPartitionKey, myRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableClient.updateEntity&#40;myTableEntity, TableEntityUpdateMode.REPLACE&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was updated&#47;created.&quot;, partitionKey,
     *     rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.updateEntity#TableEntity-TableEntityUpdateMode -->
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
        updateEntityWithResponse(entity, updateMode, false, null, null);
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
     * <!-- src_embed com.azure.data.tables.tableClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean-Duration-Context -->
     * <pre>
     * String somePartitionKey = &quot;partitionKey&quot;;
     * String someRowKey = &quot;rowKey&quot;;
     *
     * TableEntity someTableEntity = new TableEntity&#40;somePartitionKey, someRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Void&gt; response = tableClient.updateEntityWithResponse&#40;someTableEntity, TableEntityUpdateMode.REPLACE,
     *     true, Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and row key&quot;
     *     + &quot; '%s' was updated.&quot;, response.getStatusCode&#40;&#41;, partitionKey, rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean-Duration-Context -->
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
        Context contextValue = TableUtils.setContext(context, true);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        if (entity == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'entity' cannot be null."));
        }

        String partitionKey = TableUtils.escapeSingleQuotes(entity.getPartitionKey());
        String rowKey = TableUtils.escapeSingleQuotes(entity.getRowKey());
        String eTag = ifUnchanged ? entity.getETag() : "*";

        EntityHelper.setPropertiesFromGetters(entity, logger);

        Callable<Response<Void>> callable = () -> {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables()
                    .updateEntityWithResponse(tableName, partitionKey, rowKey, null, null, eTag,
                        entity.getProperties(), null, contextValue);
            } else {
                return tablesImplementation.getTables()
                    .mergeEntityWithResponse(tableName, partitionKey, rowKey, null, null, eTag,
                        entity.getProperties(), null, contextValue);
            }
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes an {@link TableEntity entity} on the table. Prints out the entity's {@code partitionKey} and
     * {@code rowKey}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.deleteEntity#String-String -->
     * <pre>
     * String partitionKey = &quot;partitionKey&quot;;
     * String rowKey = &quot;rowKey&quot;;
     *
     * tableClient.deleteEntity&#40;partitionKey, rowKey&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was deleted.&quot;, partitionKey, rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.deleteEntity#String-String -->
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The row key of the {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty.
     * @throws TableServiceException If the request is rejected by the service.
     * @throws IllegalArgumentException If 'partitionKey' or 'rowKey' is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(String partitionKey, String rowKey) {
        deleteEntityWithResponse(partitionKey, rowKey, null, false, null, null);
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the deleted
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.deleteEntity#TableEntity -->
     * <pre>
     * String myPartitionKey = &quot;partitionKey&quot;;
     * String myRowKey = &quot;rowKey&quot;;
     *
     * TableEntity myTableEntity = new TableEntity&#40;myPartitionKey, myRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableClient.deleteEntity&#40;myTableEntity&#41;;
     *
     * System.out.printf&#40;&quot;Table entity with partition key '%s' and row key: '%s' was created.&quot;, partitionKey, rowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.deleteEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to delete.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEntity(TableEntity entity) {
        deleteEntityWithResponse(entity, false, null, null);
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the
     * {@link Response HTTP response} and the deleted {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.deleteEntityWithResponse#TableEntity-Duration-Context -->
     * <pre>
     * String somePartitionKey = &quot;partitionKey&quot;;
     * String someRowKey = &quot;rowKey&quot;;
     *
     * TableEntity someTableEntity = new TableEntity&#40;somePartitionKey, someRowKey&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Void&gt; response = tableClient.deleteEntityWithResponse&#40;someTableEntity, true, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and row key&quot;
     *     + &quot; '%s' was deleted.&quot;, response.getStatusCode&#40;&#41;, somePartitionKey, someRowKey&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.deleteEntityWithResponse#TableEntity-Duration-Context -->
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
     * @throws IllegalArgumentException If the entity has null 'partitionKey' or 'rowKey'.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged, Duration timeout,
                                                   Context context) {
        return deleteEntityWithResponse(
            entity.getPartitionKey(), entity.getRowKey(), entity.getETag(), ifUnchanged, timeout, context);
    }

    private Response<Void> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, boolean ifUnchanged,
                                            Duration timeout, Context context) {
        Context contextValue = TableUtils.setContext(context, true);
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        String finalETag = ifUnchanged ? eTag : "*";

        if (partitionKey == null || rowKey == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null"));
        }

        Callable<Response<Void>> callable = () -> tablesImplementation.getTables().deleteEntityWithResponse(
            tableName, TableUtils.escapeSingleQuotes(partitionKey), TableUtils.escapeSingleQuotes(rowKey), finalETag, null,
            null, null, contextValue);

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            Throwable except = mapThrowableToTableServiceException(ex);
            return swallow404Exception(except);
        }
    }

    /**
     * Lists all {@link TableEntity entities} within the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link TableEntity entities} on the table. Prints out the details of the
     * retrieved {@link TableEntity entities}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.listEntities -->
     * <pre>
     * PagedIterable&lt;TableEntity&gt; tableEntities = tableClient.listEntities&#40;&#41;;
     *
     * tableEntities.forEach&#40;tableEntity -&gt;
     *     System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.%n&quot;,
     *         tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.listEntities -->
     *
     * @return A {@link PagedIterable} containing all {@link TableEntity entities} within the table.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TableEntity> listEntities() {
        return listEntities(new ListEntitiesOptions(), null, null);
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
     * <!-- src_embed com.azure.data.tables.tableClient.listEntities#ListEntitiesOptions-Duration-Context -->
     * <pre>
     * List&lt;String&gt; propertiesToSelect = new ArrayList&lt;&gt;&#40;&#41;;
     * propertiesToSelect.add&#40;&quot;name&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;lastname&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;age&quot;&#41;;
     *
     * ListEntitiesOptions listEntitiesOptions = new ListEntitiesOptions&#40;&#41;
     *     .setTop&#40;15&#41;
     *     .setFilter&#40;&quot;PartitionKey eq 'MyPartitionKey' and RowKey eq 'MyRowKey'&quot;&#41;
     *     .setSelect&#40;propertiesToSelect&#41;;
     *
     * PagedIterable&lt;TableEntity&gt; myTableEntities = tableClient.listEntities&#40;listEntitiesOptions,
     *     Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * myTableEntities.forEach&#40;tableEntity -&gt; &#123;
     *     System.out.printf&#40;&quot;Retrieved entity with partition key '%s', row key '%s' and properties:%n&quot;,
     *         tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;;
     *
     *     tableEntity.getProperties&#40;&#41;.forEach&#40;&#40;key, value&#41; -&gt;
     *         System.out.printf&#40;&quot;Name: '%s'. Value: '%s'.%n&quot;, key, value&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.listEntities#ListEntitiesOptions-Duration-Context -->
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
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);

        Callable<PagedIterable<TableEntity>> callable = () -> new PagedIterable<>(
            () -> listEntitiesFirstPage(context, options, TableEntity.class),
            token -> listEntitiesNextPage(token, context, options, TableEntity.class));

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    private <T extends TableEntity> PagedResponse<T> listEntitiesFirstPage(Context context,
                                                                           ListEntitiesOptions options,
                                                                           Class<T> resultType) {
        return listEntities(null, null, context, options, resultType);
    }

    private <T extends TableEntity> PagedResponse<T> listEntitiesNextPage(String token, Context context,
                                                                          ListEntitiesOptions options,
                                                                          Class<T> resultType) {
        if (token == null) {
            return null;
        }

        try {
            String[] keys = TableUtils.getKeysFromToken(token);
            return listEntities(keys[0], keys[1], context, options, resultType);
        } catch (RuntimeException ex) {
            throw logger.logExceptionAsError(ex);
        }
    }

    private <T extends TableEntity> PagedResponse<T> listEntities(String nextPartitionKey, String nextRowKey,
                                                                  Context context, ListEntitiesOptions options,
                                                                  Class<T> resultType) {
        Context contextValue = TableUtils.setContext(context, true);
        String select = null;

        if (options.getSelect() != null) {
            select = String.join(",", options.getSelect());
        }

        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setSelect(select)
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        final ResponseBase<TablesQueryEntitiesHeaders, TableEntityQueryResponse> response =
            tablesImplementation.getTables().queryEntitiesWithResponse(tableName, null, null,
            nextPartitionKey, nextRowKey, queryOptions, contextValue);

        final TableEntityQueryResponse tablesQueryEntityResponse = response.getValue();

        if (tablesQueryEntityResponse == null) {
            return null;
        }

        final List<Map<String, Object>> entityResponseValue = tablesQueryEntityResponse.getValue();

        if (entityResponseValue == null) {
            return null;
        }

        final List<T> entities = entityResponseValue.stream()
            .map(TableEntityAccessHelper::createEntity)
            .map(e -> EntityHelper.convertToSubclass(e, resultType, logger))
            .collect(Collectors.toList());

        return new EntityPaged<>(response, entities,
            response.getDeserializedHeaders().getXMsContinuationNextPartitionKey(),
            response.getDeserializedHeaders().getXMsContinuationNextRowKey());
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the retrieved
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.getEntity#String-String -->
     * <pre>
     * String partitionKey = &quot;partitionKey&quot;;
     * String rowKey = &quot;rowKey&quot;;
     *
     * TableEntity tableEntity = tableClient.getEntity&#40;partitionKey, rowKey&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.&quot;, tableEntity.getPartitionKey&#40;&#41;,
     *     tableEntity.getRowKey&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.getEntity#String-String -->
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
        return getEntityWithResponse(partitionKey, rowKey, null, null, null).getValue();
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the {@link Response HTTP response}
     * retrieved {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.getEntityWithResponse#String-String-ListEntitiesOptions-Duration-Context -->
     * <pre>
     * String myPartitionKey = &quot;partitionKey&quot;;
     * String myRowKey = &quot;rowKey&quot;;
     *
     * List&lt;String&gt; propertiesToSelect = new ArrayList&lt;&gt;&#40;&#41;;
     * propertiesToSelect.add&#40;&quot;name&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;lastname&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;age&quot;&#41;;
     *
     * Response&lt;TableEntity&gt; response = tableClient.getEntityWithResponse&#40;myPartitionKey, myRowKey, propertiesToSelect,
     *     Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * TableEntity myTableEntity = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved entity with partition key '%s', row key&quot;
     *         + &quot; '%s' and properties:&quot;, response.getStatusCode&#40;&#41;, myTableEntity.getPartitionKey&#40;&#41;,
     *     myTableEntity.getRowKey&#40;&#41;&#41;;
     *
     * myTableEntity.getProperties&#40;&#41;.forEach&#40;&#40;key, value&#41; -&gt;
     *     System.out.printf&#40;&quot;%nName: '%s'. Value: '%s'.&quot;, key, value&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.getEntityWithResponse#String-String-ListEntitiesOptions-Duration-Context -->
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
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null}
     * or if the {@code select} OData query option is malformed.
     * @throws TableServiceException If no {@link TableEntity entity} with the provided {@code partitionKey} and
     * {@code rowKey} exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableEntity> getEntityWithResponse(String partitionKey, String rowKey, List<String> select,
                                                       Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Context contextValue = TableUtils.setContext(context, true);

        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        if (select != null) {
            queryOptions.setSelect(String.join(",", select));
        }

        if (partitionKey == null || rowKey == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null."));
        }

        Callable<Response<TableEntity>> callable = () -> {
            ResponseBase<TablesQueryEntityWithPartitionAndRowKeyHeaders, Map<String, Object>> response =
                tablesImplementation.getTables().queryEntityWithPartitionAndRowKeyWithResponse(
                tableName, TableUtils.escapeSingleQuotes(partitionKey), TableUtils.escapeSingleQuotes(rowKey), null, null,
                queryOptions, contextValue);

            final Map<String, Object> matchingEntity = response.getValue();

            if (matchingEntity == null || matchingEntity.isEmpty()) {
                logger.info("There was no matching entity. Table {}, partition key: {}, row key: {}.",
                    tableName, partitionKey, rowKey);
                return null;
            }

            final TableEntity entity = TableEntityAccessHelper.createEntity(matchingEntity);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                EntityHelper.convertToSubclass(entity, TableEntity.class, logger));
        };


        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }

    /**
     * Retrieves details about any stored {@link TableAccessPolicies access policies} specified on the table that may
     * be used with Shared Access Signatures.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a table's {@link TableAccessPolicies access policies}. Prints out the details of the retrieved
     * {@link TableAccessPolicies access policies}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.getAccessPolicies -->
     * <pre>
     * TableAccessPolicies accessPolicies = tableClient.getAccessPolicies&#40;&#41;;
     *
     * accessPolicies.getIdentifiers&#40;&#41;.forEach&#40;signedIdentifier -&gt;
     *     System.out.printf&#40;&quot;Retrieved table access policy with id '%s'.&quot;, signedIdentifier.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.getAccessPolicies -->
     *
     * @return The table's {@link TableAccessPolicies access policies}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableAccessPolicies getAccessPolicies() {

        return getAccessPoliciesWithResponse(null, null).getValue();
    }

    /**
     * Retrieves details about any stored {@link TableAccessPolicies access policies} specified on the table that may be
     * used with Shared Access Signatures.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a table's {@link TableAccessPolicies access policies}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link TableAccessPolicies access policies}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.getAccessPoliciesWithResponse#Duration-Context -->
     * <pre>
     * List&lt;String&gt; propertiesToSelect = new ArrayList&lt;&gt;&#40;&#41;;
     * propertiesToSelect.add&#40;&quot;name&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;lastname&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;age&quot;&#41;;
     *
     * Response&lt;TableAccessPolicies&gt; response = tableClient.getAccessPoliciesWithResponse&#40;Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved table access policies with the following&quot;
     *     + &quot; IDs:&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     * response.getValue&#40;&#41;.getIdentifiers&#40;&#41;.forEach&#40;signedIdentifier -&gt;
     *     System.out.printf&#40;&quot;%n%s&quot;, signedIdentifier.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.getAccessPoliciesWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return An {@link Response HTTP response} containing the table's {@link TableAccessPolicies access policies}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableAccessPolicies> getAccessPoliciesWithResponse(Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Context contextValue = TableUtils.setContext(context, true);

        Callable<Response<TableAccessPolicies>> callable = () -> {
            ResponseBase<TablesGetAccessPolicyHeaders, List<SignedIdentifier>> response =
                tablesImplementation.getTables().getAccessPolicyWithResponse(
                    tableName, null, null, contextValue
            );
            return new SimpleResponse<>(response,
                new TableAccessPolicies(response.getValue() == null ? null : response.getValue().stream()
                    .map(TableUtils::toTableSignedIdentifier)
                    .collect(Collectors.toList())));
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
    }


    /**
     * Sets stored {@link TableAccessPolicies access policies} for the table that may be used with Shared Access
     * Signatures.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets stored {@link TableAccessPolicies access policies} on a table.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.setAccessPolicies#List -->
     * <pre>
     * List&lt;TableSignedIdentifier&gt; signedIdentifiers = new ArrayList&lt;&gt;&#40;&#41;;
     *
     * signedIdentifiers.add&#40;new TableSignedIdentifier&#40;&quot;id1&quot;&#41;
     *     .setAccessPolicy&#40;new TableAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.of&#40;2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.of&#40;2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setPermissions&#40;&quot;r&quot;&#41;&#41;&#41;;
     * signedIdentifiers.add&#40;new TableSignedIdentifier&#40;&quot;id2&quot;&#41;
     *     .setAccessPolicy&#40;new TableAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.of&#40;2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.of&#40;2021, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setPermissions&#40;&quot;raud&quot;&#41;&#41;&#41;;
     *
     * tableClient.setAccessPolicies&#40;signedIdentifiers&#41;;
     *
     * System.out.print&#40;&quot;Set table access policies.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.setAccessPolicies#List -->
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setAccessPolicies(List<TableSignedIdentifier> tableSignedIdentifiers) {
        setAccessPoliciesWithResponse(tableSignedIdentifiers, null, null);
    }

    /**
     * Sets stored {@link TableAccessPolicies access policies} for the table that may be used with Shared Access
     * Signatures.
     *
     * <p>This operation is only supported on Azure Storage endpoints.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets stored {@link TableAccessPolicies access policies} on a table. Prints out details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.setAccessPoliciesWithResponse#List-Duration-Context -->
     * <pre>
     * List&lt;TableSignedIdentifier&gt; mySignedIdentifiers = new ArrayList&lt;&gt;&#40;&#41;;
     *
     * mySignedIdentifiers.add&#40;new TableSignedIdentifier&#40;&quot;id1&quot;&#41;
     *     .setAccessPolicy&#40;new TableAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.of&#40;2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.of&#40;2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setPermissions&#40;&quot;r&quot;&#41;&#41;&#41;;
     * mySignedIdentifiers.add&#40;new TableSignedIdentifier&#40;&quot;id2&quot;&#41;
     *     .setAccessPolicy&#40;new TableAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.of&#40;2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.of&#40;2021, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC&#41;&#41;
     *         .setPermissions&#40;&quot;raud&quot;&#41;&#41;&#41;;
     *
     * Response&lt;Void&gt; response = tableClient.setAccessPoliciesWithResponse&#40;mySignedIdentifiers, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Set table access policies successfully with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.setAccessPoliciesWithResponse#List-Duration-Context -->
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline HTTP pipeline} during
     * the service call.
     *
     * @return The {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers,
                                                        Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Context contextValue = TableUtils.setContext(context, true);
        List<SignedIdentifier> signedIdentifiers = null;

        if (tableSignedIdentifiers != null) {
            signedIdentifiers = tableSignedIdentifiers.stream()
                .map(tableSignedIdentifier -> {
                    SignedIdentifier signedIdentifier = TableUtils.toSignedIdentifier(tableSignedIdentifier);

                    if (signedIdentifier != null) {
                        if (signedIdentifier.getAccessPolicy() != null
                            && signedIdentifier.getAccessPolicy().getStart() != null) {

                            signedIdentifier.getAccessPolicy()
                                .setStart(signedIdentifier.getAccessPolicy()
                                    .getStart().truncatedTo(ChronoUnit.SECONDS));
                        }

                        if (signedIdentifier.getAccessPolicy() != null
                            && signedIdentifier.getAccessPolicy().getExpiry() != null) {

                            signedIdentifier.getAccessPolicy()
                                .setExpiry(signedIdentifier.getAccessPolicy()
                                    .getExpiry().truncatedTo(ChronoUnit.SECONDS));
                        }
                    }

                    return signedIdentifier;
                })
                .collect(Collectors.toList());
        }

        List<SignedIdentifier> finalSignedIdentifiers = signedIdentifiers;
        Callable<Response<Void>> callable = () -> {
            ResponseBase<TablesSetAccessPolicyHeaders, Void> response = tablesImplementation.getTables()
                .setAccessPolicyWithResponse(tableName, null, null,
                    finalSignedIdentifiers, contextValue);
            return new SimpleResponse<>(response, response.getValue());
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {
            throw logger.logExceptionAsError((RuntimeException) (TableUtils.mapThrowableToTableServiceException(ex)));
        }
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
     * <!-- src_embed com.azure.data.tables.tableClient.submitTransaction#List -->
     * <pre>
     * List&lt;TableTransactionAction&gt; transactionActions = new ArrayList&lt;&gt;&#40;&#41;;
     *
     * String partitionKey = &quot;markers&quot;;
     * String firstEntityRowKey = &quot;m001&quot;;
     * String secondEntityRowKey = &quot;m002&quot;;
     *
     * TableEntity firstEntity = new TableEntity&#40;partitionKey, firstEntityRowKey&#41;
     *     .addProperty&#40;&quot;Type&quot;, &quot;Dry&quot;&#41;
     *     .addProperty&#40;&quot;Color&quot;, &quot;Red&quot;&#41;;
     *
     * transactionActions.add&#40;new TableTransactionAction&#40;TableTransactionActionType.CREATE, firstEntity&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Added create action for entity with partition key '%s', and row key '%s'.%n&quot;, partitionKey,
     *     firstEntityRowKey&#41;;
     *
     * TableEntity secondEntity = new TableEntity&#40;partitionKey, secondEntityRowKey&#41;
     *     .addProperty&#40;&quot;Type&quot;, &quot;Wet&quot;&#41;
     *     .addProperty&#40;&quot;Color&quot;, &quot;Blue&quot;&#41;;
     *
     * transactionActions.add&#40;new TableTransactionAction&#40;TableTransactionActionType.CREATE, secondEntity&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Added create action for entity with partition key '%s', and row key '%s'.%n&quot;, partitionKey,
     *     secondEntityRowKey&#41;;
     *
     * TableTransactionResult tableTransactionResult = tableClient.submitTransaction&#40;transactionActions&#41;;
     *
     * System.out.print&#40;&quot;Submitted transaction. The ordered response status codes for the actions are:&quot;&#41;;
     *
     * tableTransactionResult.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *     System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.submitTransaction#List -->
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.submitTransactionWithError#List -->
     * <pre>
     *
     * tableAsyncClient.submitTransaction&#40;transactionActions&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .doOnError&#40;TableTransactionFailedException.class, e -&gt; &#123;
     *         &#47;&#47; If the transaction fails, the resulting exception contains the index of the first action that failed.
     *         int failedActionIndex = e.getFailedTransactionActionIndex&#40;&#41;;
     *         &#47;&#47; You can use this index to modify the offending action or remove it from the list of actions to send
     *         &#47;&#47; in the transaction, for example.
     *         transactionActions.remove&#40;failedActionIndex&#41;;
     *         &#47;&#47; And then retry submitting the transaction.
     *     &#125;&#41;
     *     .subscribe&#40;tableTransactionResult -&gt; &#123;
     *         System.out.print&#40;&quot;Submitted transaction. The ordered response status codes for the actions are:&quot;&#41;;
     *
     *         tableTransactionResult.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.submitTransactionWithError#List -->
     *
     * @param transactionActions A {@link List} of {@link TableTransactionAction actions} to perform on
     * {@link TableEntity entities} in a table.
     *
     * @return A {@link List} of {@link TableTransactionActionResponse sub-responses} that correspond to each
     * {@link TableTransactionResult action} in the transaction.
     *
     * @throws IllegalArgumentException If no {@link TableTransactionAction actions} have been added to the list.
     * @throws TableServiceException If the request is rejected by the service.
     * @throws TableTransactionFailedException If any {@link TableTransactionResult action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TableTransactionResult submitTransaction(List<TableTransactionAction> transactionActions) {
        return submitTransactionWithResponse(transactionActions, null, null).getValue();
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
     * <!-- src_embed com.azure.data.tables.tableClient.submitTransactionWithResponse#List-Duration-Context -->
     * <pre>
     * List&lt;TableTransactionAction&gt; myTransactionActions = new ArrayList&lt;&gt;&#40;&#41;;
     *
     * String myPartitionKey = &quot;markers&quot;;
     * String myFirstEntityRowKey = &quot;m001&quot;;
     * String mySecondEntityRowKey = &quot;m002&quot;;
     *
     * TableEntity myFirstEntity = new TableEntity&#40;myPartitionKey, myFirstEntityRowKey&#41;
     *     .addProperty&#40;&quot;Type&quot;, &quot;Dry&quot;&#41;
     *     .addProperty&#40;&quot;Color&quot;, &quot;Red&quot;&#41;;
     *
     * myTransactionActions.add&#40;new TableTransactionAction&#40;TableTransactionActionType.CREATE, myFirstEntity&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Added create action for entity with partition key '%s', and row key '%s'.%n&quot;, myPartitionKey,
     *     myFirstEntityRowKey&#41;;
     *
     * TableEntity mySecondEntity = new TableEntity&#40;myPartitionKey, mySecondEntityRowKey&#41;
     *     .addProperty&#40;&quot;Type&quot;, &quot;Wet&quot;&#41;
     *     .addProperty&#40;&quot;Color&quot;, &quot;Blue&quot;&#41;;
     *
     * myTransactionActions.add&#40;new TableTransactionAction&#40;TableTransactionActionType.CREATE, mySecondEntity&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Added create action for entity with partition key '%s', and row key '%s'.%n&quot;, myPartitionKey,
     *     mySecondEntityRowKey&#41;;
     *
     * Response&lt;TableTransactionResult&gt; response = tableClient.submitTransactionWithResponse&#40;myTransactionActions,
     *     Duration.ofSeconds&#40;5&#41;, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. The ordered response status codes of the submitted&quot;
     *     + &quot; actions are:&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     * response.getValue&#40;&#41;.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *     System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.submitTransactionWithResponse#List-Duration-Context -->
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.submitTransactionWithResponseWithError#List-Duration-Context -->
     * <pre>
     * try &#123;
     *     Response&lt;TableTransactionResult&gt; transactionResultResponse =
     *         tableClient.submitTransactionWithResponse&#40;myTransactionActions, Duration.ofSeconds&#40;5&#41;,
     *             new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Response successful with status code: %d. The ordered response status codes of the&quot;
     *         + &quot; submitted actions are:&quot;, transactionResultResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     transactionResultResponse.getValue&#40;&#41;.getTransactionActionResponses&#40;&#41;
     *         .forEach&#40;tableTransactionActionResponse -&gt;
     *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * &#125; catch &#40;TableTransactionFailedException e&#41; &#123;
     *     &#47;&#47; If the transaction fails, the resulting exception contains the index of the first action that failed.
     *     int failedActionIndex = e.getFailedTransactionActionIndex&#40;&#41;;
     *     &#47;&#47; You can use this index to modify the offending action or remove it from the list of actions to send in
     *     &#47;&#47; the transaction, for example.
     *     myTransactionActions.remove&#40;failedActionIndex&#41;;
     *     &#47;&#47; And then retry submitting the transaction.
     * &#125;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.submitTransactionWithResponseWithError#List-Duration-Context -->
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
     * @throws TableServiceException If the request is rejected by the service.
     * @throws TableTransactionFailedException If any {@link TableTransactionAction action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TableTransactionResult> submitTransactionWithResponse(List<TableTransactionAction> transactionActions, Duration timeout, Context context) {
        OptionalLong timeoutInMillis = TableUtils.setTimeout(timeout);
        Context contextValue = TableUtils.setContext(context, true);

        if (transactionActions.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("A transaction must contain at least one operation."));
        }

        final List<TransactionalBatchAction> operations = new ArrayList<>();

        for (TableTransactionAction transactionAction : transactionActions) {
            switch (transactionAction.getActionType()) {
                case CREATE:
                    operations.add(new TransactionalBatchAction.CreateEntity(transactionAction.getEntity()));

                    break;
                case UPSERT_MERGE:
                    operations.add(new TransactionalBatchAction.UpsertEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.MERGE));

                    break;
                case UPSERT_REPLACE:
                    operations.add(new TransactionalBatchAction.UpsertEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.REPLACE));

                    break;
                case UPDATE_MERGE:
                    operations.add(new TransactionalBatchAction.UpdateEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.MERGE, transactionAction.getIfUnchanged()));

                    break;
                case UPDATE_REPLACE:
                    operations.add(new TransactionalBatchAction.UpdateEntity(transactionAction.getEntity(),
                        TableEntityUpdateMode.REPLACE, transactionAction.getIfUnchanged()));

                    break;
                case DELETE:
                    operations.add(
                        new TransactionalBatchAction.DeleteEntity(transactionAction.getEntity(),
                            transactionAction.getIfUnchanged()));

                    break;
                default:
                    break;
            }
        }

        Callable<Response<TableTransactionResult>> callable = () -> {
            BiConsumer<TransactionalBatchRequestBody, RequestActionPair> accumulator = (body, pair) ->
                body.addChangeOperation(new TransactionalBatchSubRequest(pair.getAction(), pair.getRequest()));
            BiConsumer<TransactionalBatchRequestBody, TransactionalBatchRequestBody> combiner = (body1, body2) ->
                body2.getContents().forEach(req -> body1.addChangeOperation((TransactionalBatchSubRequest) req));
            TransactionalBatchRequestBody requestBody =
                operations.stream()
                    .map(op -> new RequestActionPair(op.prepareRequest(transactionalBatchClient), op))
                    .collect(TransactionalBatchRequestBody::new, accumulator, combiner);

            ResponseBase<TransactionalBatchSubmitBatchHeaders, TableTransactionActionResponse[]> response =
                transactionalBatchImplementation
                    .submitTransactionalBatchWithRestResponse(requestBody, null, contextValue);

            Response<List<TableTransactionActionResponse>> parsedResponse = parseResponse(requestBody, response);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                new TableTransactionResult(transactionActions, parsedResponse.getValue()));
        };

        try {
            return timeoutInMillis.isPresent()
                ? THREAD_POOL.submit(callable).get(timeoutInMillis.getAsLong(), TimeUnit.MILLISECONDS)
                : callable.call();
        } catch (Exception ex) {

            throw logger.logExceptionAsError((RuntimeException) TableUtils.interpretException(ex));
        }
    }

    private static class RequestActionPair {
        private final HttpRequest request;
        private final TransactionalBatchAction action;

        RequestActionPair(HttpRequest request, TransactionalBatchAction action) {
            this.request = request;
            this.action = action;
        }

        public HttpRequest getRequest() {
            return request;
        }

        public TransactionalBatchAction getAction() {
            return action;
        }
    }

    private Response<List<TableTransactionActionResponse>> parseResponse(TransactionalBatchRequestBody requestBody,
                                                                               ResponseBase<TransactionalBatchSubmitBatchHeaders, TableTransactionActionResponse[]> response) {
        TableServiceError error = null;
        String errorMessage = null;
        TransactionalBatchChangeSet changes = null;
        TransactionalBatchAction failedAction = null;
        Integer failedIndex = null;

        if (requestBody.getContents().get(0) instanceof TransactionalBatchChangeSet) {
            changes = (TransactionalBatchChangeSet) requestBody.getContents().get(0);
        }

        for (int i = 0; i < response.getValue().length; i++) {
            TableTransactionActionResponse subResponse = response.getValue()[i];

            // Attempt to attach a sub-request to each batch sub-response
            if (changes != null && changes.getContents().get(i) != null) {
                TableTransactionActionResponseAccessHelper.updateTableTransactionActionResponse(subResponse,
                    changes.getContents().get(i).getHttpRequest());
            }

            // If one sub-response was an error, we need to throw even though the service responded with 202
            if (subResponse.getStatusCode() >= 400 && error == null && errorMessage == null) {
                if (subResponse.getValue() instanceof TableServiceError) {
                    error = (TableServiceError) subResponse.getValue();

                    // Make a best effort to locate the failed operation and include it in the message
                    if (changes != null && error.getOdataError() != null
                        && error.getOdataError().getMessage() != null
                        && error.getOdataError().getMessage().getValue() != null) {

                        String message = error.getOdataError().getMessage().getValue();

                        try {
                            failedIndex = Integer.parseInt(message.substring(0, message.indexOf(":")));
                            failedAction = changes.getContents().get(failedIndex).getOperation();
                        } catch (NumberFormatException e) {
                            // Unable to parse failed operation from batch error message - this just means
                            // the service did not indicate which request was the one that failed. Since
                            // this is optional, just swallow the exception.
                        }
                    }
                } else if (subResponse.getValue() instanceof String) {
                    errorMessage = "The service returned the following data for the failed operation: "
                        + subResponse.getValue();
                } else {
                    errorMessage =
                        "The service returned the following status code for the failed operation: "
                            + subResponse.getStatusCode();
                }
            }
        }

        if (error != null || errorMessage != null) {
            String message = "An action within the operation failed, the transaction has been rolled back.";

            if (failedAction != null) {
                message += " The failed operation was: " + failedAction;
            } else if (errorMessage != null) {
                message += " " + errorMessage;
            }

            throw logger.logExceptionAsError(new RuntimeException(
                new TableTransactionFailedException(message, null, toTableServiceError(error), failedIndex)));
        } else {
            return new SimpleResponse<>(response, Arrays.asList(response.getValue()));
        }
    }
}
