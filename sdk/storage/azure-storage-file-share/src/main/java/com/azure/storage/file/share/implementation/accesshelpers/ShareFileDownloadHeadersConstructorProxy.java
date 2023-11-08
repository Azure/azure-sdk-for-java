// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.implementation.models.FilesDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;

/**
 * Helper class to access private values of {@link ShareFileDownloadHeaders} across package boundaries.
 */
public final class ShareFileDownloadHeadersConstructorProxy {
    private static ShareFileDownloadHeadersConstructorAccessor accessor;

    private ShareFileDownloadHeadersConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link ShareFileDownloadHeaders} instance.
     */
    public interface ShareFileDownloadHeadersConstructorAccessor {
        /**
         * Creates a new instance of {@link ShareFileDownloadHeaders} backed by an internal instance of
         * {@link ShareFileDownloadHeaders}.
         *
         * @param internalHeaders The internal headers.
         * @return A new instance of {@link ShareFileDownloadHeaders}.
         */
        ShareFileDownloadHeaders create(FilesDownloadHeaders internalHeaders);
    }

    /**
     * The method called from {@link ShareFileDownloadHeaders} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ShareFileDownloadHeadersConstructorAccessor accessor) {
        ShareFileDownloadHeadersConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileDownloadHeaders} backed by an internal instance of
     * {@link ShareFileDownloadHeaders}.
     *
     * @param internalHeaders The internal headers.
     * @return A new instance of {@link ShareFileDownloadHeaders}.
     */
    public static ShareFileDownloadHeaders create(FilesDownloadHeaders internalHeaders) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileDownloadHeaders();
        }

        assert accessor != null;
        return accessor.create(internalHeaders);
    }
}
