// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.implementation.models.BlobPropertiesInternal;
import com.azure.storage.blob.models.BlobProperties;

/**
 * Helper class to access private values of {@link BlobProperties} across package boundaries.
 */
public final class BlobPropertiesConstructorProxy {
    private static BlobPropertiesConstructorAccessor accessor;

    private BlobPropertiesConstructorProxy() {
    }

    /**
     * Interface defining the methods that access non-public APIs of a {@link BlobProperties} instance.
     */
    public interface BlobPropertiesConstructorAccessor {
        /**
         * Creates a new instance of {@link BlobProperties} backed by an internal instance of
         * {@link BlobPropertiesInternal}.
         *
         * @param internalProperties The internal properties.
         * @return A new instance of {@link BlobProperties}.
         */
        BlobProperties create(BlobPropertiesInternal internalProperties);
    }

    /**
     * The method called from the static initializer of {@link BlobProperties} to set it's accessor.
     *
     * @param accessor The {@link BlobProperties} accessor.
     */
    public static void setAccessor(final BlobPropertiesConstructorAccessor accessor) {
        BlobPropertiesConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlobProperties} backed by an internal instance of
     * {@link BlobPropertiesInternal}.
     *
     * @param internalProperties The internal properties.
     * @return A new instance of {@link BlobProperties}.
     */
    public static BlobProperties create(BlobPropertiesInternal internalProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobProperties(null, null, null, 0, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        assert accessor != null;
        return accessor.create(internalProperties);
    }
}
