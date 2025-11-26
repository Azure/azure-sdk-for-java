// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.models.DebugInfo;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticSearchResults;

import reactor.core.publisher.Mono;

/**
 * Implementation of {@link ContinuablePagedFlux} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedFlux extends PagedFluxBase<SearchResult, SearchPagedResponse> {
    private final Supplier<Mono<SearchFirstPageResponseWrapper>> metadataSupplier;

    /**
     * Creates an instance of {@link SearchPagedFlux}.
     *
     * @param firstPageRetriever Supplied that handles retrieving {@link SearchPagedResponse SearchPagedResponses}.
     */
    public SearchPagedFlux(Supplier<Mono<SearchPagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
        metadataSupplier = () -> firstPageRetriever.get()
            .map(response -> new SearchFirstPageResponseWrapper().setFirstPageResponse(response));
    }

    /**
     * Creates an instance of {@link SearchPagedFlux}.
     *
     * @param firstPageRetriever Supplied that handles retrieving {@link SearchPagedResponse SearchPagedResponses}.
     * @param nextPageRetriever Function that retrieves the next {@link SearchPagedResponse SearchPagedResponses} given
     * a continuation token.
     */
    public SearchPagedFlux(Supplier<Mono<SearchPagedResponse>> firstPageRetriever,
        Function<String, Mono<SearchPagedResponse>> nextPageRetriever) {
        super(firstPageRetriever, nextPageRetriever);
        metadataSupplier = () -> firstPageRetriever.get()
            .map(response -> new SearchFirstPageResponseWrapper().setFirstPageResponse(response));
    }

    /**
     * The approximate number of documents that matched the search and filter parameters in the request.
     * <p>
     * If {@code count} is set to {@code false} in the request this will be {@code null}.
     *
     * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
     * {@code null}.
     * @deprecated Use {@link SearchPagedResponse#getCount()} when consuming {@link #byPage()}.
     */
    @Deprecated
    public Mono<Long> getTotalCount() {
        return metadataSupplier.get().flatMap(metaData -> {
            if (metaData.getFirstPageResponse().getCount() == null) {
                return Mono.empty();
            }
            return Mono.just(metaData.getFirstPageResponse().getCount());
        });
    }

    /**
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     * @deprecated Use {@link SearchPagedResponse#getCoverage()} when consuming {@link #byPage()}.
     */
    @Deprecated
    public Mono<Double> getCoverage() {
        return metadataSupplier.get().flatMap(metaData -> {
            if (metaData.getFirstPageResponse().getCoverage() == null) {
                return Mono.empty();
            }
            return Mono.just(metaData.getFirstPageResponse().getCoverage());
        });
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be {@code null}.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise {@code null}.
     * @deprecated Use {@link SearchPagedResponse#getFacets()} when consuming {@link #byPage()}.
     */
    @Deprecated
    public Mono<Map<String, List<FacetResult>>> getFacets() {
        return metadataSupplier.get().flatMap(metaData -> {
            if (metaData.getFirstPageResponse().getFacets() == null) {
                return Mono.empty();
            }
            return Mono.just(metaData.getFirstPageResponse().getFacets());
        });
    }

    /**
     * The semantic search results based on the search request.
     * <p>
     * If semantic search wasn't requested this will return a {@link SemanticSearchResults} with no values.
     *
     * @return The semantic search results if semantic search was requested, otherwise an empty
     * {@link SemanticSearchResults}.
     * @deprecated Use {@link SearchPagedResponse#getSemanticResults()} when consuming {@link #byPage()}.
     */
    @Deprecated
    public Mono<SemanticSearchResults> getSemanticResults() {
        return metadataSupplier.get().map(metadata -> metadata.getFirstPageResponse().getSemanticResults());
    }

    /**
     * The debug information that can be used to further explore your search results.
     *
     * @return The debug information that can be used to further explore your search results.
     * @deprecated Use {@link SearchPagedResponse#getDebugInfo()} when consuming {@link #byPage()}.
     */
    @Deprecated
    public Mono<DebugInfo> getDebugInfo() {
        return metadataSupplier.get().flatMap(metaData -> {
            if (metaData.getFirstPageResponse().getDebugInfo() == null) {
                return Mono.empty();
            }
            return Mono.just(metaData.getFirstPageResponse().getDebugInfo());
        });
    }
}
