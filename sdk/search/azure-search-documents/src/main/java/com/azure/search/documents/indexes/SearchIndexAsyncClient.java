// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.models.AnalyzeRequest;
import com.azure.search.documents.models.AnalyzedTokenInfo;
import com.azure.search.documents.models.GetIndexStatisticsResult;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndex;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous Client to manage and query search index, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchIndexAsyncClient {
    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    SearchIndexAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.restClient = new SearchServiceRestClientBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create.
     * @return the created Index.
     */
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
    public Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index, RequestOptions requestOptions) {
        return withContext(context -> createIndexWithResponse(index, requestOptions, context));
    }

    Mono<Response<SearchIndex>> createIndexWithResponse(SearchIndex index, RequestOptions requestOptions,
        Context context) {
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
    public Mono<Response<SearchIndex>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> getIndexWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<SearchIndex>> getIndexWithResponse(String indexName, RequestOptions requestOptions, Context context) {
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
    public PagedFlux<SearchIndex> listIndexes() {
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
    public PagedFlux<SearchIndex> listIndexes(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndex> listIndexes(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndex>> listIndexesWithResponse(String select, RequestOptions requestOptions,
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
     * @param index the definition of the {@link SearchIndex} to create or update.
     * @return the index that was created or updated.
     */
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
        return withContext(context -> deleteIndexWithResponse(indexName, null, null, null)
            .flatMap(FluxUtil::toMono));
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
     * List all SearchIndex names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndex names.
     */
    public PagedFlux<SearchIndex> listSearchIndexNames() {
        return listSearchIndexNames(null);
    }

    /**
     * List all SearchIndex names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a list of SearchIndex names
     */
    public PagedFlux<SearchIndex> listSearchIndexNames(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexesWithResponse("name", requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndex> listSearchIndexNames(RequestOptions requestOptions,
        Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexesWithResponse("name", requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

}
