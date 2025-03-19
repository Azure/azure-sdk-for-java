// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.search.documents.implementation.util.SearchPagedResponseAccessHelper;
import com.azure.search.documents.implementation.util.SemanticSearchResultsAccessHelper;
import com.azure.search.documents.util.SearchPagedResponse;

import java.util.List;

/**
 * The page-level results for a {@link QueryType#SEMANTIC semantic} search.
 */
public final class SemanticSearchResults {
    private final List<QueryAnswerResult> queryAnswers;
    private final SemanticErrorReason errorReason;
    private final SemanticSearchResultsType resultsType;

    static {
        SemanticSearchResultsAccessHelper.setAccessor(SemanticSearchResults::new);
    }

    private SemanticSearchResults(SearchPagedResponse pagedResponse) {
        this.queryAnswers = SearchPagedResponseAccessHelper.getQueryAnswers(pagedResponse);
        this.errorReason = SearchPagedResponseAccessHelper.getSemanticErrorReason(pagedResponse);
        this.resultsType = SearchPagedResponseAccessHelper.getSemanticSearchResultsType(pagedResponse);
    }

    /**
     * The answer results based on the search request.
     * <p>
     * If {@code answers} wasn't supplied in the request this will be null.
     *
     * @return The answer results if {@code answers} were supplied in the request, otherwise null.
     */
    public List<QueryAnswerResult> getQueryAnswers() {
        return this.queryAnswers;
    }

    /**
     * The reason for a partial result returned by Azure AI Search.
     *
     * @return The reason for a partial result returned by Azure AI Search.
     */
    public SemanticErrorReason getErrorReason() {
        return this.errorReason;
    }

    /**
     * The type of the partial result returned by Azure AI Search.
     *
     * @return The type of the partial result returned by Azure AI Search.
     */
    public SemanticSearchResultsType getResultsType() {
        return this.resultsType;
    }
}
