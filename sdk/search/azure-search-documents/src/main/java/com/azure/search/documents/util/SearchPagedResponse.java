// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.search.documents.implementation.util.SearchPagedResponseAccessHelper;
import com.azure.search.documents.models.DebugInfo;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticErrorReason;
import com.azure.search.documents.models.SemanticQueryRewritesResultType;
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
    private final List<QueryAnswerResult> queryAnswers;
    private final DebugInfo debugInfo;
    private final SemanticErrorReason semanticErrorReason;
    private final SemanticSearchResultsType semanticSearchResultsType;
    private final SemanticQueryRewritesResultType semanticQueryRewritesResultType;

    static {
        SearchPagedResponseAccessHelper.setAccessor(new SearchPagedResponseAccessHelper.SearchPagedResponseAccessor() {
            @Override
            public Double getCoverage(SearchPagedResponse response) {
                return response.getCoverage();
            }

            @Override
            public Map<String, List<FacetResult>> getFacets(SearchPagedResponse response) {
                return response.getFacets();
            }

            @Override
            public Long getCount(SearchPagedResponse response) {
                return response.getCount();
            }

            @Override
            public List<QueryAnswerResult> getQueryAnswers(SearchPagedResponse response) {
                return response.getQueryAnswers();
            }

            @Override
            public SemanticErrorReason getSemanticErrorReason(SearchPagedResponse response) {
                return response.getSemanticErrorReason();
            }

            @Override
            public SemanticSearchResultsType getSemanticSearchResultsType(SearchPagedResponse response) {
                return response.getSemanticSearchResultsType();
            }

            @Override
            public DebugInfo getDebugInfo(SearchPagedResponse response) {
                return response.getDebugInfo();
            }

            @Override
            public SemanticQueryRewritesResultType getSemanticQueryRewritesResultType(SearchPagedResponse response) {
                return response.getSemanticQueryRewritesResultType();
            }

        });
    }

    /**
     * Constructor
     *
     * @param response The response containing information such as the request, status code, headers, and values.
     * @param continuationToken Continuation token for the next operation.
     * @param facets Facets contained in the search.
     * @param count Total number of documents available as a result for the search.
     * @param coverage Percent of the index used in the search operation.
     */
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
     */
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
        this.queryAnswers = queryAnswers;
        this.semanticErrorReason = semanticErrorReason;
        this.semanticSearchResultsType = semanticSearchResultsType;
        this.debugInfo = debugInfo;
        this.semanticQueryRewritesResultType = semanticQueryRewritesResultType;
    }

    /**
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be null.
     *
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise null.
     */
    Double getCoverage() {
        return coverage;
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be null.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise null.
     */
    Map<String, List<FacetResult>> getFacets() {
        return facets;
    }

    /**
     * The approximate number of documents that matched the search and filter parameters in the request.
     * <p>
     * If {@code count} is set to {@code false} in the request this will be null.
     *
     * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
     * null.
     */
    Long getCount() {
        return count;
    }

    /**
     * The answer results based on the search request.
     * <p>
     * If {@code answers} wasn't supplied in the request this will be null.
     *
     * @return The answer results if {@code answers} were supplied in the request, otherwise null.
     */
    List<QueryAnswerResult> getQueryAnswers() {
        return queryAnswers;
    }

    /**
     * The reason that a partial response was returned for a semantic search request.
     *
     * @return Reason that a partial response was returned for a semantic search request if response was partial.
     */
    SemanticErrorReason getSemanticErrorReason() {
        return semanticErrorReason;
    }

    /**
     * The type of the partial response returned for a semantic search request.
     *
     * @return Type of the partial response returned for a semantic search request if response was partial.
     */
    SemanticSearchResultsType getSemanticSearchResultsType() {
        return semanticSearchResultsType;
    }

    /**
     * The debug information that can be used to further explore your search results.
     *
     * @return The debug information that can be used to further explore your search results.
     */
    DebugInfo getDebugInfo() {
        return debugInfo;
    }

    /**
     * The type of the partial response returned for a semantic query rewrites request.
     *
     * @return Type of the partial response returned for a semantic query rewrites request if response was partial.
     */
    SemanticQueryRewritesResultType getSemanticQueryRewritesResultType() {
        return semanticQueryRewritesResultType;
    }

    @Override
    public List<SearchResult> getValue() {
        return value;
    }
}
