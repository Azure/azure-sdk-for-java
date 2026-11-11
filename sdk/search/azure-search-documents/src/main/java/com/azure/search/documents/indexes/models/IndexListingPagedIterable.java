// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.paging.PageRetrieverSync;

import java.util.function.Supplier;

/**
 * A synchronous listing whose pages retain optional total-count metadata.
 *
 * @param <T> the type of items in the listing.
 */
public final class IndexListingPagedIterable<T> extends PagedIterableBase<T, IndexListingPagedResponse<T>> {
    /**
     * Creates a lazy paged listing.
     *
     * @param pageRetrieverProvider supplies a retriever for each iteration.
     */
    public IndexListingPagedIterable(
        Supplier<PageRetrieverSync<String, IndexListingPagedResponse<T>>> pageRetrieverProvider) {
        super(pageRetrieverProvider);
    }
}
