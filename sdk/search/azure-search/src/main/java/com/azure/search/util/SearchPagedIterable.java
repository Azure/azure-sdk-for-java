// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.util;

import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.search.SearchPagedResponse;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;

/**
 * Implementation of {@link ContinuablePagedIterable} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public class SearchPagedIterable extends ContinuablePagedIterable<SearchRequest, SearchResult, SearchPagedResponse> {
    public SearchPagedIterable(ContinuablePagedFlux<SearchRequest, SearchResult, SearchPagedResponse> pagedFlux) {
        super(pagedFlux);
    }
}
