// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.core.http.rest.PagedFlux;
import com.azure.storage.blob.models.BlobLayoutInfo;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;

/**
 * Helper class to access the layout retrieval methods of {@link BlobAsyncClientBase} across package boundaries.
 */
public final class BlobLayoutAccessor {
    private static BlobLayoutAccessorImpl accessor;

    private BlobLayoutAccessor() {
    }

    /**
     * Interface defining the methods to retrieve layout information from a blob.
     */
    public interface BlobLayoutAccessorImpl {
        /**
         * Gets the layout of a blob.
         *
         * @param blobAsyncClientBase The blob async client base.
         * @param options The options for getting the layout.
         * @return A paged flux of blob layout information.
         */
        PagedFlux<BlobLayoutInfo> getLayout(BlobAsyncClientBase blobAsyncClientBase, BlobGetLayoutOptions options);
    }

    /**
     * The method called from {@link BlobAsyncClientBase} to set its accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobLayoutAccessorImpl accessor) {
        BlobLayoutAccessor.accessor = accessor;
    }

    /**
     * Gets the layout of a blob.
     *
     * @param blobAsyncClientBase The blob async client base.
     * @param options The options for getting the layout.
     * @return A paged flux of blob layout information.
     */
    public static PagedFlux<BlobLayoutInfo> getLayout(BlobAsyncClientBase blobAsyncClientBase,
        BlobGetLayoutOptions options) {
        assert accessor != null : "BlobLayoutAccessor.setAccessor() must be called before getLayout()";
        return accessor.getLayout(blobAsyncClientBase, options);
    }
}
