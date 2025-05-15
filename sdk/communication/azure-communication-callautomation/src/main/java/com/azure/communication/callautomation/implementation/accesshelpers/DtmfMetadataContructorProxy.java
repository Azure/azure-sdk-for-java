// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.DtmfMetadataConverter;
import com.azure.communication.callautomation.models.DtmfMetadata;

/**
 * Helper class to access private values of {@link DtmdfMetadata} across package boundaries.
 */
public final class DtmfMetadataContructorProxy {
    private static DtmfMetadataContructorProxyAccessor accessor;

    private DtmfMetadataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link DtmfMetadataContructorProxyAccessor}
    * instance.
    */
    public interface DtmfMetadataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link DtmfMetadata} backed by an internal instance of
         * {@link DtmfMetadataConvertor}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link DtmfMetadata}.
         */
        DtmfMetadata create(DtmfMetadataConverter internalResponse);

        /**
         * Creates a new instance of {@link DtmfMetadata}
         *
         * @param data The internal response.
         * @return A new instance of {@link DtmfMetadata}.
         */
        DtmfMetadata create(String data);
    }

    /**
    * The method called from {@link DtmfMetadata} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final DtmfMetadataContructorProxyAccessor accessor) {
        DtmfMetadataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link DtmfMetadata} backed by an internal instance of
    * {@link DtmfMetadataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link DtmfMetadata}.
    */
    public static DtmfMetadata create(DtmfMetadataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses DtmfMetadata which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new DtmfMetadata();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }

    /**
     * Creates a new instance of {@link DtmfMetadata} 
     *
     * @param data The dtmf data.
     * @return A new instance of {@link DtmfMetadata}.
     */
    public static DtmfMetadata create(String data) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses DtmfMetadata which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new DtmfMetadata();
        }

        assert accessor != null;
        return accessor.create(data);
    }
}
