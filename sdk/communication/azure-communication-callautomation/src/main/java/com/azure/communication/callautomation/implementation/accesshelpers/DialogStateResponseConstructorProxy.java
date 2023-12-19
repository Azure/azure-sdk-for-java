// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.DialogStateResponse;
import com.azure.communication.callautomation.models.DialogStateResult;

/**
 * Helper class to access private values of {@link DialogStateResult} across package boundaries.
 */
public final class DialogStateResponseConstructorProxy {
    private static DialogStateResponseConstructorProxy.DialogStateResponseConstructorAccessor accessor;

    private DialogStateResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link DialogStateResponseConstructorProxy.DialogStateResponseConstructorAccessor}
     * instance.
     */
    public interface DialogStateResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link DialogStateResult} backed by an internal instance of
         * {@link DialogStateResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link DialogStateResult}.
         */
        DialogStateResult create(DialogStateResponse internalResponse);
    }

    /**
     * The method called from {@link DialogStateResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final DialogStateResponseConstructorProxy.DialogStateResponseConstructorAccessor accessor) {
        DialogStateResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link DialogStateResult} backed by an internal instance of
     * {@link DialogStateResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link DialogStateResult}.
     */
    public static DialogStateResult create(DialogStateResponse internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new DialogStateResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
