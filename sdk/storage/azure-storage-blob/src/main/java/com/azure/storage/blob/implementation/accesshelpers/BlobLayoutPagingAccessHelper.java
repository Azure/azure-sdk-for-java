// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.PageRetriever;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Provides package-internal access to Blob's stateful layout page retriever.
 *
 * <p>Data Lake decorates individual Blob layout pages without wrapping the public Blob {@code PagedFlux}. Wrapping
 * that pager would eagerly advance it during synchronous iteration and would prevent the decorator from retaining
 * the same per-subscription ETag state.</p>
 */
public final class BlobLayoutPagingAccessHelper {
    private static BlobLayoutPagingAccessor accessor;

    private BlobLayoutPagingAccessHelper() {
    }

    /**
     * Accesses the Blob layout page retriever.
     */
    public interface BlobLayoutPagingAccessor {
        /**
         * Gets a page retriever provider for Blob layout requests.
         *
         * @param client The Blob client.
         * @param options The layout options.
         * @return The page retriever provider.
         */
        Supplier<PageRetriever<String, PagedResponse<BlobLayout>>> getPageRetrieverProvider(BlobAsyncClientBase client,
            BlobGetLayoutOptions options);
    }

    /**
     * Sets the accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(BlobLayoutPagingAccessor accessor) {
        BlobLayoutPagingAccessHelper.accessor = Objects.requireNonNull(accessor, "'accessor' cannot be null.");
    }

    /**
     * Gets a page retriever provider for Blob layout requests.
     *
     * @param client The Blob client.
     * @param options The layout options.
     * @return The page retriever provider.
     */
    public static Supplier<PageRetriever<String, PagedResponse<BlobLayout>>>
        getPageRetrieverProvider(BlobAsyncClientBase client, BlobGetLayoutOptions options) {
        if (accessor == null) {
            throw new IllegalStateException("Blob layout paging accessor has not been initialized.");
        }
        return accessor.getPageRetrieverProvider(client, options);
    }
}
