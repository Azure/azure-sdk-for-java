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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex -->
     * <pre>
     * List&lt;SearchField&gt; searchFields = Arrays.asList&#40;
     *     new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;.setKey&#40;true&#41;,
     *     new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;.setSearchable&#40;true&#41;
     * &#41;;
     * SearchIndex searchIndex = new SearchIndex&#40;&quot;searchIndex&quot;, searchFields&#41;;
     * searchIndexAsyncClient.createIndex&#40;searchIndex&#41;
     *     .subscribe&#40;indexFromService -&gt;
     *         System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, indexFromService.getName&#40;&#41;,
     *         indexFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex -->
     * <pre>
     * List&lt;SearchField&gt; searchFields = Arrays.asList&#40;
     *     new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;.setKey&#40;true&#41;,
     *     new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;.setSearchable&#40;true&#41;
     * &#41;;
     * SearchIndex searchIndex = new SearchIndex&#40;&quot;searchIndex&quot;, searchFields&#41;;
     *
     * searchIndexAsyncClient.createIndexWithResponse&#40;searchIndex&#41;
     *     .subscribe&#40;indexFromServiceResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The index name is %s.%n&quot;,
     *         indexFromServiceResponse.getStatusCode&#40;&#41;, indexFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndex#String -->
     * <pre>
     * searchIndexAsyncClient.getIndex&#40;&quot;searchIndex&quot;&#41;
     *     .subscribe&#40;indexFromService -&gt;
     *         System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, indexFromService.getName&#40;&#41;,
     *             indexFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndex#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String -->
     * <pre>
     * searchIndexAsyncClient.getIndexWithResponse&#40;&quot;searchIndex&quot;&#41;
     *     .subscribe&#40;indexFromServiceResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s. The index name is %s.%n&quot;,
     *             indexFromServiceResponse.getStatusCode&#40;&#41;, indexFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatistics#String -->
     * <pre>
     * searchIndexAsyncClient.getIndexStatistics&#40;&quot;searchIndex&quot;&#41;
     *     .subscribe&#40;statistics -&gt;
     *         System.out.printf&#40;&quot;There are %d documents and storage size of %d available in 'searchIndex'.%n&quot;,
     *         statistics.getDocumentCount&#40;&#41;, statistics.getStorageSize&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatistics#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String -->
     * <pre>
     * searchIndexAsyncClient.getIndexStatisticsWithResponse&#40;&quot;searchIndex&quot;&#41;
     *     .subscribe&#40;statistics -&gt; System.out.printf&#40;&quot;The status code of the response is %s.%n&quot;
     *             + &quot;There are %d documents and storage size of %d available in 'searchIndex'.%n&quot;,
     *         statistics.getStatusCode&#40;&#41;, statistics.getValue&#40;&#41;.getDocumentCount&#40;&#41;,
     *         statistics.getValue&#40;&#41;.getStorageSize&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String -->
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
                .onErrorMap(MappingUtils::exceptionMapper);
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexes -->
     * <pre>
     * searchIndexAsyncClient.listIndexes&#40;&#41;
     *     .subscribe&#40;index -&gt;
     *         System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, index.getName&#40;&#41;,
     *             index.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexes -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexNames -->
     * <pre>
     * searchIndexAsyncClient.listIndexNames&#40;&#41;
     *     .subscribe&#40;indexName -&gt; System.out.printf&#40;&quot;The index name is %s.%n&quot;, indexName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexNames -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndex#SearchIndex -->
     * <pre>
     * searchIndexAsyncClient.getIndex&#40;&quot;searchIndex&quot;&#41;
     *     .doOnNext&#40;indexFromService -&gt; indexFromService.setSuggesters&#40;Collections.singletonList&#40;
     *         new SearchSuggester&#40;&quot;sg&quot;, Collections.singletonList&#40;&quot;hotelName&quot;&#41;&#41;&#41;&#41;&#41;
     *     .flatMap&#40;searchIndexAsyncClient::createOrUpdateIndex&#41;
     *     .subscribe&#40;updatedIndex -&gt;
     *         System.out.printf&#40;&quot;The index name is %s. The suggester name of index is %s.%n&quot;,
     *             updatedIndex.getName&#40;&#41;, updatedIndex.getSuggesters&#40;&#41;.get&#40;0&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndex#SearchIndex -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context -->
     * <pre>
     * SearchIndex indexFromService = searchIndexClient.getIndex&#40;&quot;searchIndex&quot;&#41;;
     * indexFromService.setSuggesters&#40;Collections.singletonList&#40;new SearchSuggester&#40;&quot;sg&quot;,
     *     Collections.singletonList&#40;&quot;hotelName&quot;&#41;&#41;&#41;&#41;;
     * Response&lt;SearchIndex&gt; updatedIndexResponse = searchIndexClient.createOrUpdateIndexWithResponse&#40;indexFromService, true,
     *     false, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the normal response is %s.%n&quot;
     *         + &quot;The index name is %s. The ETag of index is %s.%n&quot;, updatedIndexResponse.getStatusCode&#40;&#41;,
     *     updatedIndexResponse.getValue&#40;&#41;.getName&#40;&#41;, updatedIndexResponse.getValue&#40;&#41;.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndex#String -->
     * <pre>
     * searchIndexAsyncClient.deleteIndex&#40;&quot;searchIndex&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndex#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#SearchIndex-boolean -->
     * <pre>
     * searchIndexAsyncClient.getIndex&#40;&quot;searchIndex&quot;&#41;
     *     .flatMap&#40;indexFromService -&gt; searchIndexAsyncClient.deleteIndexWithResponse&#40;indexFromService, true&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#SearchIndex-boolean -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions -->
     * <pre>
     * searchIndexAsyncClient.analyzeText&#40;&quot;searchIndex&quot;,
     *     new AnalyzeTextOptions&#40;&quot;The quick brown fox&quot;, LexicalTokenizerName.CLASSIC&#41;&#41;
     *     .subscribe&#40;tokenInfo -&gt;
     *         System.out.printf&#40;&quot;The token emitted by the analyzer is %s.%n&quot;, tokenInfo.getToken&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMap#SynonymMap -->
     * <pre>
     * SynonymMap synonymMap = new SynonymMap&#40;&quot;synonymMap&quot;,
     *     &quot;United States, United States of America, USA&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * searchIndexAsyncClient.createSynonymMap&#40;synonymMap&#41;
     *     .subscribe&#40;synonymMapFromService -&gt;
     *         System.out.printf&#40;&quot;The synonym map name is %s. The ETag of synonym map is %s.%n&quot;,
     *         synonymMapFromService.getName&#40;&#41;, synonymMapFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMap#SynonymMap -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap -->
     * <pre>
     * SynonymMap synonymMap = new SynonymMap&#40;&quot;synonymMap&quot;,
     *     &quot;United States, United States of America, USA&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * searchIndexAsyncClient.createSynonymMapWithResponse&#40;synonymMap&#41;
     *     .subscribe&#40;synonymMapFromService -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;
     *             + &quot;The synonym map name is %s. The ETag of synonym map is %s.%n&quot;,
     *             synonymMapFromService.getStatusCode&#40;&#41;,
     *         synonymMapFromService.getValue&#40;&#41;.getName&#40;&#41;, synonymMapFromService.getValue&#40;&#41;.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap -->
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
                .createWithResponseAsync(synonymMap, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String -->
     * <pre>
     * searchIndexAsyncClient.getSynonymMap&#40;&quot;synonymMap&quot;&#41;
     *     .subscribe&#40;synonymMapFromService -&gt;
     *         System.out.printf&#40;&quot;The synonym map is %s. The ETag of synonym map is %s.%n&quot;,
     *             synonymMapFromService.getName&#40;&#41;, synonymMapFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String -->
     * <pre>
     * searchIndexAsyncClient.getSynonymMap&#40;&quot;synonymMap&quot;&#41;
     *     .subscribe&#40;synonymMapFromService -&gt;
     *         System.out.printf&#40;&quot;The synonym map is %s. The ETag of synonym map is %s.%n&quot;,
     *             synonymMapFromService.getName&#40;&#41;, synonymMapFromService.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String -->
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
                .onErrorMap(MappingUtils::exceptionMapper);
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps -->
     * <pre>
     * searchIndexAsyncClient.listSynonymMaps&#40;&#41;
     *     .subscribe&#40;synonymMap -&gt; System.out.printf&#40;&quot;The synonymMap name is %s. The ETag of synonymMap is %s.%n&quot;,
     *         synonymMap.getName&#40;&#41;, synonymMap.getETag&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMapNames -->
     * <pre>
     * searchIndexAsyncClient.listSynonymMapNames&#40;&#41;
     *     .subscribe&#40;synonymMap -&gt; System.out.printf&#40;&quot;The synonymMap name is %s.%n&quot;, synonymMap&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMapNames -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap -->
     * <pre>
     * searchIndexAsyncClient.getSynonymMap&#40;&quot;searchIndex&quot;&#41;
     *     .doOnNext&#40;synonymMap -&gt; synonymMap
     *         .setSynonyms&#40;&quot;United States, United States of America, USA, America&#92;nWashington, Wash. =&gt; WA&quot;&#41;&#41;
     *     .flatMap&#40;searchIndexAsyncClient::createOrUpdateSynonymMap&#41;
     *     .subscribe&#40;updatedSynonymMap -&gt;
     *         System.out.printf&#40;&quot;The synonym map name is %s. The synonyms are %s.%n&quot;, updatedSynonymMap.getName&#40;&#41;,
     *         updatedSynonymMap.getSynonyms&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context -->
     * <pre>
     * searchIndexAsyncClient.getSynonymMap&#40;&quot;searchIndex&quot;&#41;
     *     .flatMap&#40;synonymMap -&gt; &#123;
     *         synonymMap.setSynonyms&#40;
     *             &quot;United States, United States of America, USA, America&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     *         return searchIndexAsyncClient.createOrUpdateSynonymMapWithResponse&#40;synonymMap, true&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updatedSynonymMap -&gt;
     *         System.out.printf&#40;&quot;The status code of the normal response is %s.%n&quot;
     *             + &quot;The synonym map name is %s. The synonyms are %s.%n&quot;, updatedSynonymMap.getStatusCode&#40;&#41;,
     *         updatedSynonymMap.getValue&#40;&#41;.getName&#40;&#41;, updatedSynonymMap.getValue&#40;&#41;.getSynonyms&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context -->
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
                .createOrUpdateWithResponseAsync(synonymMap.getName(), synonymMap, ifMatch, null, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMap#String -->
     * <pre>
     * searchIndexAsyncClient.deleteSynonymMap&#40;&quot;synonymMap&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMap#String -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#SynonymMap-boolean -->
     * <pre>
     * searchIndexAsyncClient.getSynonymMap&#40;&quot;synonymMap&quot;&#41;
     *     .flatMap&#40;synonymMap -&gt; searchIndexAsyncClient.deleteSynonymMapWithResponse&#40;synonymMap, true&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;The status code of the response is&quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#SynonymMap-boolean -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatistics -->
     * <pre>
     * searchIndexAsyncClient.getServiceStatistics&#40;&#41;
     *     .subscribe&#40;serviceStatistics -&gt; System.out.printf&#40;&quot;There are %s search indexes in your service.%n&quot;,
     *         serviceStatistics.getCounters&#40;&#41;.getIndexCounter&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatistics -->
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
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse -->
     * <pre>
     * searchIndexAsyncClient.getServiceStatisticsWithResponse&#40;&#41;
     *     .subscribe&#40;serviceStatistics -&gt;
     *         System.out.printf&#40;&quot;The status code of the response is %s.%n&quot;
     *                 + &quot;There are %s search indexes in your service.%n&quot;,
     *         serviceStatistics.getStatusCode&#40;&#41;,
     *         serviceStatistics.getValue&#40;&#41;.getCounters&#40;&#41;.getIndexCounter&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse -->
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
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
