// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.CancelAddParticipantResponse;
import com.azure.communication.callautomation.models.CancelAddParticipantOperationResult;

/**
 * Helper class to access private values of {@link CancelAddParticipantOperationResult} across package boundaries.
 */
public final class CancelAddParticipantResponseConstructorProxy {
    private static CancelAddParticipantResponseConstructorAccessor accessor;

    private CancelAddParticipantResponseConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link CancelAddParticipantResponseConstructorAccessor}
     * instance.
     */
    public interface CancelAddParticipantResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link CancelAddParticipantOperationResult} backed by an internal instance of
         * {@link CancelAddParticipantOperationResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link CancelAddParticipantOperationResult}.
         */
        CancelAddParticipantOperationResult create(CancelAddParticipantResponse internalResponse);
    }

    /**
     * The method called from {@link CancelAddParticipantOperationResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final CancelAddParticipantResponseConstructorAccessor accessor) {
        CancelAddParticipantResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link CancelAddParticipantOperationResult} backed by an internal instance of
     * {@link CancelAddParticipantOperationResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link CancelAddParticipantOperationResult}.
     */
    public static CancelAddParticipantOperationResult create(CancelAddParticipantResponse internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new CancelAddParticipantOperationResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
