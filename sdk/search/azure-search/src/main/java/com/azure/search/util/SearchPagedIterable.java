// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.util;

import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;

/**
 * Implementation of {@link ContinuablePagedIterable} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedIterable extends ContinuablePagedIterable<SearchRequest, SearchResult,
    SearchPagedResponse> {
    /**
     * Creates an instance of {@link SearchPagedIterable}.
     *
     * @param pagedFlux The {@link SearchPagedFlux} that will be consumed as an iterable.
     */
    public SearchPagedIterable(SearchPagedFlux pagedFlux) {
        super(pagedFlux);
    }
}
