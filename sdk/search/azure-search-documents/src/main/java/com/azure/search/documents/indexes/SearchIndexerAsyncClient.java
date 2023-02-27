// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.util.MappingUtils;
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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains the operations for creating, getting, listing, updating, or deleting data
 * source connections, indexers, or skillsets and running or resetting indexers in an Azure Cognitive Search service.
 *
 * @see SearchIndexerClientBuilder
 */
@ServiceClient(builder = SearchIndexerClientBuilder.class, isAsync = true)
public class SearchIndexerAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerAsyncClient.class);

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

    SearchIndexerAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
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
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;;
     * dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *
     * SearchIndexerDataSourceConnection updateDataSource = SEARCH_INDEXER_CLIENT
     *     .createOrUpdateDataSourceConnection&#40;dataSource&#41;;
     * System.out.printf&#40;&quot;The dataSource name is %s. The container name of dataSource is %s.%n&quot;,
     *     updateDataSource.getName&#40;&#41;, updateDataSource.getContainer&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection -->
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSourceConnection} to create or update.
     * @return the data source that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerDataSourceConnection> createOrUpdateDataSourceConnection(
        SearchIndexerDataSourceConnection dataSource) {
        return createOrUpdateDataSourceConnectionWithResponse(dataSource, false).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;
     *     .flatMap&#40;dataSource -&gt; &#123;
     *         dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateDataSourceConnectionWithResponse&#40;dataSource, true&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updateDataSource -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe dataSource name is %s. &quot;
     *             + &quot;The container name of dataSource is %s.%n&quot;, updateDataSource.getStatusCode&#40;&#41;,
     *         updateDataSource.getValue&#40;&#41;.getName&#40;&#41;, updateDataSource.getValue&#40;&#41;.getContainer&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean -->
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSourceConnection} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a data source response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerDataSourceConnection>> createOrUpdateDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource, boolean onlyIfUnchanged) {
        return withContext(context ->
            createOrUpdateDataSourceConnectionWithResponse(dataSource, onlyIfUnchanged, null, context));
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;
     *     .flatMap&#40;dataSource -&gt; &#123;
     *         dataSource.setContainer&#40;new SearchIndexerDataContainer&#40;&quot;updatecontainer&quot;&#41;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateDataSourceConnectionWithResponse&#40;
     *             new CreateOrUpdateDataSourceConnectionOptions&#40;dataSource&#41;
     *                 .setOnlyIfUnchanged&#40;true&#41;
     *                 .setCacheResetRequirementsIgnored&#40;true&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updateDataSource -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe dataSource name is %s. &quot;
     *                 + &quot;The container name of dataSource is %s.%n&quot;, updateDataSource.getStatusCode&#40;&#41;,
     *             updateDataSource.getValue&#40;&#41;.getName&#40;&#41;, updateDataSource.getValue&#40;&#41;.getContainer&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions -->
     *
     * @param options The options used to create or update the {@link SearchIndexerDataSourceConnection data source
     * connection}.
     * @return a data source response.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerDataSourceConnection>> createOrUpdateDataSourceConnectionWithResponse(
        CreateOrUpdateDataSourceConnectionOptions options) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return withContext(context -> createOrUpdateDataSourceConnectionWithResponse(options.getDataSourceConnection(),
            options.isOnlyIfUnchanged(), options.isCacheResetRequirementsIgnored(), context));
    }

    Mono<Response<SearchIndexerDataSourceConnection>> createOrUpdateDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource, boolean onlyIfUnchanged, Boolean ignoreResetRequirements,
        Context context) {
        if (dataSource == null) {
            return monoError(LOGGER, new NullPointerException("'dataSource' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        if (dataSource.getConnectionString() == null) {
            dataSource.setConnectionString("<unchanged>");
        }
        try {
            return restClient.getDataSources()
                .createOrUpdateWithResponseAsync(dataSource.getName(), dataSource, ifMatch, null,
                    ignoreResetRequirements, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer data source connection named "dataSource".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection&#40;&quot;dataSource&quot;,
     *     com.azure.search.documents.indexes.models.SearchIndexerDataSourceType.AZURE_BLOB, &quot;&#123;connectionString&#125;&quot;,
     *     new com.azure.search.documents.indexes.models.SearchIndexerDataContainer&#40;&quot;container&quot;&#41;&#41;;
     * SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnection&#40;dataSource&#41;
     *     .subscribe&#40;dataSourceFromService -&gt;
     *         System.out.printf&#40;&quot;The data source name is %s. The ETag of data source is %s.%n&quot;,
     *             dataSourceFromService.getName&#40;&#41;, dataSourceFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection -->
     *
     * @param dataSource The definition of the dataSource to create.
     * @return a Mono which performs the network request upon subscription.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerDataSourceConnection> createDataSourceConnection(
        SearchIndexerDataSourceConnection dataSource) {
        return createDataSourceConnectionWithResponse(dataSource).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection -->
     * <pre>
     * SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection&#40;&quot;dataSource&quot;,
     *     SearchIndexerDataSourceType.AZURE_BLOB, &quot;&#123;connectionString&#125;&quot;,
     *     new SearchIndexerDataContainer&#40;&quot;container&quot;&#41;&#41;;
     * SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnectionWithResponse&#40;dataSource&#41;
     *     .subscribe&#40;dataSourceFromService -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The data source name is %s.%n&quot;,
     *         dataSourceFromService.getStatusCode&#40;&#41;, dataSourceFromService.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection -->
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSourceConnection} to create.
     * @return a Mono which performs the network request upon subscription.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerDataSourceConnection>> createDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource) {
        return withContext(context -> this.createDataSourceConnectionWithResponse(dataSource, context));
    }

    Mono<Response<SearchIndexerDataSourceConnection>> createDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource, Context context) {
        try {
            return restClient.getDataSources()
                .createWithResponseAsync(dataSource, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnection#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;
     *     .subscribe&#40;dataSource -&gt;
     *         System.out.printf&#40;&quot;The dataSource name is %s. The ETag of dataSource is %s.%n&quot;, dataSource.getName&#40;&#41;,
     *         dataSource.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnection#String -->
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSourceConnection} to retrieve.
     * @return the DataSource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerDataSourceConnection> getDataSourceConnection(String dataSourceName) {
        return getDataSourceConnectionWithResponse(dataSourceName).map(Response::getValue);
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnectionWithResponse&#40;&quot;dataSource&quot;&#41;
     *     .subscribe&#40;dataSource -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The data source name is %s.%n&quot;,
     *         dataSource.getStatusCode&#40;&#41;, dataSource.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String -->
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSourceConnection} to retrieve.
     * @return a response containing the DataSource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerDataSourceConnection>> getDataSourceConnectionWithResponse(
        String dataSourceName) {
        return withContext(context -> getDataSourceConnectionWithResponse(dataSourceName, context));
    }

    Mono<Response<SearchIndexerDataSourceConnection>> getDataSourceConnectionWithResponse(String dataSourceName,
        Context context) {
        try {
            return restClient.getDataSources()
                .getWithResponseAsync(dataSourceName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connections. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listDataSourceConnections&#40;&#41;
     *     .subscribe&#40;dataSource -&gt;
     *         System.out.printf&#40;&quot;The dataSource name is %s. The ETag of dataSource is %s.%n&quot;,
     *             dataSource.getName&#40;&#41;, dataSource.getETag&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections -->
     *
     * @return a list of DataSources
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndexerDataSourceConnection> listDataSourceConnections() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourceConnectionsWithResponse(null, context))
                    .map(MappingUtils::mappingPagingDataSource));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * List all DataSource names from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connection names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnectionNames -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listDataSourceConnectionNames&#40;&#41;
     *     .subscribe&#40;dataSourceName -&gt; System.out.printf&#40;&quot;The dataSource name is %s.%n&quot;, dataSourceName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnectionNames -->
     *
     * @return a list of DataSource names
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listDataSourceConnectionNames() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourceConnectionsWithResponse("name", context))
                    .map(MappingUtils::mappingPagingDataSourceNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    private Mono<Response<ListDataSourcesResult>> listDataSourceConnectionsWithResponse(String select,
        Context context) {
        return restClient.getDataSources()
            .listWithResponseAsync(select, null, context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Delete a DataSource
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete the search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnection#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.deleteDataSourceConnection&#40;&quot;dataSource&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnection#String -->
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSourceConnection} for deletion
     * @return a void Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDataSourceConnection(String dataSourceName) {
        return withContext(context -> deleteDataSourceConnectionWithResponse(dataSourceName, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search data source.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete the search indexer data source connection named "dataSource". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection&#40;&quot;dataSource&quot;&#41;
     *     .flatMap&#40;dataSource -&gt; SEARCH_INDEXER_ASYNC_CLIENT.deleteDataSourceConnectionWithResponse&#40;dataSource, true&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean -->
     *
     * @param dataSource The {@link SearchIndexerDataSourceConnection} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a mono response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection dataSource,
        boolean onlyIfUnchanged) {
        if (dataSource == null) {
            return monoError(LOGGER, new NullPointerException("'dataSource' cannot be null."));
        }
        String eTag = onlyIfUnchanged ? dataSource.getETag() : null;
        return withContext(context -> deleteDataSourceConnectionWithResponse(dataSource.getName(), eTag, context));
    }

    Mono<Response<Void>> deleteDataSourceConnectionWithResponse(String dataSourceName, String eTag, Context context) {
        try {
            return restClient.getDataSources()
                .deleteWithResponseAsync(dataSourceName, eTag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexer#SearchIndexer -->
     * <pre>
     * SearchIndexer searchIndexer = new SearchIndexer&#40;&quot;searchIndexer&quot;, &quot;dataSource&quot;,
     *     &quot;searchIndex&quot;&#41;;
     * SEARCH_INDEXER_ASYNC_CLIENT.createIndexer&#40;searchIndexer&#41;
     *     .subscribe&#40;indexerFromService -&gt;
     *         System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexerFromService.getName&#40;&#41;,
     *         indexerFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexer#SearchIndexer -->
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexer> createIndexer(SearchIndexer indexer) {
        return createIndexerWithResponse(indexer).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer -->
     * <pre>
     * SearchIndexer searchIndexer = new SearchIndexer&#40;&quot;searchIndexer&quot;, &quot;dataSource&quot;,
     *     &quot;searchIndex&quot;&#41;;
     * SEARCH_INDEXER_ASYNC_CLIENT.createIndexerWithResponse&#40;searchIndexer&#41;
     *     .subscribe&#40;indexerFromServiceResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The indexer name is %s.%n&quot;,
     *             indexerFromServiceResponse.getStatusCode&#40;&#41;, indexerFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer -->
     *
     * @param indexer definition of the indexer to create
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexer>> createIndexerWithResponse(SearchIndexer indexer) {
        return withContext(context -> createIndexerWithResponse(indexer, context));
    }

    Mono<Response<SearchIndexer>> createIndexerWithResponse(SearchIndexer indexer, Context context) {
        try {
            return restClient.getIndexers()
                .createWithResponseAsync(indexer, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexer#SearchIndexer -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .flatMap&#40;searchIndexerFromService -&gt; &#123;
     *         searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *             new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexer&#40;searchIndexerFromService&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updatedIndexer -&gt;
     *         System.out.printf&#40;&quot;The indexer name is %s. The target field name of indexer is %s.%n&quot;,
     *         updatedIndexer.getName&#40;&#41;, updatedIndexer.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexer#SearchIndexer -->
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexer> createOrUpdateIndexer(SearchIndexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, false).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .flatMap&#40;searchIndexerFromService -&gt; &#123;
     *         searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *             new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexerWithResponse&#40;searchIndexerFromService, true&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;indexerFromService -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer name is %s. &quot;
     *             + &quot;The target field name of indexer is %s.%n&quot;, indexerFromService.getStatusCode&#40;&#41;,
     *         indexerFromService.getValue&#40;&#41;.getName&#40;&#41;,
     *         indexerFromService.getValue&#40;&#41;.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean -->
     *
     * @param indexer the definition of the {@link SearchIndexer} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer,
        boolean onlyIfUnchanged) {
        return withContext(context -> createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, null, null, context));
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .flatMap&#40;searchIndexerFromService -&gt; &#123;
     *         searchIndexerFromService.setFieldMappings&#40;Collections.singletonList&#40;
     *             new FieldMapping&#40;&quot;hotelName&quot;&#41;.setTargetFieldName&#40;&quot;HotelName&quot;&#41;&#41;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexerWithResponse&#40;
     *             new CreateOrUpdateIndexerOptions&#40;searchIndexerFromService&#41;
     *                 .setOnlyIfUnchanged&#40;true&#41;
     *                 .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *                 .setCacheResetRequirementsIgnored&#40;true&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;indexerFromService -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer name is %s. &quot;
     *                 + &quot;The target field name of indexer is %s.%n&quot;, indexerFromService.getStatusCode&#40;&#41;,
     *             indexerFromService.getValue&#40;&#41;.getName&#40;&#41;,
     *             indexerFromService.getValue&#40;&#41;.getFieldMappings&#40;&#41;.get&#40;0&#41;.getTargetFieldName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions -->
     *
     * @param options The options used to create or update the {@link SearchIndexer indexer}.
     * @return a response containing the created Indexer.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(CreateOrUpdateIndexerOptions options) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return withContext(context -> createOrUpdateIndexerWithResponse(options.getIndexer(),
            options.isOnlyIfUnchanged(), options.isCacheReprocessingChangeDetectionDisabled(),
            options.isCacheResetRequirementsIgnored(), context));
    }

    Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        Boolean disableCacheReprocessingChangeDetection, Boolean ignoreResetRequirements, Context context) {
        if (indexer == null) {
            return monoError(LOGGER, new NullPointerException("'indexer' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        try {
            return restClient.getIndexers()
                .createOrUpdateWithResponseAsync(indexer.getName(), indexer, ifMatch, null,
                    disableCacheReprocessingChangeDetection, ignoreResetRequirements, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an indexer definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer with name "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexer#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;indexerFromService -&gt;
     *         System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexerFromService.getName&#40;&#41;,
     *             indexerFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexer#String -->
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexer> getIndexer(String indexerName) {
        return getIndexerWithResponse(indexerName).map(Response::getValue);
    }

    /**
     * Retrieves an indexer definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer with name "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexerWithResponse&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;indexerFromServiceResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The indexer name is %s.%n&quot;,
     *         indexerFromServiceResponse.getStatusCode&#40;&#41;, indexerFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String -->
     *
     * @param indexerName the name of the indexer to retrieve
     * @return a response containing the indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName) {
        return withContext(context -> getIndexerWithResponse(indexerName, context));
    }

    Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName, Context context) {
        try {
            return restClient.getIndexers()
                .getWithResponseAsync(indexerName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexers. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listIndexers&#40;&#41;
     *     .subscribe&#40;indexer -&gt;
     *         System.out.printf&#40;&quot;The indexer name is %s. The ETag of indexer is %s.%n&quot;, indexer.getName&#40;&#41;,
     *         indexer.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers -->
     *
     * @return a response containing all Indexers from the Search service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndexer> listIndexers() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse(null, context))
                    .map(MappingUtils::mappingPagingSearchIndexer));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexerNames -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listIndexerNames&#40;&#41;
     *     .subscribe&#40;indexerName -&gt; System.out.printf&#40;&quot;The indexer name is %s.%n&quot;, indexerName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexerNames -->
     *
     * @return a response containing all Indexers from the Search service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listIndexerNames() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse("name", context))
                    .map(MappingUtils::mappingPagingSearchIndexerNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    private Mono<Response<ListIndexersResult>> listIndexersWithResponse(String select, Context context) {
        return restClient.getIndexers()
            .listWithResponseAsync(select, null, context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer named "searchIndexer". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexer#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.deleteIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexer#String -->
     *
     * @param indexerName the name of the indexer to delete
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteIndexer(String indexerName) {
        return withContext(context -> deleteIndexerWithResponse(indexerName, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#SearchIndexer-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .flatMap&#40;searchIndexer -&gt;
     *         SEARCH_INDEXER_ASYNC_CLIENT.deleteIndexerWithResponse&#40;searchIndexer, true&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#SearchIndexer-boolean -->
     *
     * @param indexer the {@link SearchIndexer} to delete
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged) {
        if (indexer == null) {
            return monoError(LOGGER, new NullPointerException("'indexer' cannot be null."));
        }
        String eTag = onlyIfUnchanged ? indexer.getETag() : null;
        return withContext(context -> deleteIndexerWithResponse(indexer.getName(), eTag, context));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param eTag Optional. The eTag to match.
     * @param context the context
     * @return a response signalling completion.
     */
    Mono<Response<Void>> deleteIndexerWithResponse(String indexerName, String eTag, Context context) {
        try {
            return restClient.getIndexers()
                .deleteWithResponseAsync(indexerName, eTag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Reset search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexer#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.resetIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexer#String -->
     *
     * @param indexerName the name of the indexer to reset
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetIndexer(String indexerName) {
        return resetIndexerWithResponse(indexerName).flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Reset search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.resetIndexerWithResponse&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String -->
     *
     * @param indexerName the name of the indexer to reset
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resetIndexerWithResponse(String indexerName) {
        return withContext(context -> resetIndexerWithResponse(indexerName, context));
    }

    Mono<Response<Void>> resetIndexerWithResponse(String indexerName, Context context) {
        try {
            return restClient.getIndexers()
                .resetWithResponseAsync(indexerName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Runs an indexer on-demand.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Run search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexer#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.runIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexer#String -->
     *
     * @param indexerName the name of the indexer to run
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> runIndexer(String indexerName) {
        return runIndexerWithResponse(indexerName).flatMap(FluxUtil::toMono);
    }

    /**
     * Runs an indexer on-demand.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Run search indexer named "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.runIndexerWithResponse&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String -->
     *
     * @param indexerName the name of the indexer to run
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> runIndexerWithResponse(String indexerName) {
        return withContext(context -> runIndexerWithResponse(indexerName, context));
    }

    Mono<Response<Void>> runIndexerWithResponse(String indexerName, Context context) {
        try {
            return restClient.getIndexers().runWithResponseAsync(indexerName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get status for search indexer "searchIndexer".  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatus#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexerStatus&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;indexerStatus -&gt;
     *         System.out.printf&#40;&quot;The indexer status is %s.%n&quot;, indexerStatus.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatus#String -->
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return the indexer execution info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerStatus> getIndexerStatus(String indexerName) {
        return getIndexerStatusWithResponse(indexerName).map(Response::getValue);
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer status.  </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexerStatusWithResponse&#40;&quot;searchIndexer&quot;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer status is %s.%n&quot;,
     *         response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String -->
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return a response with the indexer execution info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerStatus>> getIndexerStatusWithResponse(String indexerName) {
        return withContext(context -> getIndexerStatusWithResponse(indexerName, context));
    }

    Mono<Response<SearchIndexerStatus>> getIndexerStatusWithResponse(String indexerName, Context context) {
        try {
            return restClient.getIndexers()
                .getStatusWithResponseAsync(indexerName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Resets specific documents in the datasource to be selectively re-ingested by the indexer.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-List-List -->
     * <pre>
     * &#47;&#47; Reset the documents with keys 1234 and 4321.
     * SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments&#40;&quot;searchIndexer&quot;, false, Arrays.asList&#40;&quot;1234&quot;, &quot;4321&quot;&#41;, null&#41;
     *     &#47;&#47; Clear the previous documents to be reset and replace them with documents 1235 and 5231.
     *     .then&#40;SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments&#40;&quot;searchIndexer&quot;, true, Arrays.asList&#40;&quot;1235&quot;, &quot;5321&quot;&#41;, null&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-List-List -->
     *
     * @param indexerName The name of the indexer to reset documents for.
     * @param overwrite If false, keys or IDs will be appended to existing ones. If true, only the keys or IDs in this
     * payload will be queued to be re-ingested.
     * @param documentKeys Document keys to be reset.
     * @param datasourceDocumentIds Datasource document identifiers to be reset.
     * @return A response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetDocuments(String indexerName, Boolean overwrite, List<String> documentKeys,
        List<String> datasourceDocumentIds) {
        return withContext(context -> resetDocumentsWithResponse(indexerName, overwrite, documentKeys,
            datasourceDocumentIds, context))
            .map(Response::getValue);
    }

    /**
     * Resets specific documents in the datasource to be selectively re-ingested by the indexer.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getIndexer&#40;&quot;searchIndexer&quot;&#41;
     *     .flatMap&#40;searchIndexer -&gt; SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse&#40;searchIndexer, false,
     *         Arrays.asList&#40;&quot;1234&quot;, &quot;4321&quot;&#41;, null&#41;
     *         .flatMap&#40;resetDocsResult -&gt; &#123;
     *             System.out.printf&#40;&quot;Requesting documents to be reset completed with status code %d.%n&quot;,
     *                 resetDocsResult.getStatusCode&#40;&#41;&#41;;
     *
     *             &#47;&#47; Clear the previous documents to be reset and replace them with documents 1235 and 5231.
     *             return SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse&#40;searchIndexer, true,
     *                 Arrays.asList&#40;&quot;1235&quot;, &quot;5321&quot;&#41;, null&#41;;
     *         &#125;&#41;&#41;
     *     .subscribe&#40;resetDocsResult -&gt;
     *         System.out.printf&#40;&quot;Overwriting the documents to be reset completed with status code %d.%n&quot;,
     *             resetDocsResult.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List -->
     *
     * @param indexer The indexer to reset documents for.
     * @param overwrite If false, keys or IDs will be appended to existing ones. If true, only the keys or IDs in this
     * payload will be queued to be re-ingested.
     * @param documentKeys Document keys to be reset.
     * @param datasourceDocumentIds Datasource document identifiers to be reset.
     * @return A response signalling completion.
     * @throws NullPointerException If {@code indexer} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resetDocumentsWithResponse(SearchIndexer indexer, Boolean overwrite,
        List<String> documentKeys, List<String> datasourceDocumentIds) {
        if (indexer == null) {
            return monoError(LOGGER, new NullPointerException("'indexer' cannot be null."));
        }

        return withContext(context -> resetDocumentsWithResponse(indexer.getName(), overwrite, documentKeys,
            datasourceDocumentIds, context));
    }

    Mono<Response<Void>> resetDocumentsWithResponse(String indexerName, Boolean overwrite, List<String> documentKeys,
        List<String> datasourceDocumentIds, Context context) {
        try {
            DocumentKeysOrIds documentKeysOrIds = new DocumentKeysOrIds()
                .setDocumentKeys(documentKeys)
                .setDatasourceDocumentIds(datasourceDocumentIds);

            return restClient.getIndexers()
                .resetDocsWithResponseAsync(indexerName, overwrite, documentKeysOrIds, null, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset -->
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
     * SEARCH_INDEXER_ASYNC_CLIENT.createSkillset&#40;searchIndexerSkillset&#41;
     *     .subscribe&#40;skillset -&gt;
     *         System.out.printf&#40;&quot;The indexer skillset name is %s. The ETag of indexer skillset is %s.%n&quot;,
     *         skillset.getName&#40;&#41;, skillset.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset -->
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerSkillset> createSkillset(SearchIndexerSkillset skillset) {
        return createSkillsetWithResponse(skillset).map(Response::getValue);
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset -->
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
     * SEARCH_INDEXER_ASYNC_CLIENT.createSkillsetWithResponse&#40;searchIndexerSkillset&#41;
     *     .subscribe&#40;skillsetWithResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The indexer skillset name is %s.%n&quot;,
     *         skillsetWithResponse.getStatusCode&#40;&#41;, skillsetWithResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset -->
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return a response containing the created Skillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset) {
        return withContext(context -> createSkillsetWithResponse(skillset, context));
    }

    Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset, Context context) {
        if (skillset == null) {
            return monoError(LOGGER, new NullPointerException("'skillset' cannot be null."));
        }
        try {
            return restClient.getSkillsets()
                .createWithResponseAsync(skillset, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves a skillset definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSearchIndexerSkillset#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .subscribe&#40;indexerSkillset -&gt;
     *         System.out.printf&#40;&quot;The indexer skillset name is %s. The ETag of indexer skillset is %s.%n&quot;,
     *         indexerSkillset.getName&#40;&#41;, indexerSkillset.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSearchIndexerSkillset#String -->
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the Skillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerSkillset> getSkillset(String skillsetName) {
        return getSkillsetWithResponse(skillsetName).map(Response::getValue);
    }

    /**
     * Retrieves a skillset definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillsetWithResponse&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .subscribe&#40;skillsetWithResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The indexer skillset name is %s.%n&quot;,
     *         skillsetWithResponse.getStatusCode&#40;&#41;, skillsetWithResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String -->
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return a response containing the Skillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> getSkillsetWithResponse(String skillsetName) {
        return withContext(context -> getSkillsetWithResponse(skillsetName, context));
    }

    Mono<Response<SearchIndexerSkillset>> getSkillsetWithResponse(String skillsetName, Context context) {
        try {
            return this.restClient.getSkillsets()
                .getWithResponseAsync(skillsetName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillsets. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listSkillsets&#40;&#41;
     *     .subscribe&#40;skillset -&gt;
     *         System.out.printf&#40;&quot;The skillset name is %s. The ETag of skillset is %s.%n&quot;, skillset.getName&#40;&#41;,
     *         skillset.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets -->
     *
     * @return a reactive response emitting the list of skillsets.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndexerSkillset> listSkillsets() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSkillsetsWithResponse(null, context))
                    .map(MappingUtils::mappingPagingSkillset));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Lists all skillset names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillset names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsetNames -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.listSkillsetNames&#40;&#41;
     *     .subscribe&#40;skillsetName -&gt; System.out.printf&#40;&quot;The indexer skillset name is %s.%n&quot;, skillsetName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsetNames -->
     *
     * @return a reactive response emitting the list of skillset names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSkillsetNames() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSkillsetsWithResponse("name", context))
                    .map(MappingUtils::mappingPagingSkillsetNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    private Mono<Response<ListSkillsetsResult>> listSkillsetsWithResponse(String select, Context context) {
        return this.restClient.getSkillsets()
            .listWithResponseAsync(select, null, context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .flatMap&#40;indexerSkillset -&gt; &#123;
     *         indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillset&#40;indexerSkillset&#41;;
     *     &#125;&#41;.subscribe&#40;updateSkillset -&gt;
     *         System.out.printf&#40;&quot;The indexer skillset name is %s. The description of indexer skillset is %s.%n&quot;,
     *         updateSkillset.getName&#40;&#41;, updateSkillset.getDescription&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset -->
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexerSkillset> createOrUpdateSkillset(SearchIndexerSkillset skillset) {
        return createOrUpdateSkillsetWithResponse(skillset, false).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .flatMap&#40;indexerSkillset -&gt; &#123;
     *         indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillsetWithResponse&#40;indexerSkillset, true&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updateSkillsetResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer skillset name is %s. &quot;
     *             + &quot;The description of indexer skillset is %s.%n&quot;, updateSkillsetResponse.getStatusCode&#40;&#41;,
     *         updateSkillsetResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *         updateSkillsetResponse.getValue&#40;&#41;.getDescription&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean -->
     *
     * @param skillset the definition of the skillset to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the skillset that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged) {
        return withContext(context -> createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, null, null,
            context));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .flatMap&#40;indexerSkillset -&gt; &#123;
     *         indexerSkillset.setDescription&#40;&quot;This is new description!&quot;&#41;;
     *         return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillsetWithResponse&#40;
     *             new CreateOrUpdateSkillsetOptions&#40;indexerSkillset&#41;
     *                 .setOnlyIfUnchanged&#40;true&#41;
     *                 .setCacheReprocessingChangeDetectionDisabled&#40;false&#41;
     *                 .setCacheResetRequirementsIgnored&#40;true&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updateSkillsetResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%nThe indexer skillset name is %s. &quot;
     *             + &quot;The description of indexer skillset is %s.%n&quot;, updateSkillsetResponse.getStatusCode&#40;&#41;,
     *             updateSkillsetResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *             updateSkillsetResponse.getValue&#40;&#41;.getDescription&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions -->
     *
     * @param options The options used to create or update the {@link SearchIndexerSkillset skillset}.
     * @return a response containing the skillset that was created or updated.
     * @throws NullPointerException If {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(
        CreateOrUpdateSkillsetOptions options) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return withContext(context -> createOrUpdateSkillsetWithResponse(options.getSkillset(),
            options.isOnlyIfUnchanged(), options.isCacheReprocessingChangeDetectionDisabled(),
            options.isCacheResetRequirementsIgnored(), context));
    }

    Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, Boolean disableCacheReprocessingChangeDetection, Boolean ignoreResetRequirements,
        Context context) {
        if (skillset == null) {
            return monoError(LOGGER, new NullPointerException("'skillset' cannot be null."));
        }
        String ifMatch = onlyIfUnchanged ? skillset.getETag() : null;
        try {
            return restClient.getSkillsets()
                .createOrUpdateWithResponseAsync(skillset.getName(), skillset, ifMatch, null,
                    disableCacheReprocessingChangeDetection, ignoreResetRequirements, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillset#String -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.deleteSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillset#String -->
     *
     * @param skillsetName the name of the skillset to delete
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSkillset(String skillsetName) {
        return withContext(context -> deleteSkillsetWithResponse(skillsetName, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer skillset "searchIndexerSkillset". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .flatMap&#40;searchIndexerSkillset -&gt;
     *         SEARCH_INDEXER_ASYNC_CLIENT.deleteSkillsetWithResponse&#40;searchIndexerSkillset, true&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean -->
     *
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSkillsetWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged) {
        if (skillset == null) {
            return monoError(LOGGER, new NullPointerException("'skillset' cannot be null."));
        }
        String eTag = onlyIfUnchanged ? skillset.getETag() : null;
        return withContext(context -> deleteSkillsetWithResponse(skillset.getName(), eTag, context));
    }

    Mono<Response<Void>> deleteSkillsetWithResponse(String skillsetName, String eTag, Context context) {
        try {
            return restClient.getSkillsets()
                .deleteWithResponseAsync(skillsetName, eTag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Resets skills in an existing skillset in an Azure Cognitive Search service.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-List -->
     * <pre>
     * &#47;&#47; Reset the &quot;myOcr&quot; and &quot;myText&quot; skills.
     * SEARCH_INDEXER_ASYNC_CLIENT.resetSkills&#40;&quot;searchIndexerSkillset&quot;, Arrays.asList&#40;&quot;myOcr&quot;, &quot;myText&quot;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-List -->
     *
     * @param skillsetName The name of the skillset to reset.
     * @param skillNames The skills to reset.
     * @return A response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetSkills(String skillsetName, List<String> skillNames) {
        return withContext(context -> resetSkillsWithResponse(skillsetName, skillNames, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Resets skills in an existing skillset in an Azure Cognitive Search service.
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#SearchIndexerSkillset-List -->
     * <pre>
     * SEARCH_INDEXER_ASYNC_CLIENT.getSkillset&#40;&quot;searchIndexerSkillset&quot;&#41;
     *     .flatMap&#40;searchIndexerSkillset -&gt; SEARCH_INDEXER_ASYNC_CLIENT.resetSkillsWithResponse&#40;searchIndexerSkillset,
     *         Arrays.asList&#40;&quot;myOcr&quot;, &quot;myText&quot;&#41;&#41;&#41;
     *     .subscribe&#40;resetSkillsResponse -&gt; System.out.printf&#40;&quot;Resetting skills completed with status code %d.%n&quot;,
     *         resetSkillsResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#SearchIndexerSkillset-List -->
     *
     * @param skillset The skillset to reset.
     * @param skillNames The skills to reset.
     * @return A response signalling completion.
     * @throws NullPointerException If {@code skillset} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resetSkillsWithResponse(SearchIndexerSkillset skillset, List<String> skillNames) {
        if (skillset == null) {
            return monoError(LOGGER, new NullPointerException("'skillset' cannot be null."));
        }

        return withContext(context -> resetSkillsWithResponse(skillset.getName(), skillNames, context));
    }

    Mono<Response<Void>> resetSkillsWithResponse(String skillsetName, List<String> skillNames, Context context) {
        try {
            return restClient.getSkillsets()
                .resetSkillsWithResponseAsync(skillsetName, new SkillNames().setSkillNames(skillNames), null, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }
}
