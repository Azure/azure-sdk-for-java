// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.AnalyzeRequest;
import com.azure.search.documents.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.GetIndexStatisticsResult;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.Indexer;
import com.azure.search.documents.models.IndexerExecutionInfo;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.ServiceStatistics;
import com.azure.search.documents.models.Skillset;
import com.azure.search.documents.models.SynonymMap;
import com.azure.search.documents.models.TokenInfo;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous Client to manage and query indexes, as well as manage other resources, on a Cognitive Search service
 */
@ServiceClient(builder = SearchServiceClientBuilder.class, isAsync = true)
public final class SearchServiceAsyncClient {

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
    private final ClientLogger logger = new ClientLogger(SearchServiceAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchServiceAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;

        this.restClient = new SearchServiceRestClientBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
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
     * Initializes a new {@link SearchIndexAsyncClient} using the given Index name and the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchIndexAsyncClient} created from the service client configuration
     */
    public SearchIndexAsyncClient getIndexClient(String indexName) {
        return new SearchIndexAsyncClient(endpoint, indexName, serviceVersion, httpPipeline);
    }

    /**
     * Gets search service version.
     *
     * @return the search service version value.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.serviceVersion;
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
     * @param dataSource The definition of the {@link DataSource} to create or update.
     * @return the data source that was created or updated.
     */
    public Mono<DataSource> createOrUpdateDataSource(DataSource dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource The definition of the {@link DataSource} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a data source response.
     */
    public Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse(DataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateDataSourceWithResponse(dataSource, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse(DataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null.");
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        try {
            return restClient
                .dataSources()
                .createOrUpdateWithRestResponseAsync(dataSource.getName(),
                    dataSource, ifMatch, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the dataSource to create.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<DataSource> createDataSource(DataSource dataSource) {
        return createDataSourceWithResponse(dataSource, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the {@link DataSource} to create.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<Response<DataSource>> createDataSourceWithResponse(DataSource dataSource,
        RequestOptions requestOptions) {
        return withContext(context -> this.createDataSourceWithResponse(dataSource, requestOptions, context));
    }

    Mono<Response<DataSource>> createDataSourceWithResponse(DataSource dataSource, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.dataSources()
                .createWithRestResponseAsync(dataSource, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link DataSource} to retrieve.
     * @return the DataSource.
     */
    public Mono<DataSource> getDataSource(String dataSourceName) {
        return getDataSourceWithResponse(dataSourceName, null).map(Response::getValue);
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link DataSource} to retrieve.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a response containing the DataSource.
     */
    public Mono<Response<DataSource>> getDataSourceWithResponse(String dataSourceName, RequestOptions requestOptions) {
        return withContext(context -> getDataSourceWithResponse(dataSourceName, requestOptions, context));
    }

    Mono<Response<DataSource>> getDataSourceWithResponse(String dataSourceName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.dataSources()
                .getWithRestResponseAsync(dataSourceName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources() {
        return listDataSources(null, null);
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourcesWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourcesWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<DataSource>> listDataSourcesWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.dataSources()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getDataSources(),
                null,
                null));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the {@link DataSource} for deletion
     * @return a void Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName) {
        return withContext(context ->
            deleteDataSourceWithResponse(dataSourceName, null, null, context).flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search data source.
     *
     * @param dataSource The {@link DataSource} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a mono response
     */
    public Mono<Response<Void>> deleteDataSourceWithResponse(DataSource dataSource, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null");
        String etag = onlyIfUnchanged ? dataSource.getETag() : null;
        return withContext(context ->
            deleteDataSourceWithResponse(dataSource.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.dataSources()
                .deleteWithRestResponseAsync(
                    dataSourceName,
                    etag, null,
                    requestOptions,
                    context).map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public Mono<Indexer> createIndexer(Indexer indexer) {
        return createIndexerWithResponse(indexer, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<Indexer>> createIndexerWithResponse(Indexer indexer, RequestOptions requestOptions) {
        return withContext(context -> createIndexerWithResponse(indexer, requestOptions, context));
    }

    Mono<Response<Indexer>> createIndexerWithResponse(Indexer indexer, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers()
                .createWithRestResponseAsync(indexer, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public Mono<Indexer> createOrUpdateIndexer(Indexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer the definition of the {@link Indexer} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<Indexer>> createOrUpdateIndexerWithResponse(Indexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<Indexer>> createOrUpdateIndexerWithResponse(Indexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be 'null'");
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        try {
            return restClient.indexers()
                .createOrUpdateWithRestResponseAsync(indexer.getName(), indexer, ifMatch, null, requestOptions,
                    context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    public Mono<Indexer> getIndexer(String indexerName) {
        return getIndexerWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the indexer.
     */
    public Mono<Response<Indexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> getIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Indexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers()
                .getWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * @return all Indexers from the Search service.
     */
    public PagedFlux<Indexer> listIndexers() {
        return listIndexers(null, null);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of the indexers to retrieve. Specified as a comma-separated list
     * of JSON property names, or '*' for all properties. The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * @return a response containing all Indexers from the Search service.
     */
    public PagedFlux<Indexer> listIndexers(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<Indexer> listIndexers(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<Indexer>> listIndexersWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.indexers()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getIndexers(),
                null,
                null));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndexer(String indexerName) {
        return withContext(context -> deleteIndexerWithResponse(indexerName, null, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexer the {@link Indexer} to delete
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexerWithResponse(Indexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be null");
        String etag = onlyIfUnchanged ? indexer.getETag() : null;
        return withContext(context -> deleteIndexerWithResponse(indexer.getName(), etag, requestOptions, context));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param etag Optional. The etag to match.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context the context
     * @return a response signalling completion.
     */
    Mono<Response<Void>> deleteIndexerWithResponse(String indexerName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .deleteWithRestResponseAsync(indexerName, etag, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @return a response signalling completion.
     */
    public Mono<Void> resetIndexer(String indexerName) {
        return resetIndexerWithResponse(indexerName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> resetIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> resetIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> resetIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers()
                .resetWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @return a response signalling completion.
     */
    public Mono<Void> runIndexer(String indexerName) {
        return runIndexerWithResponse(indexerName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> runIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> runIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> runIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers().runWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return the indexer execution info.
     */
    public Mono<IndexerExecutionInfo> getIndexerStatus(String indexerName) {
        return getIndexerStatusWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response with the indexer execution info.
     */
    public Mono<Response<IndexerExecutionInfo>> getIndexerStatusWithResponse(String indexerName,
        RequestOptions requestOptions) {
        return withContext(context -> getIndexerStatusWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<IndexerExecutionInfo>> getIndexerStatusWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .getStatusWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create.
     * @return the created Index.
     */
    public Mono<Index> createIndex(Index index) {
        return createIndexWithResponse(index, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Index.
     */
    public Mono<Response<Index>> createIndexWithResponse(Index index, RequestOptions requestOptions) {
        return withContext(context -> createIndexWithResponse(index, requestOptions, context));
    }

    Mono<Response<Index>> createIndexWithResponse(Index index, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(index, "'Index' cannot be null");
        try {
            return restClient.indexes()
                .createWithRestResponseAsync(index, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName The name of the index to retrieve
     * @return the Index.
     */
    public Mono<Index> getIndex(String indexName) {
        return getIndexWithResponse(indexName, null).map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the Index.
     */
    public Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> getIndexWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexes()
                .getWithRestResponseAsync(indexName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return the index statistics result.
     */
    public Mono<GetIndexStatisticsResult> getIndexStatistics(String indexName) {
        return getIndexStatisticsWithResponse(indexName, null).map(Response::getValue);
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the index statistics result.
     */
    public Mono<Response<GetIndexStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
        RequestOptions requestOptions) {
        return withContext(context -> getIndexStatisticsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<GetIndexStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexes()
                .getStatisticsWithRestResponseAsync(indexName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of indexes.
     */
    public PagedFlux<Index> listIndexes() {
        return listIndexes(null, null);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of indexes.
     */
    public PagedFlux<Index> listIndexes(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<Index> listIndexes(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<Index>> listIndexesWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.indexes()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getIndexes(),
                null,
                null));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the {@link Index} to create or update.
     * @return the index that was created or updated.
     */
    public Mono<Index> createOrUpdateIndex(Index index) {
        return createOrUpdateIndexWithResponse(index, false, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes indexing and query requests
     * to fail. Performance and write availability of the index can be impaired for several minutes after the index is
     * updated, or longer for very large indexes
     * @param onlyIfUnchanged {@code true} to update if the {@code index} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the index that was created or updated
     */
    public Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateIndexWithResponse(index, allowIndexDowntime, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        try {
            Objects.requireNonNull(index, "'Index' cannot null.");
            String ifMatch = onlyIfUnchanged ? index.getETag() : null;
            return restClient.indexes()
                .createOrUpdateWithRestResponseAsync(index.getName(), index, allowIndexDowntime, ifMatch, null,
                    requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndex(String indexName) {
        return withContext(context -> deleteIndexWithResponse(indexName, null, null, null).flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param index the {@link Index} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code index} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexWithResponse(Index index, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(index, "'Index' cannot be null.");
        String etag = onlyIfUnchanged ? index.getETag() : null;
        return withContext(context -> deleteIndexWithResponse(index.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexes()
                .deleteWithRestResponseAsync(indexName, etag, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @return analyze result.
     */
    public PagedFlux<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest) {
        return analyzeText(indexName, analyzeRequest, null);
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing analyze result.
     */
    public PagedFlux<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> analyzeTextWithResponse(indexName, analyzeRequest, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest, RequestOptions requestOptions,
        Context context) {
        try {
            return new PagedFlux<>(() -> analyzeTextWithResponse(indexName, analyzeRequest, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<TokenInfo>> analyzeTextWithResponse(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions, Context context) {
        return restClient.indexes()
            .analyzeWithRestResponseAsync(indexName, analyzeRequest, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getTokens(),
                null,
                null));
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    public Mono<Skillset> createSkillset(Skillset skillset) {
        return createSkillsetWithResponse(skillset, null).map(Response::getValue);
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Skillset.
     */
    public Mono<Response<Skillset>> createSkillsetWithResponse(Skillset skillset, RequestOptions requestOptions) {
        return withContext(context -> createSkillsetWithResponse(skillset, requestOptions, context));
    }

    Mono<Response<Skillset>> createSkillsetWithResponse(Skillset skillset, RequestOptions requestOptions,
        Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        try {
            return restClient.skillsets()
                .createWithRestResponseAsync(skillset, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the Skillset.
     */
    public Mono<Skillset> getSkillset(String skillsetName) {
        return getSkillsetWithResponse(skillsetName, null).map(Response::getValue);
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the Skillset.
     */
    public Mono<Response<Skillset>> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions) {
        return withContext(context -> getSkillsetWithResponse(skillsetName, requestOptions, context));
    }

    Mono<Response<Skillset>> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions,
        Context context) {
        try {
            return this.restClient.skillsets()
                .getWithRestResponseAsync(skillsetName, requestOptions, context)
                .map(result -> result);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of skillsets.
     */
    public PagedFlux<Skillset> listSkillsets() {
        return listSkillsets(null, null);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the skillset definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of skillsets.
     */
    public PagedFlux<Skillset> listSkillsets(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSkillsetsWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<Skillset> listSkillsets(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> listSkillsetsWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<Skillset>> listSkillsetsWithResponse(String select,
        RequestOptions requestOptions,
        Context context) {
        return this.restClient.skillsets()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getSkillsets(),
                null,
                null));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    public Mono<Skillset> createOrUpdateSkillset(Skillset skillset) {
        return createOrUpdateSkillsetWithResponse(skillset, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the skillset that was created or updated.
     */
    public Mono<Response<Skillset>> createOrUpdateSkillsetWithResponse(Skillset skillset,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<Skillset>> createOrUpdateSkillsetWithResponse(Skillset skillset, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        String ifMatch = onlyIfUnchanged ? skillset.getETag() : null;
        try {
            return restClient.skillsets()
                .createOrUpdateWithRestResponseAsync(skillset.getName(), skillset, ifMatch, null, requestOptions,
                    context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteSkillset(String skillsetName) {
        return withContext(context -> deleteSkillsetWithResponse(skillsetName, null, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillset the {@link Skillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteSkillsetWithResponse(Skillset skillset, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        String etag = onlyIfUnchanged ? skillset.getETag() : null;
        return withContext(context ->
            deleteSkillsetWithResponse(skillset.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteSkillsetWithResponse(String skillsetName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.skillsets()
                .deleteWithRestResponseAsync(skillsetName, etag, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public Mono<SynonymMap> createSynonymMap(SynonymMap synonymMap) {
        return createSynonymMapWithResponse(synonymMap, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created SynonymMap.
     */
    public Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap,
        RequestOptions requestOptions) {
        return withContext(context -> createSynonymMapWithResponse(synonymMap, requestOptions, context));
    }

    Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap, RequestOptions requestOptions,
        Context context) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null.");
        try {
            return restClient.synonymMaps()
                .createWithRestResponseAsync(synonymMap, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public Mono<SynonymMap> getSynonymMap(String synonymMapName) {
        return getSynonymMapWithResponse(synonymMapName, null).map(Response::getValue);
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the SynonymMap.
     */
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions) {
        return withContext(context -> getSynonymMapWithResponse(synonymMapName, requestOptions, context));
    }

    Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.synonymMaps()
                .getWithRestResponseAsync(synonymMapName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of synonym maps.
     */
    public PagedFlux<SynonymMap> listSynonymMaps() {
        return listSynonymMaps(null, null);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the synonym maps to retrieve. Specified as a comma-separated
     * list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of synonym maps.
     */
    public PagedFlux<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSynonymMapsWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> listSynonymMapsWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SynonymMap>> listSynonymMapsWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.synonymMaps()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getSynonymMaps(),
                null,
                null));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create or update
     * @return the synonym map that was created or updated.
     */
    public Mono<SynonymMap> createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return createOrUpdateSynonymMapWithResponse(synonymMap, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the synonym map that was created or updated.
     */
    public Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateSynonymMapWithResponse(synonymMap, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null.");
        String ifMatch = onlyIfUnchanged ? synonymMap.getETag() : null;
        try {
            return restClient.synonymMaps()
                .createOrUpdateWithRestResponseAsync(synonymMap.getName(), synonymMap, ifMatch, null, requestOptions,
                    context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the {@link SynonymMap} to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteSynonymMap(String synonymMapName) {
        return withContext(context -> deleteSynonymMapWithResponse(synonymMapName, null, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMap the {@link SynonymMap} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteSynonymMapWithResponse(SynonymMap synonymMap, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null");
        String etag = onlyIfUnchanged ? synonymMap.getETag() : null;
        return withContext(context ->
            deleteSynonymMapWithResponse(synonymMap.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteSynonymMapWithResponse(String synonymMapName, String etag,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.synonymMaps()
                .deleteWithRestResponseAsync(synonymMapName, etag, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     * <p>
     * Contains the tracking ID sent with the request to help with debugging
     *
     * @return the search service statistics result.
     */
    public Mono<ServiceStatistics> getServiceStatistics() {
        return getServiceStatisticsWithResponse(null).map(Response::getValue);
    }


    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return the search service statistics result.
     */
    public Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions) {
        return withContext(context -> getServiceStatisticsWithResponse(requestOptions, context));
    }

    Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions, Context context) {
        try {
            return restClient.getServiceStatisticsWithRestResponseAsync(requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
