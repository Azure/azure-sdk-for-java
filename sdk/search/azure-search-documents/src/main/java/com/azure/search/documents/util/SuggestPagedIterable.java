// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
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

    /**
     * Creates an instance of {@link SuggestPagedIterable}. The constructor takes a {@code Supplier}. The
     * {@code Supplier} returns the first page of {@code SuggestPagedResponse}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     */
    public SuggestPagedIterable(Supplier<SuggestPagedResponse> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link SuggestPagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code SuggestPagedResponse}, the {@code Function} retrieves subsequent pages of {@code
     * SuggestPagedResponse}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public SuggestPagedIterable(Supplier<SuggestPagedResponse> firstPageRetriever,
                               Function<String, SuggestPagedResponse> nextPageRetriever) {
        super(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken));
    }
}
