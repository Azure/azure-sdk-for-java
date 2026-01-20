// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;

import java.util.List;
import java.util.Map;

public final class SearchResultPage implements ContinuablePage<SearchContinuationToken, SearchResult> {
    private final SearchDocumentsResult page;
    private final SearchContinuationToken continuationToken;

    public SearchResultPage(SearchDocumentsResult page, SearchContinuationToken continuationToken) {
        this.page = page;
        this.continuationToken = continuationToken;
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
        return page.getCount();
    }

    /**
     * Get the coverage property: A value indicating the percentage of the index that was included in the query, or null
     * if minimumCoverage was not specified in the request.
     *
     * @return the coverage value.
     */
    public Double getCoverage() {
        return page.getCoverage();
    }

    /**
     * Get the facets property: The facet query results for the search operation, organized as a collection of buckets
     * for each faceted field; null if the query did not include any facet expressions.
     *
     * @return the facets value.
     */
    public Map<String, List<FacetResult>> getFacets() {
        return page.getFacets();
    }

    /**
     * Get the answers property: The answers query results for the search operation; null if the answers query parameter
     * was not specified or set to 'none'.
     *
     * @return the answers value.
     */
    public List<QueryAnswerResult> getAnswers() {
        return page.getAnswers();
    }

    /**
     * Get the debugInfo property: Debug information that applies to the search results as a whole.
     *
     * @return the debugInfo value.
     */
    public DebugInfo getDebugInfo() {
        return page.getDebugInfo();
    }

    /**
     * Get the semanticPartialResponseReason property: Reason that a partial response was returned for a semantic
     * ranking request.
     *
     * @return the semanticPartialResponseReason value.
     */
    public SemanticErrorReason getSemanticPartialResponseReason() {
        return page.getSemanticPartialResponseReason();
    }

    /**
     * Get the semanticPartialResponseType property: Type of partial response that was returned for a semantic ranking
     * request.
     *
     * @return the semanticPartialResponseType value.
     */
    public SemanticSearchResultsType getSemanticPartialResponseType() {
        return page.getSemanticPartialResponseType();
    }

    /**
     * Get the semanticQueryRewritesResultType property: Type of query rewrite that was used to retrieve documents.
     *
     * @return the semanticQueryRewritesResultType value.
     */
    public SemanticQueryRewritesResultType getSemanticQueryRewritesResultType() {
        return page.getSemanticQueryRewritesResultType();
    }

    @Override
    public IterableStream<SearchResult> getElements() {
        return IterableStream.of(page.getResults());
    }

    @Override
    public SearchContinuationToken getContinuationToken() {
        return continuationToken;
    }
}
