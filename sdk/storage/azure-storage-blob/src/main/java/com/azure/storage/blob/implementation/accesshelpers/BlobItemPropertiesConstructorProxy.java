// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;

/**
 * Helper class to access private values of {@link BlobItemProperties} across package boundaries.
 */
public final class BlobItemPropertiesConstructorProxy {
    private static BlobItemPropertiesConstructorAccessor accessor;

    private BlobItemPropertiesConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlobItemProperties} instance.
     */
    public interface BlobItemPropertiesConstructorAccessor {
        /**
         * Creates a new instance of {@link BlobItemProperties} backed by an internal instance of
         * {@link BlobItemProperties}.
         *
         * @param internalProperties The internal properties.
         * @return A new instance of {@link BlobItemProperties}.
         */
        BlobItemProperties create(BlobItemPropertiesInternal internalProperties);

        /**
         * Gets the {@link BlobItemPropertiesInternal} backing the instance of {@link BlobItemProperties}.
         *
         * @param properties The properties.
         * @return The backing {@link BlobItemPropertiesInternal}.
         */
        BlobItemPropertiesInternal getInternalProperties(BlobItemProperties properties);
    }

    /**
     * The method called from {@link BlobItem} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobItemPropertiesConstructorAccessor accessor) {
        BlobItemPropertiesConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlobItemProperties} backed by an internal instance of
     * {@link BlobItemProperties}.
     *
     * @param internalProperties The internal properties.
     * @return A new instance of {@link BlobItemProperties}.
     */
    public static BlobItemProperties create(BlobItemPropertiesInternal internalProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobItemProperties();
        }

        assert accessor != null;
        return accessor.create(internalProperties);
    }

    /**
     * Gets the {@link BlobItemPropertiesInternal} backing the instance of {@link BlobItemProperties}.
     *
     * @param properties The properties.
     * @return The backing {@link BlobItemPropertiesInternal}.
     */
    public static BlobItemPropertiesInternal getInternalProperties(BlobItemProperties properties) {
        return accessor.getInternalProperties(properties);
    }
}
