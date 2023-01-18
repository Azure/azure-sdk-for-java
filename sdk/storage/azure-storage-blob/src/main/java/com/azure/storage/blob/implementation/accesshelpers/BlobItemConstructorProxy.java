// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.models.BlobItem;

/**
 * Helper class to access private values of {@link BlobItem} across package boundaries.
 */
public final class BlobItemConstructorProxy {
    private static BlobItemConstructorAccessor accessor;

    private BlobItemConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlobItem} instance.
     */
    public interface BlobItemConstructorAccessor {
        /**
         * Creates a new instance of {@link BlobItem} backed by an internal instance of
         * {@link BlobItem}.
         *
         * @param blobItemInternal The internal blob item.
         * @return A new instance of {@link BlobItem}.
         */
        BlobItem create(BlobItemInternal blobItemInternal);
    }

    /**
     * The method called from {@link BlobItem} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobItemConstructorAccessor accessor) {
        BlobItemConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlobItem} backed by an internal instance of
     * {@link BlobItem}.
     *
     * @param blobItemInternal The internal blob item.
     * @return A new instance of {@link BlobItem}.
     */
    public static BlobItem create(BlobItemInternal blobItemInternal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobItem();
        }

        assert accessor != null;
        return accessor.create(blobItemInternal);
    }
}
