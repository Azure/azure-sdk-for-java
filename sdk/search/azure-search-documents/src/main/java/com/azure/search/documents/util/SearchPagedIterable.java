// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.search.documents.models.SearchRequest;
import com.azure.search.documents.models.SearchResult;

/**
 * Implementation of {@link ContinuablePagedIterable} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedIterable extends PagedIterableBase<SearchResult, SearchPagedResponse> {
    /**
     * Creates an instance of {@link SearchPagedIterable}.
     *
     * @param pagedFlux The {@link SearchPagedFlux} that will be consumed as an iterable.
     */
    public SearchPagedIterable(SearchPagedFlux pagedFlux) {
        super(pagedFlux);
    }
}
