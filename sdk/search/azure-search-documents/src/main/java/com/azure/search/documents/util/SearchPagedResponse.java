// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.search.documents.implementation.util.SemanticSearchResultsAccessHelper;
import com.azure.search.documents.models.DebugInfo;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticErrorReason;
import com.azure.search.documents.models.SemanticQueryRewritesResultType;
import com.azure.search.documents.models.SemanticSearchResults;
import com.azure.search.documents.models.SemanticSearchResultsType;

import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP response from the search API request that contains a list of items deserialized into a {@link
 * Page}. Each page contains additional information returned by the API request. In the Search API case the additional
 * information is: count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set
 * to true coverage - coverage value.
 */
@Immutable
public final class SearchPagedResponse extends PagedResponseBase<Void, SearchResult> {
    private final List<SearchResult> value;

    private final Long count;
    private final Double coverage;
    private final Map<String, List<FacetResult>> facets;
    private final SemanticSearchResults semanticSearchResults;
    private final DebugInfo debugInfo;

    /**
     * Constructor
     *
     * @param response The response containing information such as the request, status code, headers, and values.
     * @param continuationToken Continuation token for the next operation.
     * @param facets Facets contained in the search.
     * @param count Total number of documents available as a result for the search.
     * @param coverage Percent of the index used in the search operation.
     * @deprecated Use {@link SearchPagedResponse#SearchPagedResponse(Response, String, Map, Long, Double, List, SemanticErrorReason, SemanticSearchResultsType, DebugInfo, SemanticQueryRewritesResultType)}
     */
    @Deprecated
    public SearchPagedResponse(Response<List<SearchResult>> response, String continuationToken,
        Map<String, List<FacetResult>> facets, Long count, Double coverage) {
        this(response, continuationToken, facets, count, coverage, null, null, null);
    }

    /**
     * Constructor
     *
     * @param response The response containing information such as the request, status code, headers, and values.
     * @param continuationToken Continuation token for the next operation.
     * @param facets Facets contained in the search.
     * @param count Total number of documents available as a result for the search.
     * @param coverage Percent of the index used in the search operation.
     * @param queryAnswers Answers contained in the search.
     * @param semanticErrorReason Reason that a partial response was returned for a semantic search request.
     * @param semanticSearchResultsType Type of the partial response returned for a semantic search request.
     * @deprecated Use {@link SearchPagedResponse#SearchPagedResponse(Response, String, Map, Long, Double, List, SemanticErrorReason, SemanticSearchResultsType, DebugInfo, SemanticQueryRewritesResultType)}
     */
    @Deprecated
    public SearchPagedResponse(Response<List<SearchResult>> response, String continuationToken,
        Map<String, List<FacetResult>> facets, Long count, Double coverage, List<QueryAnswerResult> queryAnswers,
        SemanticErrorReason semanticErrorReason, SemanticSearchResultsType semanticSearchResultsType) {
        this(response, continuationToken, facets, count, coverage, queryAnswers, semanticErrorReason,
            semanticSearchResultsType, null, null);
    }

    /**
     * Constructor
     *
     * @param response The response containing information such as the request, status code, headers, and values.
     * @param continuationToken Continuation token for the next operation.
     * @param facets Facets contained in the search.
     * @param count Total number of documents available as a result for the search.
     * @param coverage Percent of the index used in the search operation.
     * @param queryAnswers Answers contained in the search.
     * @param semanticErrorReason Reason that a partial response was returned for a semantic search request.
     * @param semanticSearchResultsType Type of the partial response returned for a semantic search request.
     * @param debugInfo Debug information that applies to the search results as a whole.
     * @param semanticQueryRewritesResultType Type of the partial response returned for a semantic query rewrites request.
     */
    public SearchPagedResponse(Response<List<SearchResult>> response, String continuationToken,
        Map<String, List<FacetResult>> facets, Long count, Double coverage, List<QueryAnswerResult> queryAnswers,
        SemanticErrorReason semanticErrorReason, SemanticSearchResultsType semanticSearchResultsType,
        DebugInfo debugInfo, SemanticQueryRewritesResultType semanticQueryRewritesResultType) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(),
            continuationToken, null);

        this.value = response.getValue();
        this.facets = facets;
        this.count = count;
        this.coverage = coverage;
        this.semanticSearchResults = SemanticSearchResultsAccessHelper.create(queryAnswers, semanticErrorReason,
            semanticSearchResultsType, semanticQueryRewritesResultType);
        this.debugInfo = debugInfo;
    }

    /**
     * Get the count property: The total count of results found by the search operation, or null if the count was not
     * requested. If present, the count may be greater than the number of results in this response. This can happen if
     * you use the $top or $skip parameters, or if the query can't return all the requested documents in a single
     * response.
     *
     * @return the count value.
     */
    public Long getCount() {
        return this.count;
    }

    /**
     * Get the coverage property: A value indicating the percentage of the index that was included in the query, or null
     * if minimumCoverage was not specified in the request.
     *
     * @return the coverage value.
     */
    public Double getCoverage() {
        return this.coverage;
    }

    /**
     * Get the facets property: The facet query results for the search operation, organized as a collection of buckets
     * for each faceted field; null if the query did not include any facet expressions.
     *
     * @return the facets value.
     */
    public Map<String, List<FacetResult>> getFacets() {
        return this.facets;
    }

    /**
     * The semantic search results based on the search request.
     * <p>
     * If semantic search wasn't requested this will return a {@link SemanticSearchResults} with no values.
     *
     * @return The semantic search results if semantic search was requested, otherwise an empty
     * {@link SemanticSearchResults}.
     */
    public SemanticSearchResults getSemanticResults() {
        return semanticSearchResults;
    }

    /**
     * Get the debugInfo property: Debug information that applies to the search results as a whole.
     *
     * @return the debugInfo value.
     */
    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    @Override
    public List<SearchResult> getValue() {
        return value;
    }
}
