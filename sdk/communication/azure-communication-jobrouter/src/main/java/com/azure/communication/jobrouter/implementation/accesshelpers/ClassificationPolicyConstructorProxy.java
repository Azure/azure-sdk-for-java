// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyInternal;
import com.azure.communication.jobrouter.models.ClassificationPolicy;

/**
 * Helper class to access private values of {@link ClassificationPolicy} across package boundaries.
 */
public final class ClassificationPolicyConstructorProxy {

    private static ClassificationPolicyConstructorAccessor accessor;

    private ClassificationPolicyConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link ClassificationPolicyConstructorAccessor}
     * instance.
     */
    public interface ClassificationPolicyConstructorAccessor {
        /**
         * Creates a new instance of {@link ClassificationPolicy} backed by an internal instance of
         * {@link ClassificationPolicy}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link ClassificationPolicy}.
         */
        ClassificationPolicy create(ClassificationPolicyInternal internal);
    }

    /**
     * The method called from {@link ClassificationPolicy} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ClassificationPolicyConstructorAccessor accessor) {
        ClassificationPolicyConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ClassificationPolicy} backed by an internal instance of
     * {@link ClassificationPolicyInternal}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link ClassificationPolicy}.
     */
    public static ClassificationPolicy create(ClassificationPolicyInternal internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses RouterJob which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ClassificationPolicy();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
