// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.models.AnswerResult;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchResult;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ContinuablePagedIterable} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedIterable extends PagedIterableBase<SearchResult, SearchPagedResponse> {
    private final SearchPagedFlux pagedFlux;

    /**
     * Creates an instance of {@link SearchPagedIterable}.
     *
     * @param pagedFlux The {@link SearchPagedFlux} that will be consumed as an iterable.
     */
    public SearchPagedIterable(SearchPagedFlux pagedFlux) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
    }

    /**
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     */
    public Double getCoverage() {
        return pagedFlux.getCoverage().block();
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be {@code null}.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise {@code null}.
     */
    public Map<String, List<FacetResult>> getFacets() {
        return pagedFlux.getFacets().block();
    }

    /**
     * The approximate number of documents that matched the search and filter parameters in the request.
     * <p>
     * If {@code count} is set to {@code false} in the request this will be {@code null}.
     *
     * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
     * {@code null}.
     */
    public Long getTotalCount() {
        return pagedFlux.getTotalCount().block();
    }

    /**
     * The answer results based on the search request.
     * <p>
     * If {@code answers} wasn't supplied in the request this will be null.
     *
     * @return The answer results if {@code answers} were supplied in the request, otherwise null.
     */
    public List<AnswerResult> getAnswers() {
        return pagedFlux.getAnswers().block();
    }
}
