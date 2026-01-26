// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.implementation.models.SearchPostOptions;

import java.util.function.Supplier;

/**
 * Response type for {@link SearchAsyncClient#search(SearchPostOptions)}.
 */
public final class SearchPagedFlux
    extends ContinuablePagedFluxCore<SearchContinuationToken, SearchResult, SearchResultPage> {

    /**
     * Creates a new instance of {@link SearchPagedFlux}.
     *
     * @param pageRetrieverProvider The {@link Supplier} that returns the {@link PageRetriever} that iterates over
     * the paged results of searching.
     */
    public SearchPagedFlux(Supplier<PageRetriever<SearchContinuationToken, SearchResultPage>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
