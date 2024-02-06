// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.RouterJobInternal;
import com.azure.communication.jobrouter.models.RouterJob;

/**
 * Helper class to access private values of {@link RouterJob} across package boundaries.
 */
public final class RouterJobConstructorProxy {

    private static RouterJobConstructorAccessor accessor;

    private RouterJobConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RouterJobConstructorAccessor}
     * instance.
     */
    public interface RouterJobConstructorAccessor {
        /**
         * Creates a new instance of {@link RouterJob} backed by an internal instance of
         * {@link RouterJob}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link RouterJob}.
         */
        RouterJob create(RouterJobInternal internal);
    }

    /**
     * The method called from {@link RouterJob} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouterJobConstructorAccessor accessor) {
        RouterJobConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RouterJob} backed by an internal instance of
     * {@link RouterJobInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link RouterJob}.
     */
    public static RouterJob create(RouterJobInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RouterJob();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
