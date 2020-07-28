// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.search.documents.util.SearchPagedResponse;

/**
 * This class is a wrapper on first page response from search result.
 */
public class SearchFirstPageResponseWrapper {
    private SearchPagedResponse firstPageResponse;

    /**
     * Gets the first page response.
     *
     * @return The first page response.
     */
    public SearchPagedResponse getFirstPageResponse() {
        return firstPageResponse;
    }

    /**
     * Sets the first page response.
     *
     * @param firstPageResponse The first page response.
     * @return The {@link SearchFirstPageResponseWrapper} object itself.
     */
    public SearchFirstPageResponseWrapper setFirstPageResponse(SearchPagedResponse firstPageResponse) {
        this.firstPageResponse = firstPageResponse;
        return this;
    }
}
