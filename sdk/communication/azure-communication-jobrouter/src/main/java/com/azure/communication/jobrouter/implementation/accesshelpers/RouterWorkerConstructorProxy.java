// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.RouterWorkerInternal;
import com.azure.communication.jobrouter.models.RouterWorker;

/**
 * Helper class to access private values of {@link RouterWorker} across package boundaries.
 */
public final class RouterWorkerConstructorProxy {

    private static RouterWorkerConstructorAccessor accessor;

    private RouterWorkerConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RouterWorkerConstructorAccessor}
     * instance.
     */
    public interface RouterWorkerConstructorAccessor {
        /**
         * Creates a new instance of {@link RouterWorker} backed by an internal instance of
         * {@link RouterWorker}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link RouterWorker}.
         */
        RouterWorker create(RouterWorkerInternal internal);
    }

    /**
     * The method called from {@link RouterWorker} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouterWorkerConstructorAccessor accessor) {
        RouterWorkerConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RouterWorker} backed by an internal instance of
     * {@link RouterWorkerInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link RouterWorker}.
     */
    public static RouterWorker create(RouterWorkerInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RouterWorker();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
