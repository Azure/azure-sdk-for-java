// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.RouterQueueInternal;
import com.azure.communication.jobrouter.models.RouterQueue;

/**
 * Helper class to access private values of {@link RouterQueue} across package boundaries.
 */
public final class RouterQueueConstructorProxy {

    private static RouterQueueConstructorAccessor accessor;

    private RouterQueueConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RouterQueueConstructorAccessor}
     * instance.
     */
    public interface RouterQueueConstructorAccessor {
        /**
         * Creates a new instance of {@link RouterQueue} backed by an internal instance of
         * {@link RouterQueue}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link RouterQueue}.
         */
        RouterQueue create(RouterQueueInternal internal);
    }

    /**
     * The method called from {@link RouterQueue} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouterQueueConstructorAccessor accessor) {
        RouterQueueConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RouterQueue} backed by an internal instance of
     * {@link RouterQueueInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link RouterQueue}.
     */
    public static RouterQueue create(RouterQueueInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RouterQueue();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
