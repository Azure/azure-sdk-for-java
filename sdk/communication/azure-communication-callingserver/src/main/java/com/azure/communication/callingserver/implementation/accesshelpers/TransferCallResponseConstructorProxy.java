// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callingserver.models.TransferCallResponse;

/**
 * Helper class to access private values of {@link TransferCallResponse} across package boundaries.
 */
public final class TransferCallResponseConstructorProxy {
    private static TransferCallResponseConstructorAccessor accessor;

    private TransferCallResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link TransferCallResponseConstructorAccessor}
     * instance.
     */
    public interface TransferCallResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link TransferCallResponse} backed by an internal instance of
         * {@link TransferCallResponse}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link TransferCallResponse}.
         */
        TransferCallResponse create(TransferCallResponseInternal internalResponse);
    }

    /**
     * The method called from {@link TransferCallResponse} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final TransferCallResponseConstructorAccessor accessor) {
        TransferCallResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link TransferCallResponse} backed by an internal instance of
     * {@link TransferCallResponse}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link TransferCallResponse}.
     */
    public static TransferCallResponse create(TransferCallResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new TransferCallResponse();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
