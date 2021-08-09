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
import com.azure.search.documents.implementation.converters.SearchIndexerConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerDataSourceConverter;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImpl;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImplBuilder;
import com.azure.search.documents.indexes.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.implementation.models.ListIndexersResult;
import com.azure.search.documents.indexes.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import reactor.core.publisher.Mono;

import java.util.Objects;
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
    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexerAsyncClient.class);

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

        this.restClient = new SearchServiceClientImplBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .buildClient();
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean}
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
            createOrUpdateDataSourceConnectionWithResponse(dataSource, onlyIfUnchanged, context));
    }

    Mono<Response<SearchIndexerDataSourceConnection>> createOrUpdateDataSourceConnectionWithResponse(
        SearchIndexerDataSourceConnection dataSource, boolean onlyIfUnchanged, Context context) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null.");
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        if (dataSource.getConnectionString() == null) {
            dataSource.setConnectionString("<unchanged>");
        }
        try {
            return restClient.getDataSources()
                .createOrUpdateWithResponseAsync(dataSource.getName(), SearchIndexerDataSourceConverter.map(dataSource),
                    ifMatch, null, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer data source connection named "dataSource".  </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection}
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
                .createWithResponseAsync(SearchIndexerDataSourceConverter.map(dataSource), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer data source connection named "dataSource". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnection#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String}
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
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connections. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerDataSourceConnection> listDataSourceConnections(Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourceConnectionsWithResponse(null, context)
                .map(MappingUtils::mappingPagingDataSource));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * List all DataSource names from an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer data source connection names. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnectionNames}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listDataSourceConnectionNames(Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourceConnectionsWithResponse("name", context)
                .map(MappingUtils::mappingPagingDataSourceNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnection#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean}
     *
     * @param dataSource The {@link SearchIndexerDataSourceConnection} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a mono response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection dataSource,
        boolean onlyIfUnchanged) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null");
        String etag = onlyIfUnchanged ? dataSource.getETag() : null;
        return withContext(context -> deleteDataSourceConnectionWithResponse(dataSource.getName(), etag, context));
    }

    Mono<Response<Void>> deleteDataSourceConnectionWithResponse(String dataSourceName, String etag, Context context) {
        try {
            return restClient.getDataSources()
                .deleteWithResponseAsync(dataSourceName, etag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer named "searchIndexer". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexer#SearchIndexer}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer}
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
                .createWithResponseAsync(SearchIndexerConverter.map(indexer), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search indexer named "searchIndexer". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexer#SearchIndexer}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean}
     *
     * @param indexer the definition of the {@link SearchIndexer} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the created Indexer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer,
        boolean onlyIfUnchanged) {
        return withContext(context -> createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, context));
    }

    Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        Context context) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be 'null'");
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        try {
            return restClient.getIndexers()
                .createOrUpdateWithResponseAsync(indexer.getName(), SearchIndexerConverter.map(indexer), ifMatch, null,
                    null, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves an indexer definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer with name "searchIndexer". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexer#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String}
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
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexers. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexer> listIndexers(Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse(null, context)
                .map(MappingUtils::mappingPagingSearchIndexer));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer names. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexerNames}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listIndexerNames(Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse("name", context)
                .map(MappingUtils::mappingPagingSearchIndexerNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexer#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#SearchIndexer-boolean}
     *
     * @param indexer the {@link SearchIndexer} to delete
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be null");
        String etag = onlyIfUnchanged ? indexer.getETag() : null;
        return withContext(context -> deleteIndexerWithResponse(indexer.getName(), etag, context));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param etag Optional. The etag to match.
     * @param context the context
     * @return a response signalling completion.
     */
    Mono<Response<Void>> deleteIndexerWithResponse(String indexerName, String etag, Context context) {
        try {
            return restClient.getIndexers()
                .deleteWithResponseAsync(indexerName, etag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Reset search indexer named "searchIndexer".  </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexer#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String}
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
            return monoError(logger, ex);
        }
    }

    /**
     * Runs an indexer on-demand.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Run search indexer named "searchIndexer".  </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexer#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String}
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
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get status for search indexer "searchIndexer".  </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatus#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String}
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
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search indexer skillset "searchIndexerSkillset". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset}
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return a response containing the created Skillset.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset) {
        return withContext(context -> createSkillsetWithResponse(skillset, context));
    }

    Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset, Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        try {
            return restClient.getSkillsets()
                .createWithResponseAsync(skillset, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a skillset definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search indexer skillset "searchIndexerSkillset". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSearchIndexerSkillset#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String}
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
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillsets. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerSkillset> listSkillsets(Context context) {
        try {
            return new PagedFlux<>(() -> listSkillsetsWithResponse(null, context)
                .map(MappingUtils::mappingPagingSkillset));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all skillset names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexer skillset names. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsetNames}
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
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listSkillsetNames(Context context) {
        try {
            return new PagedFlux<>(() -> listSkillsetsWithResponse("name", context)
                .map(MappingUtils::mappingPagingSkillsetNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean}
     *
     * @param skillset the definition of the skillset to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the skillset that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged) {
        return withContext(context -> createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, context));
    }

    Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        String ifMatch = onlyIfUnchanged ? skillset.getETag() : null;
        try {
            return restClient.getSkillsets()
                .createOrUpdateWithResponseAsync(skillset.getName(), skillset, ifMatch, null, null, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search indexer skillset "searchIndexerSkillset". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillset#String}
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
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean}
     *
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSkillsetWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        String etag = onlyIfUnchanged ? skillset.getETag() : null;
        return withContext(context -> deleteSkillsetWithResponse(skillset.getName(), etag, context));
    }

    Mono<Response<Void>> deleteSkillsetWithResponse(String skillsetName, String etag, Context context) {
        try {
            return restClient.getSkillsets()
                .deleteWithResponseAsync(skillsetName, etag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

}
