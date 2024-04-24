// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.DistributionPolicyInternal;
import com.azure.communication.jobrouter.models.DistributionPolicy;

/**
 * Helper class to access private values of {@link DistributionPolicy} across package boundaries.
 */
public final class DistributionPolicyConstructorProxy {

    private static DistributionPolicyConstructorAccessor accessor;

    private DistributionPolicyConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link DistributionPolicyConstructorAccessor}
     * instance.
     */
    public interface DistributionPolicyConstructorAccessor {
        /**
         * Creates a new instance of {@link DistributionPolicy} backed by an internal instance of
         * {@link DistributionPolicy}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link DistributionPolicy}.
         */
        DistributionPolicy create(DistributionPolicyInternal internal);
    }

    /**
     * The method called from {@link DistributionPolicy} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final DistributionPolicyConstructorAccessor accessor) {
        DistributionPolicyConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link DistributionPolicy} backed by an internal instance of
     * {@link DistributionPolicyInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link DistributionPolicy}.
     */
    public static DistributionPolicy create(DistributionPolicyInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new DistributionPolicy();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
