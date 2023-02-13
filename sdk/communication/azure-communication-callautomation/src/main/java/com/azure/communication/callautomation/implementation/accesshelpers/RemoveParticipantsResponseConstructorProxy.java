// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.communication.callautomation.models.RemoveParticipantResult;

/**
 * Helper class to access private values of {@link RemoveParticipantResult} across package boundaries.
 */
public final class RemoveParticipantsResponseConstructorProxy {
    private static RemoveParticipantsResponseConstructorAccessor accessor;

    private RemoveParticipantsResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RemoveParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface RemoveParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link RemoveParticipantResult} backed by an internal instance of
         * {@link RemoveParticipantResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link RemoveParticipantResult}.
         */
        RemoveParticipantResult create(RemoveParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link RemoveParticipantResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RemoveParticipantsResponseConstructorAccessor accessor) {
        RemoveParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RemoveParticipantResult} backed by an internal instance of
     * {@link RemoveParticipantResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link RemoveParticipantResult}.
     */
    public static RemoveParticipantResult create(RemoveParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RemoveParticipantResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
