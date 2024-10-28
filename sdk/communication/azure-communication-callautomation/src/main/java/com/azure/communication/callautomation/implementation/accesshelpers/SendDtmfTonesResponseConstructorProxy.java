// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.SendDtmfTonesResultInternal;
import com.azure.communication.callautomation.models.SendDtmfTonesResult;

/**
 * Helper class to access private values of {@link SendDtmfTonesResult} across package boundaries.
 */
public final class SendDtmfTonesResponseConstructorProxy {
    private static SendDtmfTonesResponseConstructorAccessor accessor;

    private SendDtmfTonesResponseConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link SendDtmfTonesResponseConstructorAccessor}
     * instance.
     */
    public interface SendDtmfTonesResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link SendDtmfTonesResult} backed by an internal instance of
         * {@link SendDtmfTonesResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link SendDtmfTonesResult}.
         */
        SendDtmfTonesResult create(SendDtmfTonesResultInternal internalResponse);
    }

    /**
     * The method called from {@link SendDtmfTonesResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final SendDtmfTonesResponseConstructorAccessor accessor) {
        SendDtmfTonesResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link SendDtmfTonesResult} backed by an internal instance of
     * {@link SendDtmfTonesResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link SendDtmfTonesResult}.
     */
    public static SendDtmfTonesResult create(SendDtmfTonesResultInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new SendDtmfTonesResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
