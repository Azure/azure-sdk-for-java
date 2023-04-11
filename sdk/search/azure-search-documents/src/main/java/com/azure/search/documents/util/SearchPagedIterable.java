// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.core.util.paging.PageRetrieverSync;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.models.AnswerResult;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of {@link ContinuablePagedIterable} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedIterable extends PagedIterableBase<SearchResult, SearchPagedResponse> {
    private final SearchPagedFlux pagedFlux;
    private final Supplier<SearchFirstPageResponseWrapper> metadataSupplier;

    /**
     * Creates an instance of {@link SearchPagedIterable}.
     *
     * @param pagedFlux The {@link SearchPagedFlux} that will be consumed as an iterable.
     */
    public SearchPagedIterable(SearchPagedFlux pagedFlux) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
        this.metadataSupplier = null;
    }

    /**
     * Creates an instance of {@link SearchPagedIterable}. The constructor takes a {@code Supplier}. The
     * {@code Supplier} returns the first page of {@code SearchPagedResponse}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     */
    public SearchPagedIterable(Supplier<SearchPagedResponse> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link SearchPagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code SearchPagedResponse}, the {@code Function} retrieves subsequent pages of {@code
     * SearchPagedResponse}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public SearchPagedIterable(Supplier<SearchPagedResponse> firstPageRetriever,
                               Function<String, SearchPagedResponse> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken), true,
            () -> {
                SearchPagedResponse response = firstPageRetriever.get();
                return new SearchFirstPageResponseWrapper().setFirstPageResponse(response);
            });
    }

    /**
     * Create SearchPagedIterable backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider
     * @param ignored param is ignored, exists in signature only to avoid conflict with first ctr
     */
    private SearchPagedIterable(Supplier<PageRetrieverSync<String, SearchPagedResponse>> provider, boolean ignored, Supplier<SearchFirstPageResponseWrapper> metadataSupplier) {
        super(provider);
        this.pagedFlux = null;
        this.metadataSupplier = metadataSupplier;
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
        return metadataSupplier != null ?  metadataSupplier.get().getFirstPageResponse().getCoverage()
            : pagedFlux.getCoverage().block();
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be {@code null}.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise {@code null}.
     */
    public Map<String, List<FacetResult>> getFacets() {
        return metadataSupplier != null ?  metadataSupplier.get().getFirstPageResponse().getFacets()
            : pagedFlux.getFacets().block();
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
        return metadataSupplier != null ? metadataSupplier.get().getFirstPageResponse().getCount()
            : pagedFlux.getTotalCount().block();
    }

    /**
     * The answer results based on the search request.
     * <p>
     * If {@code answers} wasn't supplied in the request this will be null.
     *
     * @return The answer results if {@code answers} were supplied in the request, otherwise null.
     */
    public List<AnswerResult> getAnswers() {
        return metadataSupplier != null ?  metadataSupplier.get().getFirstPageResponse().getAnswers()
            : pagedFlux.getAnswers().block();
    }
}
