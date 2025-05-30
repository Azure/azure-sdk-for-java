// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.DtmfDataConverter;
import com.azure.communication.callautomation.models.DtmfData;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link DtmdfData} across package boundaries.
 */
public final class DtmfDataContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(DtmfDataContructorProxy.class);
    private static DtmfDataContructorProxyAccessor accessor;

    private DtmfDataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link DtmfDataContructorProxyAccessor}
    * instance.
    */
    public interface DtmfDataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link DtmfData} backed by an internal instance of
         * {@link DtmfDataConvertor}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link DtmfData}.
         */
        DtmfData create(DtmfDataConverter internalResponse);

        /**
         * Creates a new instance of {@link DtmfData}
         *
         * @param data The internal response.
         * @return A new instance of {@link DtmfData}.
         */
        DtmfData create(String data);
    }

    /**
    * The method called from {@link DtmfData} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final DtmfDataContructorProxyAccessor accessor) {
        DtmfDataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link DtmfData} backed by an internal instance of
    * {@link DtmfDataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link DtmfData}.
    */
    public static DtmfData create(DtmfDataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses DtmfData which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(DtmfData.class.getName(), true, DtmfDataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }

    /**
     * Creates a new instance of {@link DtmfData} 
     *
     * @param data The dtmf data.
     * @return A new instance of {@link DtmfData}.
     */
    public static DtmfData create(String data) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses DtmfData which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(DtmfData.class.getName(), true, DtmfDataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(data);
    }
}
