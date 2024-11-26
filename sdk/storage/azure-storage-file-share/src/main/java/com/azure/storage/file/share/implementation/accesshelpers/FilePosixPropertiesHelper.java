// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.core.http.HttpHeaders;
import com.azure.storage.file.share.models.FilePosixProperties;

/**
 * Helper class to access private values of {@link FilePosixProperties} across package boundaries.
 */
public final class FilePosixPropertiesHelper {

    private static FilePosixPropertiesAccessor accessor;

    private FilePosixPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link FilePosixProperties} instance.
     */
    public interface FilePosixPropertiesAccessor {
        /**
         * Creates a new instance of {@link FilePosixProperties} backed by an internal instance of
         * {@link FilePosixProperties}.
         *
         * @param httpHeaders The internal headers.
         * @return A new instance of {@link FilePosixProperties}.
         */
        FilePosixProperties create(HttpHeaders httpHeaders);
    }

    /**
     * The method called from {@link FilePosixProperties} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final FilePosixPropertiesAccessor accessor) {
        FilePosixPropertiesHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link FilePosixProperties} backed by an internal instance of
     * {@link FilePosixProperties}.
     *
     * @param httpHeaders The internal headers.
     * @return A new instance of {@link FilePosixProperties}.
     */
    public static FilePosixProperties create(HttpHeaders httpHeaders) {
        if (accessor == null) {
            new FilePosixProperties();
        }

        assert accessor != null;
        return accessor.create(httpHeaders);
    }
}
