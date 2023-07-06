// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.SendDtmfResponseInternal;
import com.azure.communication.callautomation.models.SendDtmfResult;

/**
 * Helper class to access private values of {@link SendDtmfResult} across package boundaries.
 */
public final class SendDtmfResponseConstructorProxy {
    private static SendDtmfResponseConstructorAccessor accessor;

    private SendDtmfResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link SendDtmfResponseConstructorAccessor}
     * instance.
     */
    public interface SendDtmfResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link SendDtmfResult} backed by an internal instance of
         * {@link SendDtmfResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link SendDtmfResult}.
         */
        SendDtmfResult create(SendDtmfResponseInternal internalResponse);
    }

    /**
     * The method called from {@link SendDtmfResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final SendDtmfResponseConstructorAccessor accessor) {
        SendDtmfResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link SendDtmfResult} backed by an internal instance of
     * {@link SendDtmfResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link SendDtmfResult}.
     */
    public static SendDtmfResult create(SendDtmfResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new SendDtmfResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
