// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;
import com.azure.search.models.SearchNextPageParameters;
import com.azure.search.models.SearchResult;

import java.util.function.Supplier;

/**
 * This class is an implementation of {@link ContinuablePagedFlux} that used by {@link
 * SearchIndexClient#search(String)}.
 */
final class SearchSearchPagedFlux extends
    ContinuablePagedFluxCore<SearchNextPageParameters, SearchResult, SearchPagedResponse> {
    protected SearchSearchPagedFlux(
        Supplier<PageRetriever<SearchNextPageParameters, SearchPagedResponse>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
