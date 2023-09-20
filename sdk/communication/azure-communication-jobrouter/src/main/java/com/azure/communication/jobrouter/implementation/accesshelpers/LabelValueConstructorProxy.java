// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.accesshelpers;

import com.azure.communication.jobrouter.models.LabelValue;

/**
 * Helper class to access private values of {@link LabelValue} across package boundaries.
 */
public final class LabelValueConstructorProxy {

    private static LabelValueConstructorAccessor accessor;

    private LabelValueConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link LabelValueConstructorAccessor}
     * instance.
     */
    public interface LabelValueConstructorAccessor {
        /**
         * Creates a new instance of {@link LabelValue} backed by an internal instance of
         * {@link LabelValue}.
         *
         * @param internal The internal response.
         * @return A new instance of {@link LabelValue}.
         */
        LabelValue create(Object internal);
    }

    /**
     * The method called from {@link LabelValue} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final LabelValueConstructorAccessor accessor) {
        LabelValueConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link LabelValue} backed by an internal instance of
     * {@link Object}.
     *
     * @param internal The internal response.
     * @return A new instance of {@link LabelValue}.
     */
    public static LabelValue create(Object internal) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses LabelValue which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new LabelValue("");
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
