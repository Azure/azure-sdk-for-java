// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.ExceptionPolicyInternal;
import com.azure.communication.jobrouter.models.ExceptionPolicy;

/**
 * Helper class to access private values of {@link ExceptionPolicy} across package boundaries.
 */
public final class ExceptionPolicyConstructorProxy {

    private static ExceptionPolicyConstructorAccessor accessor;

    private ExceptionPolicyConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link ExceptionPolicyConstructorAccessor}
     * instance.
     */
    public interface ExceptionPolicyConstructorAccessor {
        /**
         * Creates a new instance of {@link ExceptionPolicy} backed by an internal instance of
         * {@link ExceptionPolicy}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link ExceptionPolicy}.
         */
        ExceptionPolicy create(ExceptionPolicyInternal internal);
    }

    /**
     * The method called from {@link ExceptionPolicy} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ExceptionPolicyConstructorAccessor accessor) {
        ExceptionPolicyConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ExceptionPolicy} backed by an internal instance of
     * {@link ExceptionPolicyInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link ExceptionPolicy}.
     */
    public static ExceptionPolicy create(ExceptionPolicyInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ExceptionPolicy();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
