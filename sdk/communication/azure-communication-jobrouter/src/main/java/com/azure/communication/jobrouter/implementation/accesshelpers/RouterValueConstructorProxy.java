// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.models.RouterValue;

/**
 * Helper class to access private values of {@link RouterValue} across package boundaries.
 */
public final class RouterValueConstructorProxy {

    private static RouterValueConstructorAccessor accessor;

    private RouterValueConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RouterValueConstructorAccessor}
     * instance.
     */
    public interface RouterValueConstructorAccessor {
        /**
         * Creates a new instance of {@link RouterValue} backed by an internal instance of
         * {@link RouterValue}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link RouterValue}.
         */
        RouterValue create(Object internal);
    }

    /**
     * The method called from {@link RouterValue} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouterValueConstructorAccessor accessor) {
        RouterValueConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RouterValue} backed by an internal instance of
     * {@link Object}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link RouterValue}.
     */
    public static RouterValue create(Object internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses LabelValue which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RouterValue("");
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
