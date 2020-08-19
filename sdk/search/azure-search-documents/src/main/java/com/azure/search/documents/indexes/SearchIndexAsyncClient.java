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
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.converters.AnalyzeRequestConverter;
import com.azure.search.documents.implementation.converters.SearchIndexConverter;
import com.azure.search.documents.implementation.converters.SynonymMapConverter;
import com.azure.search.documents.implementation.util.FieldBuilder;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImpl;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImplBuilder;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SynonymMap;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains the operations for creating, getting, listing, updating, or deleting
 * indexes or synonym map and analyzing text in an Azure Cognitive Search service.
 *
 * @see SearchIndexClientBuilder
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
    private final SearchServiceClientImpl restClient;

    private final JsonSerializer serializer;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchIndexAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline,
        JsonSerializer serializer) {
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;

        this.restClient = new SearchServiceClientImplBuilder()
            .endpoint(endpoint)
            //  .apiVersion(serviceVersion.getVersion())
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
     * Initializes a new {@link SearchAsyncClient} using the given Index name and the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchAsyncClient} created from the service client configuration
     */
    public SearchAsyncClient getSearchAsyncClient(String indexName) {
        return getSearchClientBuilder(indexName)
            .buildAsyncClient();
    }

    SearchClientBuilder getSearchClientBuilder(String indexName) {
        return new SearchClientBuilder()
            .endpoint(endpoint)
            .indexName(indexName)
            .serviceVersion(serviceVersion)
            .pipeline(httpPipeline)
            .serializer(serializer);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search index named "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex}
     *
     * @param index definition of the index to create.
     * @return the created Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> createIndex(SearchIndex index) {
        return createIndexWithResponse(index).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search index named "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex}
     *
     * @param index definition of the index to create
     * @return a response containing the created Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index) {
        return withContext(context -> createIndexWithResponse(index, context));
    }

    Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index, Context context) {
        Objects.requireNonNull(index, "'Index' cannot be null");
        try {
            return restClient.getIndexes()
                .createWithResponseAsync(SearchIndexConverter.map(index), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index with name "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndex#String}
     *
     * @param indexName The name of the index to retrieve
     * @return the Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> getIndex(String indexName) {
        return getIndexWithResponse(indexName).map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index with "searchIndex. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String}
     *
     * @param indexName the name of the index to retrieve
     * @return a response containing the Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> getIndexWithResponse(String indexName) {
        return withContext(context -> getIndexWithResponse(indexName, context));
    }

    Mono<Response<SearchIndex>> getIndexWithResponse(String indexName, Context context) {
        try {
            return restClient.getIndexes()
                .getWithResponseAsync(indexName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index "searchIndex" statistics. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatistics#String}
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return the index statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndexStatistics> getIndexStatistics(String indexName) {
        return getIndexStatisticsWithResponse(indexName).map(Response::getValue);
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index "searchIndex" statistics. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String}
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return a response containing the index statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndexStatistics>> getIndexStatisticsWithResponse(String indexName) {
        return withContext(context -> getIndexStatisticsWithResponse(indexName, context));
    }

    Mono<Response<SearchIndexStatistics>> getIndexStatisticsWithResponse(String indexName,
        Context context) {
        try {
            return restClient.getIndexes()
                .getStatisticsWithResponseAsync(indexName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingGetIndexStatistics);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexes}
     *
     * @return a reactive response emitting the list of indexes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SearchIndex> listIndexes() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse(null, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndex> listIndexes(Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse(null, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all indexes names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes names. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexNames}
     *
     * @return a reactive response emitting the list of index names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listIndexNames() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse("name", context))
                    .map(MappingUtils::mappingPagingSearchIndexNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listIndexNames(Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse("name", context)
                .map(MappingUtils::mappingPagingSearchIndexNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndex>> listIndexesWithResponse(String select, Context context) {
        return restClient.getIndexes()
            .listSinglePageAsync(select, null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingListingSearchIndex);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search index named "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndex#SearchIndex}
     *
     * @param index the definition of the {@link SearchIndex} to create or update.
     * @return the index that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchIndex> createOrUpdateIndex(SearchIndex index) {
        return createOrUpdateIndexWithResponse(index, false, false).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search index named "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context}
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes indexing and query requests
     * to fail. Performance and write availability of the index can be impaired for several minutes after the index is
     * updated, or longer for very large indexes
     * @param onlyIfUnchanged {@code true} to update if the {@code index} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the index that was created or updated
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchIndex>> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged) {
        return withContext(context ->
            createOrUpdateIndexWithResponse(index, allowIndexDowntime, onlyIfUnchanged, context));
    }

    Mono<Response<SearchIndex>> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, Context context) {
        try {
            Objects.requireNonNull(index, "'Index' cannot null.");
            String ifMatch = onlyIfUnchanged ? index.getETag() : null;
            return restClient.getIndexes()
                .createOrUpdateWithResponseAsync(index.getName(), SearchIndexConverter.map(index),
                    allowIndexDowntime, ifMatch, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSearchIndex);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search index with name "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndex#String}
     *
     * @param indexName the name of the index to delete
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteIndex(String indexName) {
        return deleteIndexWithResponse(indexName, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search index with name "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#SearchIndex-boolean}
     *
     * @param index the {@link SearchIndex} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code index} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteIndexWithResponse(SearchIndex index, boolean onlyIfUnchanged) {
        Objects.requireNonNull(index, "'Index' cannot be null.");
        String etag = onlyIfUnchanged ? index.getETag() : null;
        return withContext(context -> deleteIndexWithResponse(index.getName(), etag, context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName, String etag, Context context) {
        try {
            return restClient.getIndexes()
                .deleteWithResponseAsync(indexName, etag, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Analyzer text with LexicalTokenizerName "Classic" in search index "searchIndex". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions}
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeTextOptions the text and analyzer or analysis components to test
     * @return a response containing analyze result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeTextOptions analyzeTextOptions) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                analyzeTextWithResponse(indexName, analyzeTextOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeTextOptions analyzeTextOptions,
        Context context) {
        try {
            return new PagedFlux<>(() -> analyzeTextWithResponse(indexName, analyzeTextOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<AnalyzedTokenInfo>> analyzeTextWithResponse(String indexName,
        AnalyzeTextOptions analyzeTextOptions, Context context) {
        return restClient.getIndexes()
            .analyzeWithResponseAsync(indexName, AnalyzeRequestConverter.map(analyzeTextOptions), null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(MappingUtils::mappingTokenInfo);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create synonym map named "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMap#SynonymMap}
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SynonymMap> createSynonymMap(SynonymMap synonymMap) {
        return createSynonymMapWithResponse(synonymMap).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create synonym map named "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap}
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create
     * @return a response containing the created SynonymMap.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap) {
        return withContext(context -> createSynonymMapWithResponse(synonymMap, context));
    }

    Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap,
        Context context) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null.");
        try {
            return restClient.getSynonymMaps()
                .createWithResponseAsync(SynonymMapConverter.map(synonymMap), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a synonym map definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get synonym map with name "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String}
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SynonymMap> getSynonymMap(String synonymMapName) {
        return getSynonymMapWithResponse(synonymMapName).map(Response::getValue);
    }

    /**
     * Retrieves a synonym map definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get synonym map with name "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String}
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return a response containing the SynonymMap.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName) {
        return withContext(context -> getSynonymMapWithResponse(synonymMapName, context));
    }

    Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, Context context) {
        try {
            return restClient.getSynonymMaps()
                .getWithResponseAsync(synonymMapName, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym maps. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps}
     *
     * @return a reactive response emitting the list of synonym maps.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SynonymMap> listSynonymMaps() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSynonymMapsWithResponse(null, context))
                    .map(MappingUtils::mappingPagingSynonymMap));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SynonymMap> listSynonymMaps(Context context) {
        try {
            return new PagedFlux<>(() -> listSynonymMapsWithResponse(null, context)
                .map(MappingUtils::mappingPagingSynonymMap));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists all synonym map names for an Azure Cognitive Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym map names. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMapNames}
     *
     * @return a reactive response emitting the list of synonym map names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSynonymMapNames() {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listSynonymMapsWithResponse("name", context))
                    .map(MappingUtils::mappingPagingSynonymMapNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<String> listSynonymMapNames(Context context) {
        try {
            return new PagedFlux<>(() -> listSynonymMapsWithResponse("name", context)
                .map(MappingUtils::mappingPagingSynonymMapNames));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<Response<ListSynonymMapsResult>> listSynonymMapsWithResponse(String select, Context context) {
        return restClient.getSynonymMaps()
            .listWithResponseAsync(select, null, context)
            .onErrorMap(MappingUtils::exceptionMapper);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update synonym map named "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap}
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create or update
     * @return the synonym map that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SynonymMap> createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return createOrUpdateSynonymMapWithResponse(synonymMap, false).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update synonym map named "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context}
     *
     * @param synonymMap the definition of the {@link SynonymMap} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always update existing value.
     * @return a response containing the synonym map that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged) {
        return withContext(context ->
            createOrUpdateSynonymMapWithResponse(synonymMap, onlyIfUnchanged, context));
    }

    Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, Context context) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null.");
        String ifMatch = onlyIfUnchanged ? synonymMap.getETag() : null;
        try {
            return restClient.getSynonymMaps()
                .createOrUpdateWithResponseAsync(synonymMap.getName(), SynonymMapConverter.map(synonymMap),
                    ifMatch, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalSynonymMap);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete synonym map with name "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMap#String}
     *
     * @param synonymMapName the name of the {@link SynonymMap} to delete
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSynonymMap(String synonymMapName) {
        return withContext(context -> deleteSynonymMapWithResponse(synonymMapName, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete synonym map with name "synonymMap". </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#SynonymMap-boolean}
     *
     * @param synonymMap the {@link SynonymMap} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSynonymMapWithResponse(SynonymMap synonymMap, boolean onlyIfUnchanged) {
        Objects.requireNonNull(synonymMap, "'SynonymMap' cannot be null");
        String etag = onlyIfUnchanged ? synonymMap.getETag() : null;
        return withContext(context -> deleteSynonymMapWithResponse(synonymMap.getName(), etag, context));
    }

    /**
     * Convenience method to convert a {@link Class Class's} {@link Field Fields} and {@link Method Methods} into {@link
     * SearchField SearchFields} to help aid the creation of a {@link SearchField} which represents the {@link Class}.
     *
     * @param model The model {@link Class} that will have {@link SearchField SearchFields} generated from its
     * structure.
     * @param options Configuration used to determine generation of the {@link SearchField SearchFields}.
     * @return A list {@link SearchField SearchFields} which represent the model {@link Class}.
     */
    public static List<SearchField> buildSearchFields(Class<?> model, FieldBuilderOptions options) {
        return FieldBuilder.build(model, options);
    }

    Mono<Response<Void>> deleteSynonymMapWithResponse(String synonymMapName, String etag,
        Context context) {
        try {
            return restClient.getSynonymMaps()
                .deleteWithResponseAsync(synonymMapName, etag, null, null, context)
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
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get service statistics. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatistics}
     *
     * @return the search service statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchServiceStatistics> getServiceStatistics() {
        return getServiceStatisticsWithResponse().map(Response::getValue);
    }


    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get service statistics. </p>
     *
     * {@codesnippet com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse}
     *
     * @return the search service statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchServiceStatistics>> getServiceStatisticsWithResponse() {
        return withContext(this::getServiceStatisticsWithResponse);
    }

    Mono<Response<SearchServiceStatistics>> getServiceStatisticsWithResponse(Context context) {
        try {
            return restClient.getServiceStatisticsWithResponseAsync(null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(MappingUtils::mappingExternalServiceStatistics);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
