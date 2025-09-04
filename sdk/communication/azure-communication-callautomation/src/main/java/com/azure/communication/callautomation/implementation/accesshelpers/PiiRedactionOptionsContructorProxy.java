// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.PiiRedactionOptionsConverter;
import com.azure.communication.callautomation.models.PiiRedactionOptions;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link PiiRedactionOptions} across package boundaries.
 */
public final class PiiRedactionOptionsContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(PiiRedactionOptionsContructorProxy.class);
    private static PiiRedactionOptionsContructorProxyAccessor accessor;

    private PiiRedactionOptionsContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link PiiRedactionOptionsContructorProxyAccessor}
    * instance.
    */
    public interface PiiRedactionOptionsContructorProxyAccessor {
        /**
         * Creates a new instance of {@link PiiRedactionOptions} backed by an internal instance of
         * {@link PiiRedactionOptionsConverter}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link PiiRedactionOptions}.
         */
        PiiRedactionOptions create(PiiRedactionOptionsConverter internalResponse);

        /**
         * Creates a new instance of {@link PiiRedactionOptions}
         *
         * @param data The internal response.
         * @return A new instance of {@link PiiRedactionOptions}.
         */
        PiiRedactionOptions create(String data);
    }

    /**
    * The method called from {@link PiiRedactionOptions} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final PiiRedactionOptionsContructorProxyAccessor accessor) {
        PiiRedactionOptionsContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link PiiRedactionOptions} backed by an internal instance of
    * {@link PiiRedactionOptionsConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link PiiRedactionOptions}.
    */
    public static PiiRedactionOptions create(PiiRedactionOptionsConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses PiiRedactionOptions which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(PiiRedactionOptions.class.getName(), true,
                    PiiRedactionOptionsContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }

    /**
     * Creates a new instance of {@link PiiRedactionOptions}
     *
     * @param data The internal response.
     * @return A new instance of {@link PiiRedactionOptions}.
     */
    public static PiiRedactionOptions create(String data) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses PiiRedactionOptions which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(PiiRedactionOptions.class.getName(), true,
                    PiiRedactionOptionsContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(data);
    }
}
