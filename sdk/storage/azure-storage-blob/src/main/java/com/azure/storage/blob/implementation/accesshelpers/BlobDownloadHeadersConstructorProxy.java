// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadHeaders;

/**
 * Helper class to access private values of {@link BlobDownloadHeaders} across package boundaries.
 */
public final class BlobDownloadHeadersConstructorProxy {
    private static BlobDownloadHeadersConstructorAccessor accessor;

    private BlobDownloadHeadersConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlobDownloadHeadersConstructorAccessor}
     * instance.
     */
    public interface BlobDownloadHeadersConstructorAccessor {
        /**
         * Creates a new instance of {@link BlobDownloadHeaders} backed by an internal instance of
         * {@link BlobsDownloadHeaders}.
         *
         * @param internalHeaders The internal headers.
         * @return A new instance of {@link BlobDownloadHeaders}.
         */
        BlobDownloadHeaders create(BlobsDownloadHeaders internalHeaders);
    }

    /**
     * The method called from {@link BlobDownloadHeaders} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobDownloadHeadersConstructorAccessor accessor) {
        BlobDownloadHeadersConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlobDownloadHeaders} backed by an internal instance of
     * {@link BlobsDownloadHeaders}.
     *
     * @param internalHeaders The internal headers.
     * @return A new instance of {@link BlobDownloadHeaders}.
     */
    public static BlobDownloadHeaders create(BlobsDownloadHeaders internalHeaders) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobDownloadHeaders();
        }

        assert accessor != null;
        return accessor.create(internalHeaders);
    }
}
