// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.communication.callingserver.models.RemoveParticipantsResult;

/**
 * Helper class to access private values of {@link RemoveParticipantsResult} across package boundaries.
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
         * Creates a new instance of {@link RemoveParticipantsResult} backed by an internal instance of
         * {@link RemoveParticipantsResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link RemoveParticipantsResult}.
         */
        RemoveParticipantsResult create(RemoveParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link RemoveParticipantsResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RemoveParticipantsResponseConstructorAccessor accessor) {
        RemoveParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RemoveParticipantsResult} backed by an internal instance of
     * {@link RemoveParticipantsResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link RemoveParticipantsResult}.
     */
    public static RemoveParticipantsResult create(RemoveParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RemoveParticipantsResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
