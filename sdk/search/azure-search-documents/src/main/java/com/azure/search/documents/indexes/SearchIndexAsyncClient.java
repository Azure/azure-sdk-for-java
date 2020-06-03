// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.converters.AnalyzeRequestConverter;
import com.azure.search.documents.implementation.converters.RequestOptionsIndexesConverter;
import com.azure.search.documents.implementation.converters.SearchIndexConverter;
import com.azure.search.documents.implementation.converters.SynonymMapConverter;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.indexes.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.indexes.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.indexes.implementation.models.ListIndexesResult;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.models.AnalyzeRequest;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.ServiceStatistics;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.RequestOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous Client to manage and query indexes, as well as Synonym Map, on a Cognitive Search service
 */
@ServiceClient(builder = SearchIndexClientBuilder.class, isAsync = true)
public final class SearchIndexAsyncClient {

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
    private final ClientLogger logger = new ClientLogger(SearchIndexAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchIndexAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
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
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Initializes a new {@link SearchAsyncClient} using the given Index name and the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchAsyncClient} created from the service client configuration
     */
    public SearchAsyncClient getSearchAsyncClient(String indexName) {
        return getSearchClientBuilder(indexName).buildAsyncClient();
    }

    SearchClientBuilder getSearchClientBuilder(String indexName) {
        return new SearchClientBuilder()
            .endpoint(endpoint)
            .indexName(indexName)
            .serviceVersion(serviceVersion)
            .pipeline(httpPipeline);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create.
     * @return the created Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> createIndex(SearchIndex index) {
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index, RequestOptions requestOptions) {
        return withContext(context -> createIndexWithResponse(index, requestOptions, context));
    }

    Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index, RequestOptions requestOptions,
        Context context) {
        Objects.requireNonNull(index, "'Index' cannot be null");
        try {
            return restClient.indexes()
                .createWithRestResponseAsync(SearchIndexConverter.map(index),
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> getIndex(String indexName) {
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> getIndexWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<SearchIndex>> getIndexWithResponse(String indexName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexes()
                .getWithRestResponseAsync(indexName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GetIndexStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
        RequestOptions requestOptions) {
        return withContext(context -> getIndexStatisticsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<GetIndexStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexes()
                .getStatisticsWithRestResponseAsync(indexName, RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingGetIndexStatistics);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of indexes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndex> listIndexes() {
        return listIndexes(null, null);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of indexes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndex> listIndexes(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse(null, requestOptions, context))
                    .map(MappingUtils::mappingPagingSearchIndex));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndex> listIndexes(RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse(null, requestOptions, context)
            .map(MappingUtils::mappingPagingSearchIndex));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all indexes names for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of index names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listIndexNames() {
        return listIndexNames(null);
    }

    /**
     * Lists all indexes names for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of index names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listIndexNames(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse("name", requestOptions, context))
                    .map(MappingUtils::mappingPagingSearchIndexNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listIndexNames(RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse("name", requestOptions, context)
                .map(MappingUtils::mappingPagingSearchIndexNames)
            );
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<SimpleResponse<ListIndexesResult>> listIndexesWithResponse(String select,
        RequestOptions requestOptions, Context context) {
        return restClient.indexes()
            .listWithRestResponseAsync(select, RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the {@link SearchIndex} to create or update.
     * @return the index that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> createOrUpdateIndex(SearchIndex index) {
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateIndexWithResponse(index, allowIndexDowntime, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndex>> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        try {
            Objects.requireNonNull(index, "'Index' cannot null.");
            String ifMatch = onlyIfUnchanged ? index.getETag() : null;
            return restClient.indexes()
                .createOrUpdateWithRestResponseAsync(index.getName(), SearchIndexConverter.map(index),
                    allowIndexDowntime, ifMatch, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteIndex(String indexName) {
        return withContext(context -> deleteIndexWithResponse(indexName, null, null, null).flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param index the {@link SearchIndex} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code index} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteIndexWithResponse(SearchIndex index, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(index, "'Index' cannot be null.");
        String etag = onlyIfUnchanged ? index.getETag() : null;
        return withContext(context -> deleteIndexWithResponse(index.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexes()
                .deleteWithRestResponseAsync(indexName, etag, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest) {
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> analyzeTextWithResponse(indexName, analyzeRequest, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> analyzeTextWithResponse(indexName, analyzeRequest, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<AnalyzedTokenInfo>> analyzeTextWithResponse(String indexName,
        AnalyzeRequest analyzeRequest, RequestOptions requestOptions, Context context) {
        return restClient.indexes()
            .analyzeWithRestResponseAsync(indexName, AnalyzeRequestConverter.map(analyzeRequest),
                RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingTokenInfo);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap,
        RequestOptions requestOptions) {
        return withContext(context -> createSynonymMapWithResponse(synonymMap, requestOptions, context));
    }

    Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap, RequestOptions requestOptions,
        Context context) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null.");
        try {
            return restClient.synonymMaps()
                .createWithRestResponseAsync(SynonymMapConverter.map(synonymMap),
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions) {
        return withContext(context -> getSynonymMapWithResponse(synonymMapName, requestOptions, context));
    }

    Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.synonymMaps()
                .getWithRestResponseAsync(synonymMapName, RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of synonym maps.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SynonymMap> listSynonymMaps() {
        return listSynonymMaps(null);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of synonym maps.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SynonymMap> listSynonymMaps(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSynonymMapsWithResponse(null, requestOptions, context))
                .map(MappingUtils::mappingPagingSynonymMap));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SynonymMap> listSynonymMaps(RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> listSynonymMapsWithResponse(null, requestOptions, context)
                .map(MappingUtils::mappingPagingSynonymMap));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all synonym map names for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of synonym map names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSynonymMapNames() {
        return listIndexNames(null);
    }

    /**
     * Lists all synonym map names for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a reactive response emitting the list of synonym map names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSynonymMapNames(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSynonymMapsWithResponse("name", requestOptions, context))
                    .map(MappingUtils::mappingPagingSynonymMapNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listSynonymMapNames(RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> listSynonymMapsWithResponse("name", requestOptions, context)
                .map(MappingUtils::mappingPagingSynonymMapNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<SimpleResponse<ListSynonymMapsResult>> listSynonymMapsWithResponse(String select,
        RequestOptions requestOptions, Context context) {
        return restClient.synonymMaps()
            .listWithRestResponseAsync(select, RequestOptionsIndexesConverter.map(requestOptions), context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create or update
     * @return the synonym map that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
                .createOrUpdateWithRestResponseAsync(synonymMap.getName(), SynonymMapConverter.map(synonymMap),
                    ifMatch, null,
                    RequestOptionsIndexesConverter.map(requestOptions),
                    context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
                .deleteWithRestResponseAsync(synonymMapName, etag, null,
                    RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions) {
        return withContext(context -> getServiceStatisticsWithResponse(requestOptions, context));
    }

    Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions, Context context) {
        try {
            return restClient.getServiceStatisticsWithRestResponseAsync(
                RequestOptionsIndexesConverter.map(requestOptions), context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalServiceStatistics);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
