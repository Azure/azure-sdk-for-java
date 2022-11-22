// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.paging.PageRetrieverSync;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.models.SuggestResult;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of {@link PagedIterableBase} where the element type is {@link SuggestResult} and the page type is
 * {@link SuggestPagedResponse}.
 */
public final class SuggestPagedIterable extends PagedIterableBase<SuggestResult, SuggestPagedResponse> {
    /**
     * Creates instance given {@link SuggestPagedIterable}.
     *
     * @param pagedFluxBase The {@link SuggestPagedIterable} that will be consumed as an iterable.
     */
    public SuggestPagedIterable(SuggestPagedFlux pagedFluxBase) {
        super(pagedFluxBase);
    }

    public SuggestPagedIterable(Supplier<SuggestPagedResponse> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link PagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public SuggestPagedIterable(Supplier<SuggestPagedResponse> firstPageRetriever,
                               Function<String, SuggestPagedResponse> nextPageRetriever) {
        this(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken), true);
    }

    /**
     * Create PagedIterable backed by Page Retriever Function Supplier.
     *
     * @param provider the Page Retrieval Provider
     * @param ignored param is ignored, exists in signature only to avoid conflict with first ctr
     */
    private SuggestPagedIterable(Supplier<PageRetrieverSync<String, SuggestPagedResponse>> provider, boolean ignored) {
        super(provider);
    }
}
