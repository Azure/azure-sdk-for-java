// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.search.documents.models.SearchRequest;
import com.azure.search.documents.models.SearchResult;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of {@link ContinuablePagedFlux} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedFlux extends PagedFluxBase<SearchResult, SearchPagedResponse> {
    /**
     * Creates an instance of {@link SearchPagedFlux}.
     *
     * @param firstPageRetriever Supplied that handles retrieving {@link SearchPagedResponse SearchPagedResponses}.
     */
    public SearchPagedFlux(Supplier<Mono<SearchPagedResponse>> firstPageRetriever) {
        super(firstPageRetriever);
    }

    /**
     * Creates an instance of {@link SearchPagedFlux}.
     *
     * @param firstPageRetriever Supplied that handles retrieving {@link SearchPagedResponse SearchPagedResponses}.
     * @param nextPageRetriever Function that retrieves the next {@link SearchPagedResponse SearchPagedResponses}
     * given a continuation token.
     */
    public SearchPagedFlux(Supplier<Mono<SearchPagedResponse>> firstPageRetriever,
        Function<String, Mono<SearchPagedResponse>> nextPageRetriever) {
        super(firstPageRetriever, nextPageRetriever);
    }
}
