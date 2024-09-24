// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
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
import com.azure.data.tables.implementation.models.TableServiceJsonError;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.tables.implementation.TableUtils.applyOptionalTimeout;
import static com.azure.data.tables.implementation.TableUtils.swallowExceptionForStatusCode;
import static com.azure.data.tables.implementation.TableUtils.toTableServiceError;

/**
 * Provides an asynchronous service client for accessing a table in the Azure Tables service.
 *
 * <h2>Overview</h2>
 *
 * <p>The client encapsulates the URL for the table within the Tables service endpoint, the name of the table, and the
 * credentials for accessing the storage or CosmosDB table API account. It provides methods to create and delete the
 * table itself, as well as methods to create, upsert, update, delete, list, and get entities within the table. These
 * methods invoke REST API operations to make the requests and obtain the results that are returned.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Authenticating and building instances of this client are handled by {@link TableClientBuilder}.
 * This sample shows how to authenticate and build a TableClient instance using the {@link TableClientBuilder} and
 * a connection string.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.instantiation.connectionstring -->
 * <pre>
 * TableAsyncClient tableAsyncClient = new TableClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .tableName&#40;&quot;myTable&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.instantiation.connectionstring -->
 *
 * <p>For more information on building and authenticating, see the {@link TableClientBuilder} documentation.</p>
 *
 * <p>The following code samples provide examples of common operations preformed with this client.</p>
 *
 * <hr/>
 *
 * <h3>Create a {@link TableEntity}</h3>
 *
 * <p>The {@link #createEntity(TableEntity) createEntity} method can be used to create a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below creates a {@link TableEntity} with a partition key of "partitionKey" and a row key of "rowKey".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.createEntity#TableEntity -->
 * <pre>
 * TableEntity tableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
 *
 * tableAsyncClient.createEntity&#40;tableEntity&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;unused -&gt;
 *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was created.&quot;, &quot;partitionKey&quot;,
 *             &quot;rowKey&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.createEntity#TableEntity -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Retrieve a {@link TableEntity}</h3>
 *
 * <p>The {@link #getEntity(String, String) getEntity} method can be used to retrieve a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below retrieves a {@link TableEntity} with a partition key of "partitionKey" and a row key of "rowKey".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.getEntity#String-String -->
 * <pre>
 * tableAsyncClient.getEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;tableEntity -&gt;
 *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.&quot;,
 *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.getEntity#String-String -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Update a {@link TableEntity}</h3>
 *
 * <p>The {@link #updateEntity(TableEntity) updateEntity} method can be used to update a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below updates a {@link TableEntity} with a partition key of "partitionKey" and a row key of "rowKey", adding a new property with a key of "Property" and a value of "Value".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode -->
 * <pre>
 * TableEntity myTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
 *
 * tableAsyncClient.updateEntity&#40;myTableEntity, TableEntityUpdateMode.REPLACE&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;unused -&gt;
 *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was updated&#47;created.&quot;,
 *             &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Listing {@link TableEntity TableEntities}</h3>
 *
 * <p>The {@link #listEntities() listEntities} method can be used to list the entities within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below lists all {@link TableEntity TableEntities} within the table without filtering out any entities.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.listEntities -->
 * <pre>
 * tableAsyncClient.listEntities&#40;&#41;
 *     .subscribe&#40;tableEntity -&gt;
 *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.%n&quot;,
 *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.listEntities -->
 *
 * <strong>List {@link TableEntity TableEntities} with filtering and selecting</strong>
 *
 * <p>The sample below lists {@link TableEntity TableEntities} within the table, filtering out any entities that do not have a partition key of "partitionKey" and a row key of "rowKey"
 *  and only selects the "name", "lastname", and "age" properties.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions -->
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
 * tableAsyncClient.listEntities&#40;listEntitiesOptions&#41;
 *     .subscribe&#40;tableEntity -&gt; &#123;
 *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s', row key '%s' and properties:%n&quot;,
 *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;;
 *
 *         tableEntity.getProperties&#40;&#41;.forEach&#40;&#40;key, value&#41; -&gt;
 *             System.out.printf&#40;&quot;Name: '%s'. Value: '%s'.%n&quot;, key, value&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Delete a {@link TableEntity}</h3>
 *
 * <p>The {@link #deleteEntity(String, String) deleteEntity} method can be used to delete a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below deletes a {@link TableEntity} with a partition key of "partitionKey" and a row key of "rowKey".</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteEntity#String-String -->
 * <pre>
 * tableAsyncClient.deleteEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;unused -&gt;
 *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was deleted.&quot;, &quot;partitionKey&quot;,
 *             &quot;rowKey&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.deleteEntity#String-String -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * <hr/>
 *
 * <h3>Submit a transactional batch</h3>
 *
 *  <p>The {@link #submitTransaction(List) submitTransaction} method can be used to submit a transactional batch of actions to perform on the table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The sample below shows how to prepare and submit a transactional batch with multiple actions.</p>
 *
 * <!-- src_embed com.azure.data.tables.tableAsyncClient.submitTransaction#List -->
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
 * tableAsyncClient.submitTransaction&#40;transactionActions&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;tableTransactionResult -&gt; &#123;
 *         System.out.print&#40;&quot;Submitted transaction. The ordered response status codes for the actions are:&quot;&#41;;
 *
 *         tableTransactionResult.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
 *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.tableAsyncClient.submitTransaction#List -->
 *
 * <em><strong>Note: </strong>for a synchronous sample, refer to {@link TableClient the synchronous client}</em>
 *
 * @see TableClientBuilder
 * @see TableEntity
 * @see com.azure.data.tables
 */
@ServiceClient(builder = TableClientBuilder.class, isAsync = true)
public final class TableAsyncClient {
    private final ClientLogger logger = new ClientLogger(TableAsyncClient.class);
    private final String tableName;
    private final AzureTableImpl tablesImplementation;
    private final TransactionalBatchImpl transactionalBatchImplementation;
    private final String accountName;
    private final String tableEndpoint;
    private final HttpPipeline pipeline;
    private final TableAsyncClient transactionalBatchClient;

    TableAsyncClient(String tableName, HttpPipeline pipeline, String serviceUrl, TableServiceVersion serviceVersion,
                     SerializerAdapter tablesSerializer, SerializerAdapter transactionalBatchSerializer) {
        try {
            if (tableName == null) {
                throw new NullPointerException("'tableName' must not be null to create a TableClient.");
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
        this.transactionalBatchClient = new TableAsyncClient(this, serviceVersion, tablesSerializer);
    }

    // Create a hollow client to be used for obtaining the body of a transaction operation to submit.
    TableAsyncClient(TableAsyncClient client, ServiceVersion serviceVersion, SerializerAdapter tablesSerializer) {
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
        // A batch prep client does not need its own batch prep client nor batch implementation.
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
     * @throws IllegalStateException If this {@link TableAsyncClient} is not authenticated with an
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.createTable -->
     * <pre>
     * tableAsyncClient.createTable&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;tableItem -&gt;
     *         System.out.printf&#40;&quot;Table with name '%s' was created.&quot;, tableItem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.createTable -->
     *
     * @return A {@link Mono} containing a {@link TableItem} that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableItem> createTable() {
        return createTableWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a table. Prints out the details of the {@link Response HTTP response} and the created table.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.createTableWithResponse -->
     * <pre>
     * tableAsyncClient.createTableWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table with name '%s' was created.&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.createTableWithResponse -->
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains a {@link TableItem}
     * that represents the table.
     *
     * @throws TableServiceException If a table with the same name already exists within the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableItem>> createTableWithResponse() {
        return withContext(this::createTableWithResponse);
    }

    Mono<Response<TableItem>> createTableWithResponse(Context context) {
        final TableProperties properties = new TableProperties().setTableName(tableName);

        try {
            return tablesImplementation.getTables().createWithResponseAsync(properties, null,
                    ResponseFormat.RETURN_NO_CONTENT, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response ->
                    new SimpleResponse<>(response,
                        TableItemAccessHelper.createItem(new TableResponseProperties().setTableName(tableName))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteTable -->
     * <pre>
     * tableAsyncClient.deleteTable&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt; System.out.print&#40;&quot;Table was deleted.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.deleteTable -->
     *
     * @return An empty {@link Mono}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTable() {
        return deleteTableWithResponse().flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes the table within the Tables service.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a table. Prints out the details of the {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteTableWithResponse -->
     * <pre>
     * tableAsyncClient.deleteTableWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Table was deleted successfully with status code: %d.&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.deleteTableWithResponse -->
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTableWithResponse() {
        return withContext(this::deleteTableWithResponse);
    }

    Mono<Response<Void>> deleteTableWithResponse(Context context) {
        try {
            return tablesImplementation.getTables().deleteWithResponseAsync(tableName, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(TableServiceException.class, e -> swallowExceptionForStatusCode(404, e, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the created
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.createEntity#TableEntity -->
     * <pre>
     * TableEntity tableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.createEntity&#40;tableEntity&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was created.&quot;, &quot;partitionKey&quot;,
     *             &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.createEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to insert.
     *
     * @return An empty {@link Mono}.
     *
     * @throws TableServiceException If an {@link TableEntity entity} with the same partition key and row key already
     * exists within the table.
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createEntity(TableEntity entity) {
        return createEntityWithResponse(entity).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an {@link TableEntity entity} into the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Inserts an {@link TableEntity entity} into the table. Prints out the details of the
     * {@link Response HTTP response} and the created {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.createEntityWithResponse#TableEntity -->
     * <pre>
     * TableEntity myTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.createEntityWithResponse&#40;myTableEntity&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and&quot;
     *             + &quot; row key '%s' was created.&quot;, response.getStatusCode&#40;&#41;, &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.createEntityWithResponse#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to insert.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws TableServiceException If an {@link TableEntity entity} with the same partition key and row key already
     * exists within the table.
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createEntityWithResponse(TableEntity entity) {
        return withContext(context -> createEntityWithResponse(entity, context));
    }

    Mono<Response<Void>> createEntityWithResponse(TableEntity entity, Context context) {
        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            return tablesImplementation.getTables().insertEntityWithResponseAsync(tableName, null, null,
                    ResponseFormat.RETURN_NO_CONTENT, entity.getProperties(), null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response ->
                    new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Inserts an {@link TableEntity entity} into the table if it does not exist, or merges the
     * {@link TableEntity entity} with the existing {@link TableEntity entity} otherwise.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Upserts an {@link TableEntity entity} into the table. Prints out the details of the upserted
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.upsertEntity#TableEntity -->
     * <pre>
     * TableEntity tableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.upsertEntity&#40;tableEntity&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was updated&#47;created.&quot;,
     *             &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.upsertEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to upsert.
     *
     * @return An empty {@link Mono}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upsertEntity(TableEntity entity) {
        return upsertEntityWithResponse(entity, null).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Inserts an {@link TableEntity entity} into the table if it does not exist, or updates the existing
     * {@link TableEntity entity} using the specified {@link TableEntityUpdateMode update mode} otherwise. The default
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}.
     *
     * <p>When the {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}, the provided
     * {@link TableEntity entity}'s properties will be merged into the existing {@link TableEntity entity}. When the
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#REPLACE REPLACE}, the provided
     * {@link TableEntity entity}'s properties will completely replace those in the existing {@link TableEntity entity}.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Upserts an {@link TableEntity entity} into the table with the specified
     * {@link TableEntityUpdateMode update mode} if said {@link TableEntity entity} already exists. Prints out the
     * details of the {@link Response HTTP response} and the upserted {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode -->
     * <pre>
     * TableEntity myTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.upsertEntityWithResponse&#40;myTableEntity, TableEntityUpdateMode.REPLACE&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and&quot;
     *             + &quot; row key '%s' was updated&#47;created.&quot;, response.getStatusCode&#40;&#41;, &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode -->
     *
     * @param entity The {@link TableEntity entity} to upsert.
     * @param updateMode The type of update to perform if the {@link TableEntity entity} already exits.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode) {
        return withContext(context -> upsertEntityWithResponse(entity, updateMode, context));
    }

    Mono<Response<Void>> upsertEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                  Context context) {
        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        String partitionKey = TableUtils.escapeSingleQuotes(entity.getPartitionKey());
        String rowKey = TableUtils.escapeSingleQuotes(entity.getRowKey());

        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables()
                    .updateEntityWithResponseAsync(tableName, partitionKey, rowKey, null, null, null,
                        entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            } else {
                return tablesImplementation.getTables()
                    .mergeEntityWithResponseAsync(tableName, partitionKey, rowKey, null, null, null,
                        entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates an existing {@link TableEntity entity} by merging the provided {@link TableEntity entity} with the
     * existing {@link TableEntity entity}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table. Prints out the details of the updated
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity -->
     * <pre>
     * TableEntity tableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.updateEntity&#40;tableEntity&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was updated&#47;created.&quot;,
     *             &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to update.
     *
     * @return An empty {@link Mono}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity) {
        return updateEntity(entity, null);
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode -->
     * <pre>
     * TableEntity myTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.updateEntity&#40;myTableEntity, TableEntityUpdateMode.REPLACE&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was updated&#47;created.&quot;,
     *             &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode -->
     *
     * @param entity The {@link TableEntity entity} to update.
     * @param updateMode The type of update to perform.
     *
     * @return An empty {@link Mono}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateEntity(TableEntity entity, TableEntityUpdateMode updateMode) {
        return updateEntityWithResponse(entity, updateMode, false)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Updates an existing {@link TableEntity entity} using the specified {@link TableEntityUpdateMode update mode}.
     * The default {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}.
     *
     * <p>When the {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#MERGE MERGE}, the provided
     * {@link TableEntity entity}'s properties will be merged into the existing {@link TableEntity entity}. When the
     * {@link TableEntityUpdateMode update mode} is {@link TableEntityUpdateMode#REPLACE REPLACE}, the provided
     * {@link TableEntity entity}'s properties will completely replace those in the existing
     * {@link TableEntity entity}.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a {@link TableEntity entity} on the table with the specified {@link TableEntityUpdateMode update
     * mode}
     * if the {@code ETags} on both {@link TableEntity entities} match. Prints out the details of the
     * {@link Response HTTP response} updated {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean -->
     * <pre>
     * TableEntity someTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.updateEntityWithResponse&#40;someTableEntity, TableEntityUpdateMode.REPLACE, true&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and&quot;
     *             + &quot; row key '%s' was updated.&quot;, response.getStatusCode&#40;&#41;, &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean -->
     *
     * @param entity The {@link TableEntity entity} to update.
     * @param updateMode The type of update to perform.
     * @param ifUnchanged When true, the ETag of the provided {@link TableEntity entity} must match the ETag of the
     * {@link TableEntity entity} in the Table service. If the values do not match, the update will not occur and an
     * exception will be thrown.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws IllegalArgumentException If the provided {@link TableEntity entity} is {@code null}.
     * @throws TableServiceException If no {@link TableEntity entity} with the same partition key and row key exists
     * within the table, or if {@code ifUnchanged} is {@code true} and the existing {@link TableEntity entity}'s ETag
     * does not match that of the provided {@link TableEntity entity}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                         boolean ifUnchanged) {
        return withContext(context -> updateEntityWithResponse(entity, updateMode, ifUnchanged, context));
    }

    Mono<Response<Void>> updateEntityWithResponse(TableEntity entity, TableEntityUpdateMode updateMode,
                                                  boolean ifUnchanged, Context context) {
        if (entity == null) {
            return monoError(logger, new IllegalArgumentException("'entity' cannot be null."));
        }

        String partitionKey = TableUtils.escapeSingleQuotes(entity.getPartitionKey());
        String rowKey = TableUtils.escapeSingleQuotes(entity.getRowKey());
        String eTag = ifUnchanged ? entity.getETag() : "*";

        EntityHelper.setPropertiesFromGetters(entity, logger);

        try {
            if (updateMode == TableEntityUpdateMode.REPLACE) {
                return tablesImplementation.getTables()
                    .updateEntityWithResponseAsync(tableName, partitionKey, rowKey, null, null, eTag,
                        entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            } else {
                return tablesImplementation.getTables()
                    .mergeEntityWithResponseAsync(tableName, partitionKey, rowKey, null, null, eTag,
                        entity.getProperties(), null, context)
                    .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                    .map(response ->
                        new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                            null));
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes an {@link TableEntity entity} on the table. Prints out the entity's {@code partitionKey} and
     * {@code rowKey}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteEntity#String-String -->
     * <pre>
     * tableAsyncClient.deleteEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was deleted.&quot;, &quot;partitionKey&quot;,
     *             &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.deleteEntity#String-String -->
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The row key of the {@link TableEntity entity}.
     *
     * @return An empty {@link Mono}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(String partitionKey, String rowKey) {
        return deleteEntityWithResponse(partitionKey, rowKey, null, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the deleted
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteEntity#TableEntity -->
     * <pre>
     * TableEntity myTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.deleteEntity&#40;myTableEntity&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Table entity with partition key '%s' and row key '%s' was created.&quot;, &quot;partitionKey&quot;,
     *             &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.deleteEntity#TableEntity -->
     *
     * @param entity The {@link TableEntity entity} to delete.
     *
     * @return An empty {@link Mono}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEntity(TableEntity entity) {
        return deleteEntityWithResponse(entity, false).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link TableEntity entity} on the table. Prints out the details of the
     * {@link Response HTTP response} and the deleted {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.deleteEntityWithResponse#TableEntity -->
     * <pre>
     * TableEntity someTableEntity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .addProperty&#40;&quot;Property&quot;, &quot;Value&quot;&#41;;
     *
     * tableAsyncClient.deleteEntityWithResponse&#40;someTableEntity, true&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Table entity with partition key '%s' and&quot;
     *             + &quot; row key '%s' was deleted.&quot;, response.getStatusCode&#40;&#41;, &quot;partitionKey&quot;, &quot;rowKey&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.deleteEntityWithResponse#TableEntity -->
     *
     * @param entity The table {@link TableEntity entity} to delete.
     * @param ifUnchanged When true, the ETag of the provided {@link TableEntity entity} must match the ETag of the
     * {@link TableEntity entity} in the Table service. If the values do not match, the update will not occur and an
     * exception will be thrown.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     * @throws IllegalArgumentException If 'partitionKey' or 'rowKey' is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged) {
        return withContext(context -> deleteEntityWithResponse(entity.getPartitionKey(), entity.getRowKey(),
            entity.getETag(), ifUnchanged, context));
    }

    Mono<Response<Void>> deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, boolean ifUnchanged,
                                                  Context context) {
        eTag = ifUnchanged ? eTag : "*";

        if (partitionKey == null || rowKey == null) {
            return monoError(logger, new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null."));
        }

        try {
            return tablesImplementation.getTables().deleteEntityWithResponseAsync(tableName,
            TableUtils.escapeSingleQuotes(partitionKey), TableUtils.escapeSingleQuotes(rowKey), eTag, null, null, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(TableServiceException.class, e -> swallowExceptionForStatusCode(404, e, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all {@link TableEntity entities} within the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link TableEntity entities} on the table. Prints out the details of the
     * retrieved {@link TableEntity entities}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.listEntities -->
     * <pre>
     * tableAsyncClient.listEntities&#40;&#41;
     *     .subscribe&#40;tableEntity -&gt;
     *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.%n&quot;,
     *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.listEntities -->
     *
     * @return A {@link PagedFlux} containing all {@link TableEntity entities} within the table.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities() {
        return listEntities(new ListEntitiesOptions());
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions -->
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
     * tableAsyncClient.listEntities&#40;listEntitiesOptions&#41;
     *     .subscribe&#40;tableEntity -&gt; &#123;
     *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s', row key '%s' and properties:%n&quot;,
     *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;;
     *
     *         tableEntity.getProperties&#40;&#41;.forEach&#40;&#40;key, value&#41; -&gt;
     *             System.out.printf&#40;&quot;Name: '%s'. Value: '%s'.%n&quot;, key, value&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions -->
     *
     * @param options The {@code filter}, {@code select}, and {@code top} OData query options to apply to this
     * operation.
     *
     * @return A {@link PagedFlux} containing matching {@link TableEntity entities} within the table.
     *
     * @throws IllegalArgumentException If one or more of the OData query options in {@code options} is malformed.
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TableEntity> listEntities(ListEntitiesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listEntitiesFirstPage(context, options, TableEntity.class)),
            token -> withContext(context -> listEntitiesNextPage(token, context, options, TableEntity.class)));
    }

    PagedFlux<TableEntity> listEntities(ListEntitiesOptions options, Context context, Duration timeout) {
        return new PagedFlux<>(
            () -> applyOptionalTimeout(listEntitiesFirstPage(context, options, TableEntity.class), timeout),
            token -> applyOptionalTimeout(listEntitiesNextPage(token, context, options, TableEntity.class), timeout));
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesFirstPage(Context context,
                                                                                 ListEntitiesOptions options,
                                                                                 Class<T> resultType) {
        return listEntities(null, null, context, options, resultType);
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntitiesNextPage(String token, Context context,
                                                                                ListEntitiesOptions options,
                                                                                Class<T> resultType) {
        if (token == null) {
            return Mono.empty();
        }

        try {
            String[] split = TableUtils.getKeysFromToken(token);
            return listEntities(split[0], split[1], context, options, resultType);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private <T extends TableEntity> Mono<PagedResponse<T>> listEntities(String nextPartitionKey, String nextRowKey,
                                                                        Context context, ListEntitiesOptions options,
                                                                        Class<T> resultType) {
        String select = null;

        if (options.getSelect() != null) {
            select = String.join(",", options.getSelect());
        }

        QueryOptions queryOptions = new QueryOptions()
            .setFilter(options.getFilter())
            .setTop(options.getTop())
            .setSelect(select)
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        try {
            return tablesImplementation.getTables().queryEntitiesWithResponseAsync(tableName, null, null,
                    nextPartitionKey, nextRowKey, queryOptions, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .flatMap(response -> {
                    final TableEntityQueryResponse tablesQueryEntityResponse = response.getValue();

                    if (tablesQueryEntityResponse == null) {
                        return Mono.empty();
                    }

                    final List<Map<String, Object>> entityResponseValue = tablesQueryEntityResponse.getValue();

                    if (entityResponseValue == null) {
                        return Mono.empty();
                    }

                    final List<T> entities = entityResponseValue.stream()
                        .map(TableEntityAccessHelper::createEntity)
                        .map(e -> EntityHelper.convertToSubclass(e, resultType, logger))
                        .collect(Collectors.toList());

                    return Mono.just(new EntityPaged<>(response, entities,
                        response.getDeserializedHeaders().getXMsContinuationNextPartitionKey(),
                        response.getDeserializedHeaders().getXMsContinuationNextRowKey()));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the retrieved
     * {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.getEntity#String-String -->
     * <pre>
     * tableAsyncClient.getEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;tableEntity -&gt;
     *         System.out.printf&#40;&quot;Retrieved entity with partition key '%s' and row key '%s'.&quot;,
     *             tableEntity.getPartitionKey&#40;&#41;, tableEntity.getRowKey&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.getEntity#String-String -->
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The partition key of the {@link TableEntity entity}.
     *
     * @return A {@link Mono} containing the {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null} or
     * empty.
     * @throws TableServiceException If no {@link TableEntity entity} with the provided {@code partitionKey} and
     * {@code rowKey} exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableEntity> getEntity(String partitionKey, String rowKey) {
        return getEntityWithResponse(partitionKey, rowKey, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a single {@link TableEntity entity} from the table.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets an {@link TableEntity entity} on the table. Prints out the details of the {@link Response HTTP response}
     * retrieved {@link TableEntity entity}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.getEntityWithResponse#String-String-ListEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; propertiesToSelect = new ArrayList&lt;&gt;&#40;&#41;;
     * propertiesToSelect.add&#40;&quot;name&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;lastname&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;age&quot;&#41;;
     *
     * tableAsyncClient.getEntityWithResponse&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;, propertiesToSelect&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         TableEntity tableEntity = response.getValue&#40;&#41;;
     *
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved entity with partition key '%s',&quot;
     *                 + &quot; row key '%s' and properties:&quot;, response.getStatusCode&#40;&#41;, tableEntity.getPartitionKey&#40;&#41;,
     *             tableEntity.getRowKey&#40;&#41;&#41;;
     *
     *         tableEntity.getProperties&#40;&#41;.forEach&#40;&#40;key, value&#41; -&gt;
     *             System.out.printf&#40;&quot;%nName: '%s'. Value: '%s'.&quot;, key, value&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.getEntityWithResponse#String-String-ListEntitiesOptions -->
     *
     * @param partitionKey The partition key of the {@link TableEntity entity}.
     * @param rowKey The partition key of the {@link TableEntity entity}.
     * @param select A list of properties to select on the {@link TableEntity entity}.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} that in turn contains the
     * {@link TableEntity entity}.
     *
     * @throws IllegalArgumentException If the provided {@code partitionKey} or {@code rowKey} are {@code null}
     * or if the {@code select} OData query option is malformed.
     * @throws TableServiceException If no {@link TableEntity entity} with the provided {@code partitionKey} and
     * {@code rowKey} exists within the table.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableEntity>> getEntityWithResponse(String partitionKey, String rowKey, List<String> select) {
        return withContext(context -> getEntityWithResponse(partitionKey, rowKey, select, TableEntity.class, context));
    }

    <T extends TableEntity> Mono<Response<T>> getEntityWithResponse(String partitionKey, String rowKey,
                                                                    List<String> select, Class<T> resultType,
                                                                    Context context) {
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

        if (select != null) {
            queryOptions.setSelect(String.join(",", select));
        }

        if (partitionKey == null || rowKey == null) {
            return monoError(logger, new IllegalArgumentException("'partitionKey' and 'rowKey' cannot be null."));
        }

        try {
            return tablesImplementation.getTables().queryEntityWithPartitionAndRowKeyWithResponseAsync(tableName,
                    TableUtils.escapeSingleQuotes(partitionKey), TableUtils.escapeSingleQuotes(rowKey), null, null, queryOptions, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .handle((response, sink) -> {
                    final Map<String, Object> matchingEntity = response.getValue();

                    if (matchingEntity == null || matchingEntity.isEmpty()) {
                        logger.info("There was no matching entity. Table: {}, partition key: {}, row key: {}.",
                            tableName, partitionKey, rowKey);

                        sink.complete();

                        return;
                    }

                    // Deserialize the first entity.
                    final TableEntity entity = TableEntityAccessHelper.createEntity(matchingEntity);
                    sink.next(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), EntityHelper.convertToSubclass(entity, resultType, logger)));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.getAccessPolicies -->
     * <pre>
     * tableAsyncClient.getAccessPolicies&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;accessPolicies -&gt;
     *         accessPolicies.getIdentifiers&#40;&#41;.forEach&#40;signedIdentifier -&gt;
     *             System.out.printf&#40;&quot;Retrieved table access policy with id '%s'.&quot;, signedIdentifier.getId&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.getAccessPolicies -->
     *
     * @return A {@link Mono} containing the table's {@link TableAccessPolicies access policies}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableAccessPolicies> getAccessPolicies() {
        return withContext(context -> getAccessPoliciesWithResponse(context)
            .flatMap(response -> Mono.justOrEmpty(response.getValue())));
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.getAccessPoliciesWithResponse -->
     * <pre>
     * List&lt;String&gt; propertiesToSelect = new ArrayList&lt;&gt;&#40;&#41;;
     * propertiesToSelect.add&#40;&quot;name&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;lastname&quot;&#41;;
     * propertiesToSelect.add&#40;&quot;age&quot;&#41;;
     *
     * tableAsyncClient.getAccessPoliciesWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved table access policies with the&quot;
     *             + &quot; following IDs:&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         response.getValue&#40;&#41;.getIdentifiers&#40;&#41;.forEach&#40;signedIdentifier -&gt;
     *             System.out.printf&#40;&quot;%n%s&quot;, signedIdentifier.getId&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.getAccessPoliciesWithResponse -->
     *
     * @return A {@link Mono} containing an {@link Response HTTP response} that in turn contains the table's
     * {@link TableAccessPolicies access policies}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableAccessPolicies>> getAccessPoliciesWithResponse() {
        return withContext(this::getAccessPoliciesWithResponse);
    }

    Mono<Response<TableAccessPolicies>> getAccessPoliciesWithResponse(Context context) {
        try {
            return tablesImplementation.getTables()
                .getAccessPolicyWithResponseAsync(tableName, null, null, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response,
                    new TableAccessPolicies(response.getValue() == null ? null : response.getValue().items().stream()
                        .map(TableUtils::toTableSignedIdentifier)
                        .collect(Collectors.toList()))));
        } catch (RuntimeException e) {
            return monoError(logger, e);
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.setAccessPolicies#List -->
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
     * tableAsyncClient.setAccessPolicies&#40;signedIdentifiers&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;unused -&gt; System.out.print&#40;&quot;Set table access policies.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.setAccessPolicies#List -->
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     *
     * @return An empty {@link Mono}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setAccessPolicies(List<TableSignedIdentifier> tableSignedIdentifiers) {
        return this.setAccessPoliciesWithResponse(tableSignedIdentifiers).flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.setAccessPoliciesWithResponse#List -->
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
     * tableAsyncClient.setAccessPoliciesWithResponse&#40;mySignedIdentifiers&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Set table access policies successfully with status code: %d.&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.setAccessPoliciesWithResponse#List -->
     *
     * @param tableSignedIdentifiers The {@link TableSignedIdentifier access policies} for the table.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response}.
     *
     * @throws TableServiceException If the request is rejected by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers) {
        return withContext(context -> this.setAccessPoliciesWithResponse(tableSignedIdentifiers, context));
    }

    Mono<Response<Void>> setAccessPoliciesWithResponse(List<TableSignedIdentifier> tableSignedIdentifiers,
                                                       Context context) {
        List<SignedIdentifier> signedIdentifiers = null;

        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
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

        try {
            return tablesImplementation.getTables()
                .setAccessPolicyWithResponseAsync(tableName, null, null, signedIdentifiers, context)
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .map(response -> new SimpleResponse<>(response, response.getValue()));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Executes all {@link TableTransactionAction actions} within the list inside a transaction. When the call
     * completes, either all {@link TableTransactionAction actions} in the transaction will succeed, or if a failure
     * occurs, all {@link TableTransactionAction actions} in the transaction will be rolled back. Each
     * {@link TableTransactionAction action} must operate on a distinct row key. Attempting to pass multiple
     * {@link TableTransactionAction actions} that share the same row key will cause an error.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Submits a transaction that contains multiple {@link TableTransactionAction actions} to be applied to
     * {@link TableEntity entities} on a table. Prints out details of each {@link TableTransactionAction action}'s
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.submitTransaction#List -->
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
     * tableAsyncClient.submitTransaction&#40;transactionActions&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;tableTransactionResult -&gt; &#123;
     *         System.out.print&#40;&quot;Submitted transaction. The ordered response status codes for the actions are:&quot;&#41;;
     *
     *         tableTransactionResult.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.submitTransaction#List -->
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * <!-- src_embed com.azure.data.tables.tableClient.submitTransactionWithError#List -->
     * <pre>
     * try &#123;
     *     TableTransactionResult transactionResult = tableClient.submitTransaction&#40;transactionActions&#41;;
     *
     *     System.out.print&#40;&quot;Submitted transaction. The ordered response status codes for the actions are:&quot;&#41;;
     *
     *     transactionResult.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *         System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * &#125; catch &#40;TableTransactionFailedException e&#41; &#123;
     *     &#47;&#47; If the transaction fails, the resulting exception contains the index of the first action that failed.
     *     int failedActionIndex = e.getFailedTransactionActionIndex&#40;&#41;;
     *     &#47;&#47; You can use this index to modify the offending action or remove it from the list of actions to send in
     *     &#47;&#47; the transaction, for example.
     *     transactionActions.remove&#40;failedActionIndex&#41;;
     *     &#47;&#47; And then retry submitting the transaction.
     * &#125;
     * </pre>
     * <!-- end com.azure.data.tables.tableClient.submitTransactionWithError#List -->
     *
     * @param transactionActions A {@link List} of {@link TableTransactionAction actions} to perform on
     * {@link TableEntity entities} in a table.
     *
     * @return A {@link Mono} containing a {@link List} of {@link TableTransactionActionResponse sub-responses} that
     * correspond to each {@link TableTransactionAction action} in the transaction.
     *
     * @throws IllegalArgumentException If no {@link TableTransactionAction actions} have been added to the list.
     * @throws TableServiceException If the request is rejected by the service.
     * @throws TableTransactionFailedException If any {@link TableTransactionResult action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableTransactionResult> submitTransaction(List<TableTransactionAction> transactionActions) {
        return submitTransactionWithResponse(transactionActions)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Executes all {@link TableTransactionAction actions} within the list inside a transaction. When the call
     * completes, either all {@link TableTransactionAction actions} in the transaction will succeed, or if a failure
     * occurs, all {@link TableTransactionAction actions} in the transaction will be rolled back. Each
     * {@link TableTransactionAction action} must operate on a distinct row key. Attempting to pass multiple
     * {@link TableTransactionAction actions} that share the same row key will cause an error.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Submits a transaction that contains multiple {@link TableTransactionAction actions} to be applied to
     * {@link TableEntity entities} on a table. Prints out details of the {@link Response HTTP response} for the
     * operation, as well as each {@link TableTransactionAction action}'s corresponding {@link Response HTTP
     * response}.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.submitTransactionWithResponse#List -->
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
     * tableAsyncClient.submitTransactionWithResponse&#40;myTransactionActions&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. The ordered response status codes of the&quot;
     *             + &quot; submitted actions are:&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         response.getValue&#40;&#41;.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.submitTransactionWithResponse#List -->
     * <p>Shows how to handle a transaction with a failing {@link TableTransactionAction action} via the provided
     * {@link TableTransactionFailedException exception}, which contains the index of the first failing action in the
     * transaction.</p>
     * <!-- src_embed com.azure.data.tables.tableAsyncClient.submitTransactionWithResponseWithError#List -->
     * <pre>
     * tableAsyncClient.submitTransactionWithResponse&#40;myTransactionActions&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .doOnError&#40;TableTransactionFailedException.class, e -&gt; &#123;
     *         &#47;&#47; If the transaction fails, the resulting exception contains the index of the first action that failed.
     *         int failedActionIndex = e.getFailedTransactionActionIndex&#40;&#41;;
     *         &#47;&#47; You can use this index to modify the offending action or remove it from the list of actions to send
     *         &#47;&#47; in the transaction, for example.
     *         transactionActions.remove&#40;failedActionIndex&#41;;
     *         &#47;&#47; And then retry submitting the transaction.
     *     &#125;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. The ordered response status codes of the&quot;
     *             + &quot; submitted actions are:&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         response.getValue&#40;&#41;.getTransactionActionResponses&#40;&#41;.forEach&#40;tableTransactionActionResponse -&gt;
     *             System.out.printf&#40;&quot;%n%d&quot;, tableTransactionActionResponse.getStatusCode&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.tables.tableAsyncClient.submitTransactionWithResponseWithError#List -->
     *
     * @param transactionActions A {@link List} of {@link TableTransactionAction transaction actions} to perform on
     * {@link TableEntity entities} in a table.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} produced for the transaction itself. The
     * response's value will contain a {@link List} of {@link TableTransactionActionResponse sub-responses} that
     * correspond to each {@link TableTransactionAction action} in the transaction.
     *
     * @throws IllegalArgumentException If no {@link TableTransactionAction actions} have been added to the list.
     * @throws TableServiceException If the request is rejected by the service.
     * @throws TableTransactionFailedException If any {@link TableTransactionAction action} within the transaction
     * fails. See the documentation for the client methods in {@link TableClient} to understand the conditions that
     * may cause a given {@link TableTransactionAction action} to fail.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TableTransactionResult>> submitTransactionWithResponse(List<TableTransactionAction> transactionActions) {
        return withContext(context -> submitTransactionWithResponse(transactionActions, context));
    }

    Mono<Response<TableTransactionResult>> submitTransactionWithResponse(List<TableTransactionAction> transactionActions, Context context) {
        if (transactionActions.isEmpty()) {
            return monoError(logger,
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

        try {
            return Flux.fromIterable(operations)
                .flatMapSequential(op -> op.prepareRequest(transactionalBatchClient).zipWith(Mono.just(op)))
                .collect(TransactionalBatchRequestBody::new, (body, pair) ->
                    body.addChangeOperation(new TransactionalBatchSubRequest(pair.getT2(), pair.getT1())))
                .publishOn(Schedulers.boundedElastic())
                .flatMap(body ->
                    transactionalBatchImplementation.submitTransactionalBatchWithRestResponseAsync(body, null,
                        context).zipWith(Mono.just(body)))
                .onErrorMap(TableUtils::mapThrowableToTableServiceException)
                .flatMap(pair -> parseResponse(pair.getT2(), pair.getT1()))
                .map(response ->
                    new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        new TableTransactionResult(transactionActions, response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private Mono<Response<List<TableTransactionActionResponse>>> parseResponse(TransactionalBatchRequestBody requestBody,
        ResponseBase<TransactionalBatchSubmitBatchHeaders, TableTransactionActionResponse[]> response) {
        TableServiceJsonError error = null;
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
                if (subResponse.getValue() instanceof TableServiceJsonError) {
                    error = (TableServiceJsonError) subResponse.getValue();

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

            return monoError(logger,
                new TableTransactionFailedException(message, null, toTableServiceError(error), failedIndex));
        } else {
            return Mono.just(new SimpleResponse<>(response, Arrays.asList(response.getValue())));
        }
    }
}
