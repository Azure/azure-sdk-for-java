// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * An asynchronous listing whose pages retain optional total-count metadata.
 *
 * @param <T> the type of items in the listing.
 */
public final class IndexListingPagedFlux<T> extends ContinuablePagedFluxCore<String, T, IndexListingPagedResponse<T>> {
    /**
     * Creates a lazy paged listing.
     *
     * @param pageRetrieverProvider supplies a retriever for each subscription.
     */
    public IndexListingPagedFlux(Supplier<PageRetriever<String, IndexListingPagedResponse<T>>> pageRetrieverProvider) {
        super(pageRetrieverProvider, null, token -> !CoreUtils.isNullOrEmpty(token));
    }
}
