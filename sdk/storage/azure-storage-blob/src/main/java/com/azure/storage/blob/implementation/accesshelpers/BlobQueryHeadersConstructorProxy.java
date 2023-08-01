// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.implementation.models.BlobsQueryHeaders;
import com.azure.storage.blob.models.BlobQueryHeaders;

/**
 * Helper class to access private values of {@link BlobQueryHeaders} across package boundaries.
 */
public final class BlobQueryHeadersConstructorProxy {
    private static BlobQueryHeadersConstructorAccessor accessor;

    private BlobQueryHeadersConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlobQueryHeaders} instance.
     */
    public interface BlobQueryHeadersConstructorAccessor {
        /**
         * Creates a new instance of {@link BlobQueryHeaders} backed by an internal instance of
         * {@link BlobsQueryHeaders}.
         *
         * @param internalHeader The internal headers.
         * @return A new instance of {@link BlobQueryHeaders}.
         */
        BlobQueryHeaders create(BlobsQueryHeaders internalHeader);
    }

    /**
     * The method called from {@link BlobQueryHeaders} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobQueryHeadersConstructorAccessor accessor) {
        BlobQueryHeadersConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlobQueryHeaders} backed by an internal instance of
     * {@link BlobsQueryHeaders}.
     *
     * @param internalHeaders The internal headers.
     * @return A new instance of {@link BlobQueryHeaders}.
     */
    public static BlobQueryHeaders create(BlobsQueryHeaders internalHeaders) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobQueryHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobQueryHeaders();
        }

        assert accessor != null;
        return accessor.create(internalHeaders);
    }
}
