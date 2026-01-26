// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.core.util.paging.PageRetrieverSync;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.implementation.models.SearchPostOptions;

import java.util.function.Supplier;

/**
 * Response type for {@link SearchClient#search(SearchPostOptions)}.
 */
public final class SearchPagedIterable
    extends ContinuablePagedIterable<SearchContinuationToken, SearchResult, SearchPagedResponse> {

    /**
     * Creates a new instance of {@link SearchPagedIterable}.
     *
     * @param pageRetrieverProvider The {@link Supplier} that returns the {@link PageRetrieverSync} that iterates over
     * the paged results of searching.
     */
    public SearchPagedIterable(
        Supplier<PageRetrieverSync<SearchContinuationToken, SearchPagedResponse>> pageRetrieverProvider) {
        super(pageRetrieverProvider, null, null);
    }
}
