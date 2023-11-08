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
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImpl;
import com.azure.search.documents.indexes.implementation.models.DocumentKeysOrIds;
import com.azure.search.documents.indexes.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.implementation.models.ListIndexersResult;
import com.azure.search.documents.indexes.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.implementation.models.SkillNames;
import com.azure.search.documents.indexes.models.CreateOrUpdateDataSourceConnectionOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateIndexerOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateSkillsetOptions;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;

import java.util.List;
import java.util.Objects;

/**
 * This class provides a client that contains the operations for creating, getting, listing, updating, or deleting data
 * source connections, indexers, or skillsets and running or resetting indexers in an Azure Cognitive Search service.
 *
 * @see SearchIndexerClientBuilder
 */
@ServiceClient(builder = SearchIndexerClientBuilder.class)
public class SearchIndexerClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerClient.class);

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchIndexerClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.restClient = new SearchServiceClientImpl(httpPipeline, endpoint, serviceVersion.getVersion());
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
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
     * SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *
     * SearchIndexerDataSourceConnection updateDataSource = SEARCH_INDEXER_CLIENT
     *     .createOrUpdateDataSourceConnection&#40;dataSource&#41;;
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
     * SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *
     * Response&lt;SearchIndexerDataSourceConnection&gt; updateDataSource = SEARCH_INDEXER_CLIENT
     *     .createOrUpdateDataSourceConnectionWithResponse&#40;dataSource, true, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return createOrUpdateDataSourceConnectionWithResponse(dataSourceConnection, onlyIfUnchanged, null,
            context);
    }

    Response<SearchIndexerDataSourceConnection> createOrUpdateDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource, boolean onlyIfUnchanged, Boolean ignoreResetRequirements,
        Context context) {
        if (dataSource == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'dataSource' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        if (dataSource.getConnectionString() == null) {
            dataSource.setConnectionString("<unchanged>");
        }
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDataSources()
                .createOrUpdateWithResponse(dataSource.getName(), dataSource, ifMatch, null, ignoreResetRequirements,
                    null, Utility.enableSyncRestProxy(context)));
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
     * SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     * CreateOrUpdateDataSourceConnectionOptions options = new CreateOrUpdateDataSourceConnectionOptions&#40;dataSource&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     *
     * Response&lt;SearchIndexerDataSourceConnection&gt; updateDataSource = SEARCH_INDEXER_CLIENT
     *     .createOrUpdateDataSourceConnectionWithResponse&#40;options, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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

        return createOrUpdateDataSourceConnectionWithResponse(options.getDataSourceConnection(),
            options.isOnlyIfUnchanged(), options.isCacheResetRequirementsIgnored(), context);
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
     *     SEARCH_INDEXER_CLIENT.createDataSourceConnection&#40;dataSource&#41;;
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
     *     SEARCH_INDEXER_CLIENT.createDataSourceConnectionWithResponse&#40;dataSource, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDataSources()
            .createWithResponse(dataSourceConnection, null, Utility.enableSyncRestProxy(context)));
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
     *     SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
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
     *     SEARCH_INDEXER_CLIENT.getDataSourceConnectionWithResponse&#40;
     *         &quot;dataSource&quot;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDataSources()
            .getWithResponse(dataSourceConnectionName, null, Utility.enableSyncRestProxy(context)));
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
     * PagedIterable&lt;SearchIndexerDataSourceConnection&gt; dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnections&#40;&#41;;
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
     *     SEARCH_INDEXER_CLIENT.listDataSourceConnections&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingDataSource(listDataSourceConnectionsWithResponse(null, context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private Response<ListDataSourcesResult> listDataSourceConnectionsWithResponse(String select,
                                                                                        Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDataSources()
            .listWithResponse(select, null, Utility.enableSyncRestProxy(context)));
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
     * PagedIterable&lt;String&gt; dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames&#40;&#41;;
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
     * PagedIterable&lt;String&gt; dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingDataSourceNames(this.listDataSourceConnectionsWithResponse("name", context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
     * SEARCH_INDEXER_CLIENT.deleteDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
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
     *     SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = SEARCH_INDEXER_CLIENT.deleteDataSourceConnectionWithResponse&#40;dataSource, true,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDataSources()
            .deleteWithResponse(dataSourceConnection.getName(), eTag, null, null, Utility.enableSyncRestProxy(context)));
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
     * SearchIndexer indexerFromService = SEARCH_INDEXER_CLIENT.createIndexer&#40;searchIndexer&#41;;
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
     * Response&lt;SearchIndexer&gt; indexerFromServiceResponse = SEARCH_INDEXER_CLIENT.createIndexerWithResponse&#40;
     *     searchIndexer, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .createWithResponse(indexer, null, Utility.enableSyncRestProxy(context)));
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
     * SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * SearchIndexer updateIndexer = SEARCH_INDEXER_CLIENT.createOrUpdateIndexer&#40;searchIndexerFromService&#41;;
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
     * SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * Response&lt;SearchIndexer&gt; indexerFromService = SEARCH_INDEXER_CLIENT.createOrUpdateIndexerWithResponse&#40;
     *     searchIndexerFromService, true, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, null, null, context);
    }

    Response<SearchIndexer> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        Boolean disableCacheReprocessingChangeDetection, Boolean ignoreResetRequirements, Context context) {
        if (indexer == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'indexer' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .createOrUpdateWithResponse(indexer.getName(), indexer, ifMatch, null,
                disableCacheReprocessingChangeDetection, ignoreResetRequirements, null,
                Utility.enableSyncRestProxy(context)));

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
     * SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *     new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     * CreateOrUpdateIndexerOptions options = new CreateOrUpdateIndexerOptions&#40;searchIndexerFromService&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     * Response&lt;SearchIndexer&gt; indexerFromService = SEARCH_INDEXER_CLIENT.createOrUpdateIndexerWithResponse&#40;
     *     options, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return createOrUpdateIndexerWithResponse(options.getIndexer(), options.isOnlyIfUnchanged(),
            options.isCacheReprocessingChangeDetectionDisabled(), options.isCacheResetRequirementsIgnored(), context);
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
     * PagedIterable&lt;SearchIndexer&gt; indexers = SEARCH_INDEXER_CLIENT.listIndexers&#40;&#41;;
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
     * PagedIterable&lt;SearchIndexer&gt; indexers = SEARCH_INDEXER_CLIENT.listIndexers&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingSearchIndexer(
                listIndexersWithResponse(null, context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private Response<ListIndexersResult> listIndexersWithResponse(String select, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .listWithResponse(select, null, Utility.enableSyncRestProxy(context)));
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
     * PagedIterable&lt;String&gt; indexers = SEARCH_INDEXER_CLIENT.listIndexerNames&#40;&#41;;
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
     * PagedIterable&lt;String&gt; indexers = SEARCH_INDEXER_CLIENT.listIndexerNames&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingSearchIndexerNames(
                this.listIndexersWithResponse("name", context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
     *     SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
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
     * Response&lt;SearchIndexer&gt; indexerFromServiceResponse = SEARCH_INDEXER_CLIENT.getIndexerWithResponse&#40;
     *     &quot;searchIndexer&quot;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
                .getWithResponse(indexerName, null, Utility.enableSyncRestProxy(context)));
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
     * SEARCH_INDEXER_CLIENT.deleteIndexer&#40;&quot;searchIndexer&quot;&#41;;
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
     * SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = SEARCH_INDEXER_CLIENT.deleteIndexerWithResponse&#40;searchIndexer, true,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .deleteWithResponse(indexer.getName(), eTag, null, null, Utility.enableSyncRestProxy(context)));
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
     * SEARCH_INDEXER_CLIENT.resetIndexer&#40;&quot;searchIndexer&quot;&#41;;
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
     * Response&lt;Void&gt; response = SEARCH_INDEXER_CLIENT.resetIndexerWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .resetWithResponse(indexerName, null, Utility.enableSyncRestProxy(context)));
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
     * SEARCH_INDEXER_CLIENT.runIndexer&#40;&quot;searchIndexer&quot;&#41;;
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
     * Response&lt;Void&gt; response = SEARCH_INDEXER_CLIENT.runIndexerWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers().runWithResponse(indexerName, null, Utility.enableSyncRestProxy(context)));
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
     * SearchIndexerStatus indexerStatus = SEARCH_INDEXER_CLIENT.getIndexerStatus&#40;&quot;searchIndexer&quot;&#41;;
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
     * Response&lt;SearchIndexerStatus&gt; response = SEARCH_INDEXER_CLIENT.getIndexerStatusWithResponse&#40;&quot;searchIndexer&quot;,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexers()
            .getStatusWithResponse(indexerName, null, Utility.enableSyncRestProxy(context)));
    }

    /**
     * Resets specific documents in the datasource to be selectively re-ingested by the indexer.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-List-List -->
     * <pre>
     * &#47;&#47; Reset the documents with keys 1234 and 4321.
     * SEARCH_INDEXER_CLIENT.resetDocuments&#40;&quot;searchIndexer&quot;, false, Arrays.asList&#40;&quot;1234&quot;, &quot;4321&quot;&#41;, null&#41;;
     *
     * &#47;&#47; Clear the previous documents to be reset and replace them with documents 1235 and 5231.
     * SEARCH_INDEXER_CLIENT.resetDocuments&#40;&quot;searchIndexer&quot;, true, Arrays.asList&#40;&quot;1235&quot;, &quot;5321&quot;&#41;, null&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-List-List -->
     *
     * @param indexerName The name of the indexer to reset documents for.
     * @param overwrite If false, keys or IDs will be appended to existing ones. If true, only the keys or IDs in this
     * payload will be queued to be re-ingested.
     * @param documentKeys Document keys to be reset.
     * @param datasourceDocumentIds Datasource document identifiers to be reset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resetDocuments(String indexerName, Boolean overwrite, List<String> documentKeys,
        List<String> datasourceDocumentIds) {
        resetDocumentsWithResponse(new SearchIndexer(indexerName), overwrite, documentKeys, datasourceDocumentIds,
            Context.NONE);
    }

    /**
     * Resets specific documents in the datasource to be selectively re-ingested by the indexer.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List-Context -->
     * <pre>
     * SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;;
     *
     * &#47;&#47; Reset the documents with keys 1234 and 4321.
     * Response&lt;Void&gt; resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse&#40;searchIndexer, false,
     *     Arrays.asList&#40;&quot;1234&quot;, &quot;4321&quot;&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;Requesting documents to be reset completed with status code %d.%n&quot;,
     *     resetDocsResult.getStatusCode&#40;&#41;&#41;;
     *
     * &#47;&#47; Clear the previous documents to be reset and replace them with documents 1235 and 5231.
     * resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse&#40;searchIndexer, true,
     *     Arrays.asList&#40;&quot;1235&quot;, &quot;5321&quot;&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;Overwriting the documents to be reset completed with status code %d.%n&quot;,
     *     resetDocsResult.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List-Context -->
     *
     * @param indexer The indexer to reset documents for.
     * @param overwrite If false, keys or IDs will be appended to existing ones. If true, only the keys or IDs in this
     * payload will be queued to be re-ingested.
     * @param documentKeys Document keys to be reset.
     * @param datasourceDocumentIds Datasource document identifiers to be reset.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return A response signalling completion.
     * @throws NullPointerException If {@code indexer} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetDocumentsWithResponse(SearchIndexer indexer, Boolean overwrite,
        List<String> documentKeys, List<String> datasourceDocumentIds, Context context) {
        try {
            DocumentKeysOrIds documentKeysOrIds = new DocumentKeysOrIds()
                .setDocumentKeys(documentKeys)
                .setDatasourceDocumentIds(datasourceDocumentIds);
            return restClient.getIndexers().resetDocsWithResponse(indexer.getName(), overwrite, documentKeysOrIds, null,
                Utility.enableSyncRestProxy(context));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
     * SearchIndexerSkillset skillset = SEARCH_INDEXER_CLIENT.createSkillset&#40;searchIndexerSkillset&#41;;
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
     *     SEARCH_INDEXER_CLIENT.createSkillsetWithResponse&#40;searchIndexerSkillset, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        if (skillset == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'skillset' cannot be null."));
        }
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSkillsets()
            .createWithResponse(skillset, null, Utility.enableSyncRestProxy(context)));
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
     *     SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
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
     * Response&lt;SearchIndexerSkillset&gt; skillsetWithResponse = SEARCH_INDEXER_CLIENT.getSkillsetWithResponse&#40;
     *     &quot;searchIndexerSkillset&quot;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSkillsets()
            .getWithResponse(skillsetName, null, Utility.enableSyncRestProxy(context)));
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
     * PagedIterable&lt;SearchIndexerSkillset&gt; indexerSkillsets = SEARCH_INDEXER_CLIENT.listSkillsets&#40;&#41;;
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
     * PagedIterable&lt;SearchIndexerSkillset&gt; indexerSkillsets = SEARCH_INDEXER_CLIENT
     *     .listSkillsets&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingSkillset(
                listSkillsetsWithResponse(null, context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private Response<ListSkillsetsResult> listSkillsetsWithResponse(String select, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> this.restClient.getSkillsets()
            .listWithResponse(select, null, Utility.enableSyncRestProxy(context)));
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
     * PagedIterable&lt;String&gt; skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames&#40;&#41;;
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
     * PagedIterable&lt;String&gt; skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        try {
            return new PagedIterable<>(() -> MappingUtils.mappingPagingSkillsetNames(
                listSkillsetsWithResponse("name", context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
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
     * SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * SearchIndexerSkillset updateSkillset = SEARCH_INDEXER_CLIENT.createOrUpdateSkillset&#40;indexerSkillset&#41;;
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
     * SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * Response&lt;SearchIndexerSkillset&gt; updateSkillsetResponse = SEARCH_INDEXER_CLIENT.createOrUpdateSkillsetWithResponse&#40;
     *     indexerSkillset, true, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, null, null, context);
    }

    Response<SearchIndexerSkillset> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, Boolean disableCacheReprocessingChangeDetection, Boolean ignoreResetRequirements,
        Context context) {
        if (skillset == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'skillset' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? skillset.getETag() : null;
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSkillsets()
            .createOrUpdateWithResponse(skillset.getName(), skillset, ifMatch, null,
                disableCacheReprocessingChangeDetection, ignoreResetRequirements, null,
                Utility.enableSyncRestProxy(context)));
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
     * SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     * CreateOrUpdateSkillsetOptions options = new CreateOrUpdateSkillsetOptions&#40;indexerSkillset&#41;
     *     .setOnlyIfUnchanged&#40;true&#41;
     *     .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *     .setCacheResetRequirementsIgnored&#40;true&#41;;
     * Response&lt;SearchIndexerSkillset&gt; updateSkillsetResponse = SEARCH_INDEXER_CLIENT.createOrUpdateSkillsetWithResponse&#40;
     *     options, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return createOrUpdateSkillsetWithResponse(options.getSkillset(), options.isOnlyIfUnchanged(),
            options.isCacheReprocessingChangeDetectionDisabled(), options.isCacheResetRequirementsIgnored(), context);
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
     * SEARCH_INDEXER_CLIENT.deleteSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
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
     * SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = SEARCH_INDEXER_CLIENT.deleteSkillsetWithResponse&#40;searchIndexerSkillset, true,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
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
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSkillsets()
            .deleteWithResponse(skillset.getName(), eTag, null, null, Utility.enableSyncRestProxy(context)));
    }

    /**
     * Resets skills in an existing skillset in an Azure Cognitive Search service.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-List -->
     * <pre>
     * &#47;&#47; Reset the &quot;myOcr&quot; and &quot;myText&quot; skills.
     * SEARCH_INDEXER_CLIENT.resetSkills&#40;&quot;searchIndexerSkillset&quot;, Arrays.asList&#40;&quot;myOcr&quot;, &quot;myText&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-List -->
     *
     * @param skillsetName The name of the skillset to reset.
     * @param skillNames The skills to reset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resetSkills(String skillsetName, List<String> skillNames) {
        resetSkillsWithResponse(new SearchIndexerSkillset(skillsetName), skillNames, Context.NONE);
    }

    /**
     * Resets skills in an existing skillset in an Azure Cognitive Search service.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#SearchIndexerSkillset-List-Context -->
     * <pre>
     * SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;;
     *
     * &#47;&#47; Reset the &quot;myOcr&quot; and &quot;myText&quot; skills.
     * Response&lt;Void&gt; resetSkillsResponse = SEARCH_INDEXER_CLIENT.resetSkillsWithResponse&#40;searchIndexerSkillset,
     *     Arrays.asList&#40;&quot;myOcr&quot;, &quot;myText&quot;&#41;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;Resetting skills completed with status code %d.%n&quot;, resetSkillsResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#SearchIndexerSkillset-List-Context -->
     *
     * @param skillset The skillset to reset.
     * @param skillNames The skills to reset.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A response signalling completion.
     * @throws NullPointerException If {@code skillset} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetSkillsWithResponse(SearchIndexerSkillset skillset, List<String> skillNames,
        Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSkillsets()
            .resetSkillsWithResponse(skillset.getName(), new SkillNames().setSkillNames(skillNames), null,
                Utility.enableSyncRestProxy(context)));
    }
}
