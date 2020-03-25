// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;
import com.azure.search.documents.models.SearchRequest;
import com.azure.search.documents.models.SearchResult;

import java.util.function.Supplier;

/**
 * Implementation of {@link ContinuablePagedFlux} where the continuation token type is {@link SearchRequest}, the
 * element type is {@link SearchResult}, and the page type is {@link SearchPagedResponse}.
 */
public final class SearchPagedFlux extends ContinuablePagedFluxCore<SearchRequest, SearchResult, SearchPagedResponse> {
    /**
     * Creates an instance of {@link SearchPagedFlux}.
     *
     * @param pageRetrieverProvider Supplied that handles retrieving {@link SearchPagedResponse SearchPagedResponses}.
     */
    public SearchPagedFlux(
        Supplier<PageRetriever<SearchRequest, SearchPagedResponse>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
