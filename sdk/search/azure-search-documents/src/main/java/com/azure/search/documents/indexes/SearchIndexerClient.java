// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.CreateOrUpdateDataSourceConnectionOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateIndexerOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateSkillsetOptions;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;

import java.util.Objects;

/**
 * This class provides a client that contains the operations for creating, getting, listing, updating, or deleting data
 * source connections, indexers, or skillsets and running or resetting indexers in an Azure Cognitive Search service.
 *
 * @see SearchIndexerClientBuilder
 */
@ServiceClient(builder = SearchIndexerClientBuilder.class)
public class SearchIndexerClient {
    private final SearchIndexerAsyncClient asyncClient;

    SearchIndexerClient(SearchIndexerAsyncClient searchIndexerAsyncClient) {
        this.asyncClient = searchIndexerAsyncClient;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.asyncClient.getHttpPipeline();
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.asyncClient.getEndpoint();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = searchIndexerClient.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *
     * SearchIndexerDataSourceConnection updateDataSource = searchIndexerClient.createOrUpdateDataSourceConnection&#40;dataSource&#41;;
     * System.out.printf&#40;&quot;The dataSource name is %s. The container name of dataSource is %s.%n&quot;,
     *     updateDataSource.getName&#40;&#41;, updateDataSource.getContainer&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection -->
     *
     * @param dataSourceConnection The definition of the data source to create or update.
     * @return the data source that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerDataSourceConnection createOrUpdateDataSourceConnection(
        SearchIndexerDataSourceConnection dataSourceConnection) {
        return createOrUpdateDataSourceConnectionWithResponse(dataSourceConnection, false, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = searchIndexerClient.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *
     * Response&lt;SearchIndexerDataSourceConnection&gt; updateDataSource = searchIndexerClient
     *     .createOrUpdateDataSourceConnectionWithResponse&#40;dataSource, true, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe dataSource name is %s. &quot;
     *     + &quot;The container name of dataSource is %s.%n&quot;, updateDataSource.getStatusCode&#40;&#41;,
     *     updateDataSource.getValue&#40;&#41;.getName&#40;&#41;, updateDataSource.getValue&#40;&#41;.getContainer&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context -->
     *
     * @param dataSourceConnection the {@link SearchIndexerDataSourceConnection} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSourceConnection} is the same as the current
     * service value. {@code false} to always update existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerDataSourceConnection> createOrUpdateDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSourceConnection, boolean onlyIfUnchanged, Context context) {
        return asyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSourceConnection, onlyIfUnchanged, null,
            context).block();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions-Context -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = searchIndexerClient.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     * CreateOrUpdateDataSourceConnectionOptions options = new CreateOrUpdateDataSourceConnectionOptions&#40;dataSource&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     *
     * Response&lt;SearchIndexerDataSourceConnection&gt; updateDataSource = searchIndexerClient
     *     .createOrUpdateDataSourceConnectionWithResponse&#40;options, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe dataSource name is %s. &quot;
     *         + &quot;The container name of dataSource is %s.%n&quot;, updateDataSource.getStatusCode&#40;&#41;,
     *     updateDataSource.getValue&#40;&#41;.getName&#40;&#41;, updateDataSource.getValue&#40;&#41;.getContainer&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions-Context -->
     *
     * @param options The options used to create or update the {@link SearchIndexerDataSourceConnection data source
     * connection}.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a data source response.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerDataSourceConnection> createOrUpdateDataSourceConnectionWithResponse(
        CreateOrUpdateDataSourceConnectionOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        return asyncClient.createOrUpdateDataSourceConnectionWithResponse(options.getDataSourceConnection(),
            options.isOnlyIfUnchanged(), options.isCacheResetRequirementsIgnored(), context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer data source connection named "dataSource".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection&#40;&quot;dataSource&quot;,
     *     com.azure.search.documents.indexes.models.SearchIndexerDataSourceType.AZURE_BLOB, &quot;&#123;connectionString&#125;&quot;,
     *     new com.azure.search.documents.indexes.models.SearchIndexerDataContainer&#40;&quot;container&quot;&#41;&#41;;
     * SearchIndexerDataSourceConnection dataSourceFromService =
     *     searchIndexerClient.createDataSourceConnection&#40;dataSource&#41;;
     * System.out.printf&#40;&quot;The data source name is %s. The ETag of data source is %s.%n&quot;,
     *     dataSourceFromService.getName&#40;&#41;, dataSourceFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection -->
     *
     * @param dataSourceConnection The definition of the data source to create
     * @return the data source that was created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerDataSourceConnection createDataSourceConnection(
        SearchIndexerDataSourceConnection dataSourceConnection) {
        return createDataSourceConnectionWithResponse(dataSourceConnection, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-Context -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection&#40;&quot;dataSource&quot;,
     *     SearchIndexerDataSourceType.AZURE_BLOB, &quot;&#123;connectionString&#125;&quot;,
     *     new SearchIndexerDataContainer&#40;&quot;container&quot;&#41;&#41;;
     * Response&lt;SearchIndexerDataSourceConnection&gt; dataSourceFromService =
     *     searchIndexerClient.createDataSourceConnectionWithResponse&#40;dataSource, new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The data source name is %s.%n&quot;,
     *     dataSourceFromService.getStatusCode&#40;&#41;, dataSourceFromService.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-Context -->
     *
     * @param dataSourceConnection the definition of the data source to create doesn't match specified values
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerDataSourceConnection> createDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSourceConnection, Context context) {
        return asyncClient.createDataSourceConnectionWithResponse(dataSourceConnection, context)
            .block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource =
     *     searchIndexerClient.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * System.out.printf&#40;&quot;The dataSource name is %s. The ETag of dataSource is %s.%n&quot;, dataSource.getName&#40;&#41;,
     *     dataSource.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String -->
     *
     * @param dataSourceConnectionName the name of the data source to retrieve
     * @return the DataSource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerDataSourceConnection getDataSourceConnection(String dataSourceConnectionName) {
        return getDataSourceConnectionWithResponse(dataSourceConnectionName, Context.NONE).getValue();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndexerDataSourceConnection&gt; dataSource =
     *     searchIndexerClient.getDataSourceConnectionWithResponse&#40;
     *         &quot;dataSource&quot;, new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The data source name is %s.%n&quot;,
     *     dataSource.getStatusCode&#40;&#41;, dataSource.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context -->
     *
     * @param dataSourceConnectionName the name of the data source to retrieve
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the DataSource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerDataSourceConnection> getDataSourceConnectionWithResponse(
        String dataSourceConnectionName, Context context) {
        return asyncClient.getDataSourceConnectionWithResponse(dataSourceConnectionName, context)
            .block();
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connections. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections -->
     * <pre>
     * PagedIterable&lt;SearchIndexerDataSourceConnection&gt; dataSources = searchIndexerClient.listDataSourceConnections&#40;&#41;;
     * for &#40;SearchIndexerDataSourceConnection dataSource: dataSources&#41; &#123;
     *     System.out.printf&#40;&quot;The dataSource name is %s. The ETag of dataSource is %s.%n&quot;, dataSource.getName&#40;&#41;,
     *         dataSource.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections -->
     *
     * @return a list of DataSources
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexerDataSourceConnection> listDataSourceConnections() {
        return listDataSourceConnections(Context.NONE);
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connections. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;SearchIndexerDataSourceConnection&gt; dataSources =
     *     searchIndexerClient.listDataSourceConnections&#40;new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + dataSources.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;SearchIndexerDataSourceConnection dataSource: dataSources&#41; &#123;
     *     System.out.printf&#40;&quot;The dataSource name is %s. The ETag of dataSource is %s.%n&quot;,
     *         dataSource.getName&#40;&#41;, dataSource.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context -->
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response containing the list of DataSources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexerDataSourceConnection> listDataSourceConnections(Context context) {
        return new PagedIterable<>(asyncClient.listDataSourceConnections(context));
    }

    /**
     * List all DataSource names from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connection names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames -->
     * <pre>
     * PagedIterable&lt;String&gt; dataSources = searchIndexerClient.listDataSourceConnectionNames&#40;&#41;;
     * for &#40;String dataSourceName: dataSources&#41; &#123;
     *     System.out.printf&#40;&quot;The dataSource name is %s.%n&quot;, dataSourceName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames -->
     *
     * @return a list of DataSources names
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listDataSourceConnectionNames() {
        return listDataSourceConnectionNames(Context.NONE);
    }

    /**
     * List all DataSources names from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connection names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context -->
     * <pre>
     * PagedIterable&lt;String&gt; dataSources = searchIndexerClient.listDataSourceConnectionNames&#40;new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + dataSources.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;String dataSourceName: dataSources&#41; &#123;
     *     System.out.printf&#40;&quot;The dataSource name is %s.%n&quot;, dataSourceName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context -->
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response containing the list of DataSource names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listDataSourceConnectionNames(Context context) {
        return new PagedIterable<>(asyncClient.listDataSourceConnectionNames(context));
    }

    /**
     * Delete a DataSource
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete all search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String -->
     * <pre>
     * searchIndexerClient.deleteDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String -->
     *
     * @param dataSourceConnectionName the name of the data source to be deleted
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDataSourceConnection(String dataSourceConnectionName) {
        deleteDataSourceConnectionWithResponse(new SearchIndexerDataSourceConnection(dataSourceConnectionName), false,
            Context.NONE);
    }

    /**
     * Delete a DataSource with Response
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete all search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource =
     *     searchIndexerClient.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = searchIndexerClient.deleteDataSourceConnectionWithResponse&#40;dataSource, true,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context -->
     *
     * @param dataSourceConnection the {@link SearchIndexerDataSourceConnection} to be deleted.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSourceConnection} is the same as the current
     * service value. {@code false} to always delete existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return an empty response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection dataSourceConnection,
        boolean onlyIfUnchanged, Context context) {
        String eTag = onlyIfUnchanged ? dataSourceConnection.getETag() : null;
        return asyncClient.deleteDataSourceConnectionWithResponse(dataSourceConnection.getName(), eTag, context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createIndexer#SearchIndexer -->
     * <pre>
     * SearchIndexer searchIndexer = new SearchIndexer&#40;&quot;searchIndexer&quot;, &quot;dataSource&quot;,
     *     &quot;searchIndex&quot;&#41;;
     * SearchIndexer indexerFromService = searchIndexerClient.createIndexer&#40;searchIndexer&#41;;
     * System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexerFromService.getName&#40;&#41;,
     *     indexerFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createIndexer#SearchIndexer -->
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexer createIndexer(SearchIndexer indexer) {
        return createIndexerWithResponse(indexer, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-Context -->
     * <pre>
     * SearchIndexer searchIndexer = new SearchIndexer&#40;&quot;searchIndexer&quot;, &quot;dataSource&quot;,
     *     &quot;searchIndex&quot;&#41;;
     * Response&lt;SearchIndexer&gt; indexerFromServiceResponse = searchIndexerClient.createIndexerWithResponse&#40;
     *     searchIndexer, new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The indexer name is %s.%n&quot;,
     *     indexerFromServiceResponse.getStatusCode&#40;&#41;, indexerFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-Context -->
     *
     * @param indexer definition of the indexer to create
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexer> createIndexerWithResponse(SearchIndexer indexer, Context context) {
        return asyncClient.createIndexerWithResponse(indexer, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer -->
     * <pre>
     * SearchIndexer searchIndexerFromService = searchIndexerClient.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * SearchIndexer updateIndexer = searchIndexerClient.createOrUpdateIndexer&#40;searchIndexerFromService&#41;;
     * System.out.printf&#40;&quot;The indexer name is %s. The target field name of indexer is %s.%n&quot;,
     *     updateIndexer.getName&#40;&#41;, updateIndexer.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer -->
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexer createOrUpdateIndexer(SearchIndexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, false, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context -->
     * <pre>
     * SearchIndexer searchIndexerFromService = searchIndexerClient.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * Response&lt;SearchIndexer&gt; indexerFromService = searchIndexerClient.createOrUpdateIndexerWithResponse&#40;
     *     searchIndexerFromService, true, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer name is %s. &quot;
     *     + &quot;The target field name of indexer is %s.%n&quot;, indexerFromService.getStatusCode&#40;&#41;,
     *     indexerFromService.getValue&#40;&#41;.getName&#40;&#41;,
     *     indexerFromService.getValue&#40;&#41;.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context -->
     *
     * @param indexer The {@link SearchIndexer} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return A response object containing the Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexer> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        Context context) {
        return asyncClient.createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, null, null, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions-Context -->
     * <pre>
     * SearchIndexer searchIndexerFromService = searchIndexerClient.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * CreateOrUpdateIndexerOptions options = new CreateOrUpdateIndexerOptions&#40;searchIndexerFromService&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     * Response&lt;SearchIndexer&gt; indexerFromService = searchIndexerClient.createOrUpdateIndexerWithResponse&#40;
     *     options, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer name is %s. &quot;
     *         + &quot;The target field name of indexer is %s.%n&quot;, indexerFromService.getStatusCode&#40;&#41;,
     *     indexerFromService.getValue&#40;&#41;.getName&#40;&#41;,
     *     indexerFromService.getValue&#40;&#41;.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions-Context -->
     *
     * @param options The options used to create or update the {@link SearchIndexer indexer}.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return A response object containing the Indexer.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexer> createOrUpdateIndexerWithResponse(CreateOrUpdateIndexerOptions options,
        Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return asyncClient.createOrUpdateIndexerWithResponse(options.getIndexer(), options.isOnlyIfUnchanged(),
            options.isCacheReprocessingChangeDetectionDisabled(), options.isCacheResetRequirementsIgnored(), context)
            .block();
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexers. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listIndexers -->
     * <pre>
     * PagedIterable&lt;SearchIndexer&gt; indexers = searchIndexerClient.listIndexers&#40;&#41;;
     * for &#40;SearchIndexer indexer: indexers&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexer.getName&#40;&#41;,
     *         indexer.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listIndexers -->
     *
     * @return all Indexers from the Search service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexer> listIndexers() {
        return listIndexers(Context.NONE);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexers. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;SearchIndexer&gt; indexers = searchIndexerClient.listIndexers&#40;new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + indexers.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;SearchIndexer indexer: indexers&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer name is %s. The ETag of index is %s.%n&quot;,
     *         indexer.getName&#40;&#41;, indexer.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return all Indexers from the Search service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexer> listIndexers(Context context) {
        return new PagedIterable<>(asyncClient.listIndexers(context));
    }

    /**
     * Lists all indexers names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames -->
     * <pre>
     * PagedIterable&lt;String&gt; indexers = searchIndexerClient.listIndexerNames&#40;&#41;;
     * for &#40;String indexerName: indexers&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer name is %s.%n&quot;, indexerName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames -->
     *
     * @return all Indexer names from the Search service .
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listIndexerNames() {
        return listIndexerNames(Context.NONE);
    }

    /**
     * Lists all indexers names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context -->
     * <pre>
     * PagedIterable&lt;String&gt; indexers = searchIndexerClient.listIndexerNames&#40;new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + indexers.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;String indexerName: indexers&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer name is %s.%n&quot;, indexerName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return all Indexer names from the Search service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listIndexerNames(Context context) {
        return new PagedIterable<>(asyncClient.listIndexerNames(context));
    }

    /**
     * Retrieves an indexer definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer with name "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String -->
     * <pre>
     * SearchIndexer indexerFromService =
     *     searchIndexerClient.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexerFromService.getName&#40;&#41;,
     *     indexerFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String -->
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexer getIndexer(String indexerName) {
        return getIndexerWithResponse(indexerName, Context.NONE).getValue();
    }

    /**
     * Retrieves an indexer definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer with name "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndexer&gt; indexerFromServiceResponse = searchIndexerClient.getIndexerWithResponse&#40;
     *     &quot;searchIndexer&quot;, new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The indexer name is %s.%n&quot;,
     *     indexerFromServiceResponse.getStatusCode&#40;&#41;, indexerFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context -->
     *
     * @param indexerName the name of the indexer to retrieve
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexer> getIndexerWithResponse(String indexerName, Context context) {
        return asyncClient.getIndexerWithResponse(indexerName, context).block();
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String -->
     * <pre>
     * searchIndexerClient.deleteIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String -->
     *
     * @param indexerName the name of the indexer to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteIndexer(String indexerName) {
        deleteIndexerWithResponse(new SearchIndexer(indexerName), false, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search index named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context -->
     * <pre>
     * SearchIndexer searchIndexer = searchIndexerClient.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = searchIndexerClient.deleteIndexerWithResponse&#40;searchIndexer, true,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context -->
     *
     * @param indexer the search {@link SearchIndexer}
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param context the context
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged, Context context) {
        String eTag = onlyIfUnchanged ? indexer.getETag() : null;
        return asyncClient.deleteIndexerWithResponse(indexer.getName(), eTag, context).block();
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Reset search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String -->
     * <pre>
     * searchIndexerClient.resetIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String -->
     *
     * @param indexerName the name of the indexer to reset
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resetIndexer(String indexerName) {
        resetIndexerWithResponse(indexerName, Context.NONE);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Reset search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context -->
     * <pre>
     * Response&lt;Void&gt; response = searchIndexerClient.resetIndexerWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context -->
     *
     * @param indexerName the name of the indexer to reset
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetIndexerWithResponse(String indexerName, Context context) {
        return asyncClient.resetIndexerWithResponse(indexerName, context).block();
    }

    /**
     * Runs an indexer on-demand.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Run search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String -->
     * <pre>
     * searchIndexerClient.runIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String -->
     *
     * @param indexerName the name of the indexer to run
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void runIndexer(String indexerName) {
        runIndexerWithResponse(indexerName, Context.NONE);
    }

    /**
     * Runs an indexer on-demand.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Run search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context -->
     * <pre>
     * Response&lt;Void&gt; response = searchIndexerClient.runIndexerWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context -->
     *
     * @param indexerName the name of the indexer to run
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> runIndexerWithResponse(String indexerName, Context context) {
        return asyncClient.runIndexerWithResponse(indexerName, context).block();
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer status.  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String -->
     * <pre>
     * SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus&#40;&quot;searchIndexer&quot;&#41;;
     * System.out.printf&#40;&quot;The indexer status is %s.%n&quot;, indexerStatus.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String -->
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return a response with the indexer execution info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerStatus getIndexerStatus(String indexerName) {
        return getIndexerStatusWithResponse(indexerName, Context.NONE).getValue();
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer status.  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndexerStatus&gt; response = searchIndexerClient.getIndexerStatusWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer status is %s.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context -->
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response with the indexer execution info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerStatus> getIndexerStatusWithResponse(String indexerName, Context context) {
        return asyncClient.getIndexerStatusWithResponse(indexerName, context).block();
    }


    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset -->
     * <pre>
     * List&lt;InputFieldMappingEntry&gt; inputs = Collections.singletonList&#40;
     *     new InputFieldMappingEntry&#40;&quot;image&quot;&#41;
     *         .setSource&#40;&quot;&#47;document&#47;normalized_images&#47;*&quot;&#41;
     * &#41;;
     *
     * List&lt;OutputFieldMappingEntry&gt; outputs = Arrays.asList&#40;
     *     new OutputFieldMappingEntry&#40;&quot;text&quot;&#41;
     *         .setTargetName&#40;&quot;mytext&quot;&#41;,
     *     new OutputFieldMappingEntry&#40;&quot;layoutText&quot;&#41;
     *         .setTargetName&#40;&quot;myLayoutText&quot;&#41;
     * &#41;;
     * SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset&#40;&quot;searchIndexerSkillset&quot;,
     *     Collections.singletonList&#40;new OcrSkill&#40;inputs, outputs&#41;
     *         .setShouldDetectOrientation&#40;true&#41;
     *         .setDefaultLanguageCode&#40;null&#41;
     *         .setName&#40;&quot;myocr&quot;&#41;
     *         .setDescription&#40;&quot;Extracts text &#40;plain and structured&#41; from image.&quot;&#41;
     *         .setContext&#40;&quot;&#47;document&#47;normalized_images&#47;*&quot;&#41;&#41;&#41;;
     * SearchIndexerSkillset skillset = searchIndexerClient.createSkillset&#40;searchIndexerSkillset&#41;;
     * System.out.printf&#40;&quot;The indexer skillset name is %s. The ETag of indexer skillset is %s.%n&quot;,
     *     skillset.getName&#40;&#41;, skillset.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset -->
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created SearchIndexerSkillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerSkillset createSkillset(SearchIndexerSkillset skillset) {
        return createSkillsetWithResponse(skillset, Context.NONE).getValue();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-Context -->
     * <pre>
     * List&lt;InputFieldMappingEntry&gt; inputs = Collections.singletonList&#40;
     *     new InputFieldMappingEntry&#40;&quot;image&quot;&#41;
     *         .setSource&#40;&quot;&#47;document&#47;normalized_images&#47;*&quot;&#41;
     * &#41;;
     *
     * List&lt;OutputFieldMappingEntry&gt; outputs = Arrays.asList&#40;
     *     new OutputFieldMappingEntry&#40;&quot;text&quot;&#41;
     *         .setTargetName&#40;&quot;mytext&quot;&#41;,
     *     new OutputFieldMappingEntry&#40;&quot;layoutText&quot;&#41;
     *         .setTargetName&#40;&quot;myLayoutText&quot;&#41;
     * &#41;;
     * SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset&#40;&quot;searchIndexerSkillset&quot;,
     *     Collections.singletonList&#40;new OcrSkill&#40;inputs, outputs&#41;
     *         .setShouldDetectOrientation&#40;true&#41;
     *         .setDefaultLanguageCode&#40;null&#41;
     *         .setName&#40;&quot;myocr&quot;&#41;
     *         .setDescription&#40;&quot;Extracts text &#40;plain and structured&#41; from image.&quot;&#41;
     *         .setContext&#40;&quot;&#47;document&#47;normalized_images&#47;*&quot;&#41;&#41;&#41;;
     * Response&lt;SearchIndexerSkillset&gt; skillsetWithResponse =
     *     searchIndexerClient.createSkillsetWithResponse&#40;searchIndexerSkillset, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s. The indexer skillset name is %s.%n&quot;,
     *     skillsetWithResponse.getStatusCode&#40;&#41;, skillsetWithResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-Context -->
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SearchIndexerSkillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerSkillset> createSkillsetWithResponse(SearchIndexerSkillset skillset, Context context) {
        return asyncClient.createSkillsetWithResponse(skillset, context).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String -->
     * <pre>
     * SearchIndexerSkillset indexerSkillset =
     *     searchIndexerClient.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * System.out.printf&#40;&quot;The indexer skillset name is %s. The ETag of indexer skillset is %s.%n&quot;,
     *     indexerSkillset.getName&#40;&#41;, indexerSkillset.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String -->
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the SearchIndexerSkillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerSkillset getSkillset(String skillsetName) {
        return getSkillsetWithResponse(skillsetName, Context.NONE).getValue();
    }

    /**
     * Retrieves a skillset definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndexerSkillset&gt; skillsetWithResponse = searchIndexerClient.getSkillsetWithResponse&#40;
     *     &quot;searchIndexerSkillset&quot;, new Context&#40;key1, value1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The indexer skillset name is %s.%n&quot;,
     *     skillsetWithResponse.getStatusCode&#40;&#41;, skillsetWithResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context -->
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SearchIndexerSkillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerSkillset> getSkillsetWithResponse(String skillsetName, Context context) {
        return asyncClient.getSkillsetWithResponse(skillsetName, context).block();
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillsets. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets -->
     * <pre>
     * PagedIterable&lt;SearchIndexerSkillset&gt; indexerSkillsets = searchIndexerClient.listSkillsets&#40;&#41;;
     * for &#40;SearchIndexerSkillset skillset: indexerSkillsets&#41; &#123;
     *     System.out.printf&#40;&quot;The skillset name is %s. The ETag of skillset is %s.%n&quot;, skillset.getName&#40;&#41;,
     *         skillset.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets -->
     *
     * @return the list of skillsets.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexerSkillset> listSkillsets() {
        return listSkillsets(Context.NONE);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillsets. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context -->
     * <pre>
     * PagedIterable&lt;SearchIndexerSkillset&gt; indexerSkillsets = searchIndexerClient.listSkillsets&#40;new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + indexerSkillsets.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;SearchIndexerSkillset skillset: indexerSkillsets&#41; &#123;
     *     System.out.printf&#40;&quot;The skillset name is %s. The ETag of skillset is %s.%n&quot;,
     *         skillset.getName&#40;&#41;, skillset.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of skillsets.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndexerSkillset> listSkillsets(Context context) {
        return new PagedIterable<>(asyncClient.listSkillsets(context));
    }

    /**
     * Lists all skillset names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillset names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames -->
     * <pre>
     * PagedIterable&lt;String&gt; skillsetNames = searchIndexerClient.listSkillsetNames&#40;&#41;;
     * for &#40;String skillsetName: skillsetNames&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer skillset name is %s.%n&quot;, skillsetName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames -->
     *
     * @return the list of skillset names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSkillsetNames() {
        return listSkillsetNames(Context.NONE);
    }

    /**
     * Lists all skillset names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillset names with response. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;String&gt; skillsetNames = searchIndexerClient.listSkillsetNames&#40;new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + skillsetNames.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;String skillsetName: skillsetNames&#41; &#123;
     *     System.out.printf&#40;&quot;The indexer skillset name is %s.%n&quot;, skillsetName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of skillset names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSkillsetNames(Context context) {
        return new PagedIterable<>(asyncClient.listSkillsetNames(context));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset -->
     * <pre>
     * SearchIndexerSkillset indexerSkillset = searchIndexerClient.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * SearchIndexerSkillset updateSkillset = searchIndexerClient.createOrUpdateSkillset&#40;indexerSkillset&#41;;
     * System.out.printf&#40;&quot;The indexer skillset name is %s. The description of indexer skillset is %s.%n&quot;,
     *     updateSkillset.getName&#40;&#41;, updateSkillset.getDescription&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset -->
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @return the skillset that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexerSkillset createOrUpdateSkillset(SearchIndexerSkillset skillset) {
        return createOrUpdateSkillsetWithResponse(skillset, false, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context -->
     * <pre>
     * SearchIndexerSkillset indexerSkillset = searchIndexerClient.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * Response&lt;SearchIndexerSkillset&gt; updateSkillsetResponse = searchIndexerClient.createOrUpdateSkillsetWithResponse&#40;
     *     indexerSkillset, true, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer skillset name is %s. &quot;
     *         + &quot;The description of indexer skillset is %s.%n&quot;, updateSkillsetResponse.getStatusCode&#40;&#41;,
     *     updateSkillsetResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     updateSkillsetResponse.getValue&#40;&#41;.getDescription&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context -->
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerSkillset> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, Context context) {
        return asyncClient.createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, null, null, context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions-Context -->
     * <pre>
     * SearchIndexerSkillset indexerSkillset = searchIndexerClient.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * CreateOrUpdateSkillsetOptions options = new CreateOrUpdateSkillsetOptions&#40;indexerSkillset&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     * Response&lt;SearchIndexerSkillset&gt; updateSkillsetResponse = searchIndexerClient.createOrUpdateSkillsetWithResponse&#40;
     *     options, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer skillset name is %s. &quot;
     *         + &quot;The description of indexer skillset is %s.%n&quot;, updateSkillsetResponse.getStatusCode&#40;&#41;,
     *     updateSkillsetResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     updateSkillsetResponse.getValue&#40;&#41;.getDescription&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions-Context -->
     *
     * @param options The options used to create or update the {@link SearchIndexerSkillset skillset}.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexerSkillset> createOrUpdateSkillsetWithResponse(CreateOrUpdateSkillsetOptions options,
        Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        return asyncClient.createOrUpdateSkillsetWithResponse(options.getSkillset(), options.isOnlyIfUnchanged(),
            options.isCacheReprocessingChangeDetectionDisabled(), options.isCacheResetRequirementsIgnored(), context)
            .block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String -->
     * <pre>
     * searchIndexerClient.deleteSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String -->
     *
     * @param skillsetName the name of the skillset to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSkillset(String skillsetName) {
        deleteSkillsetWithResponse(new SearchIndexerSkillset(skillsetName), false, Context.NONE);
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context -->
     * <pre>
     * SearchIndexerSkillset searchIndexerSkillset = searchIndexerClient.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = searchIndexerClient.deleteSkillsetWithResponse&#40;searchIndexerSkillset, true,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context -->
     *
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSkillsetWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged,
        Context context) {
        String eTag = onlyIfUnchanged ? skillset.getETag() : null;
        return asyncClient.deleteSkillsetWithResponse(skillset.getName(), eTag, context).block();
    }

}
