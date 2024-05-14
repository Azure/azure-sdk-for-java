// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.core.http.HttpHeaders;
import com.azure.storage.file.share.FileSmbProperties;

/**
 * Helper class to access private values of {@link FileSmbProperties} across package boundaries.
 */
public final class FileSmbPropertiesHelper {

    private static FileSmbPropertiesAccessor accessor;

    private FileSmbPropertiesHelper() { }


    /**
     * Type defining the methods to set the non-public properties of a {@link FileSmbProperties} instance.
     */
    public interface FileSmbPropertiesAccessor {
        /**
         * Creates a new instance of {@link FileSmbProperties} backed by an internal instance of
         * {@link FileSmbProperties}.
         *
         * @param httpHeaders The internal headers.
         * @return A new instance of {@link FileSmbProperties}.
         */
        FileSmbProperties create(HttpHeaders httpHeaders);
    }

    /**
     * The method called from {@link FileSmbProperties} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final FileSmbPropertiesAccessor accessor) {
        FileSmbPropertiesHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link FileSmbProperties} backed by an internal instance of
     * {@link FileSmbProperties}.
     *
     * @param httpHeaders The internal headers.
     * @return A new instance of {@link FileSmbProperties}.
     */
    public static FileSmbProperties create(HttpHeaders httpHeaders) {
        if (accessor == null) {
            new FileSmbProperties();
        }

        assert accessor != null;
        return accessor.create(httpHeaders);
    }
}
