// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.converters.RequestOptionsIndexesConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerDataSourceConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerSkillsetConverter;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.indexes.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.indexes.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSource;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.models.RequestOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous Client to manage and query indexers, as well as manage other resources, on a Cognitive Search service
 */
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
    private final SearchServiceRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchIndexerAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
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
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create or update.
     * @return the data source that was created or updated.
     */
    public Mono<SearchIndexerDataSource> createOrUpdateDataSource(SearchIndexerDataSource dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a data source response.
     */
    public Mono<Response<SearchIndexerDataSource>> createOrUpdateDataSourceWithResponse(
        SearchIndexerDataSource dataSource, boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateDataSourceWithResponse(dataSource, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> createOrUpdateDataSourceWithResponse(SearchIndexerDataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null.");
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        try {
            return restClient
                .dataSources()
                .createOrUpdateWithRestResponseAsync(dataSource.getName(),
                    SearchIndexerDataSourceConverter.map(dataSource), ifMatch, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
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
    public Mono<SearchIndexerDataSource> createDataSource(SearchIndexerDataSource dataSource) {
        return createDataSourceWithResponse(dataSource, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<Response<SearchIndexerDataSource>> createDataSourceWithResponse(SearchIndexerDataSource dataSource,
        RequestOptions requestOptions) {
        return withContext(context -> this.createDataSourceWithResponse(dataSource, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> createDataSourceWithResponse(SearchIndexerDataSource dataSource,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.dataSources()
                .createWithRestResponseAsync(SearchIndexerDataSourceConverter.map(dataSource),
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} to retrieve.
     * @return the DataSource.
     */
    public Mono<SearchIndexerDataSource> getDataSource(String dataSourceName) {
        return getDataSourceWithResponse(dataSourceName, null).map(Response::getValue);
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} to retrieve.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a response containing the DataSource.
     */
    public Mono<Response<SearchIndexerDataSource>> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions) {
        return withContext(context -> getDataSourceWithResponse(dataSourceName, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.dataSources()
                .getWithRestResponseAsync(dataSourceName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalDataSource);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedFlux<SearchIndexerDataSource> listDataSources() {
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
    public PagedFlux<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourcesWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourcesWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndexerDataSource>> listDataSourcesWithResponse(String select,
        RequestOptions requestOptions, Context context) {
        return restClient.dataSources()
            .listWithRestResponseAsync(select, RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingPagingDataSource);
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} for deletion
     * @return a void Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName) {
        return withContext(context ->
            deleteDataSourceWithResponse(dataSourceName, null, null, context).flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search data source.
     *
     * @param dataSource The {@link SearchIndexerDataSource} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a mono response
     */
    public Mono<Response<Void>> deleteDataSourceWithResponse(SearchIndexerDataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
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
                    RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
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
    public Mono<SearchIndexer> createIndexer(SearchIndexer indexer) {
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
    public Mono<Response<SearchIndexer>> createIndexerWithResponse(SearchIndexer indexer,
        RequestOptions requestOptions) {
        return withContext(context -> createIndexerWithResponse(indexer, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> createIndexerWithResponse(SearchIndexer indexer, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .createWithRestResponseAsync(SearchIndexerConverter.map(indexer),
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
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
    public Mono<SearchIndexer> createOrUpdateIndexer(SearchIndexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer the definition of the {@link SearchIndexer} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be 'null'");
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        try {
            return restClient.indexers()
                .createOrUpdateWithRestResponseAsync(indexer.getName(), SearchIndexerConverter.map(indexer), ifMatch,
                    null,
                    RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
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
    public Mono<SearchIndexer> getIndexer(String indexerName) {
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
    public Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> getIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .getWithRestResponseAsync(indexerName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndexer);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * @return all Indexers from the Search service.
     */
    public PagedFlux<SearchIndexer> listIndexers() {
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
    public PagedFlux<SearchIndexer> listIndexers(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexer> listIndexers(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndexer>> listIndexersWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.indexers()
            .listWithRestResponseAsync(select, RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingPagingSearchIndexer);
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
     * @param indexer the {@link SearchIndexer} to delete
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
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
                .deleteWithRestResponseAsync(indexerName, etag, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
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
                .resetWithRestResponseAsync(indexerName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
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
            return restClient.indexers().runWithRestResponseAsync(indexerName,
                RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
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
    public Mono<SearchIndexerStatus> getIndexerStatus(String indexerName) {
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
    public Mono<Response<SearchIndexerStatus>> getIndexerStatusWithResponse(String indexerName,
        RequestOptions requestOptions) {
        return withContext(context -> getIndexerStatusWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<SearchIndexerStatus>> getIndexerStatusWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .getStatusWithRestResponseAsync(indexerName, RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingIndexerStatus);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    public Mono<SearchIndexerSkillset> createSkillset(SearchIndexerSkillset skillset) {
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
    public Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset,
        RequestOptions requestOptions) {
        return withContext(context -> createSkillsetWithResponse(skillset, requestOptions, context));
    }

    Mono<Response<SearchIndexerSkillset>> createSkillsetWithResponse(SearchIndexerSkillset skillset,
        RequestOptions requestOptions,
        Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        try {
            return restClient.skillsets()
                .createWithRestResponseAsync(SearchIndexerSkillsetConverter.map(skillset),
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSkillset);
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
    public Mono<SearchIndexerSkillset> getSkillset(String skillsetName) {
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
    public Mono<Response<SearchIndexerSkillset>> getSkillsetWithResponse(String skillsetName,
        RequestOptions requestOptions) {
        return withContext(context -> getSkillsetWithResponse(skillsetName, requestOptions, context));
    }

    Mono<Response<SearchIndexerSkillset>> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions,
        Context context) {
        try {
            return this.restClient.skillsets()
                .getWithRestResponseAsync(skillsetName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSkillset);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of skillsets.
     */
    public PagedFlux<SearchIndexerSkillset> listSkillsets() {
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
    public PagedFlux<SearchIndexerSkillset> listSkillsets(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSkillsetsWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerSkillset> listSkillsets(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> listSkillsetsWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndexerSkillset>> listSkillsetsWithResponse(String select,
        RequestOptions requestOptions,
        Context context) {
        return this.restClient.skillsets()
            .listWithRestResponseAsync(select, RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingPagingSkillset);
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    public Mono<SearchIndexerSkillset> createOrUpdateSkillset(SearchIndexerSkillset skillset) {
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
    public Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndexerSkillset>> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(skillset, "'Skillset' cannot be null.");
        String ifMatch = onlyIfUnchanged ? skillset.getETag() : null;
        try {
            return restClient.skillsets()
                .createOrUpdateWithRestResponseAsync(skillset.getName(), SearchIndexerSkillsetConverter.map(skillset),
                    ifMatch, null,
                    RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSkillset);
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
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteSkillsetWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged,
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
                .deleteWithRestResponseAsync(skillsetName, etag, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

}
