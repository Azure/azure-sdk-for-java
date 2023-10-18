// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.MuteParticipantsResultInternal;
import com.azure.communication.callautomation.models.MuteParticipantResult;

/**
 * Helper class to access private values of {@link MuteParticipantResult} across package boundaries.
 */
public final class MuteParticipantsResponseConstructorProxy {
    private static MuteParticipantsResponseConstructorAccessor accessor;

    private MuteParticipantsResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link MuteParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface MuteParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link MuteParticipantResult} backed by an internal instance of
         * {@link MuteParticipantResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link MuteParticipantResult}.
         */
        MuteParticipantResult create(MuteParticipantsResultInternal internalResponse);
    }

    /**
     * The method called from {@link MuteParticipantResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final MuteParticipantsResponseConstructorAccessor accessor) {
        MuteParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link MuteParticipantResult} backed by an internal instance of
     * {@link MuteParticipantResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link MuteParticipantResult}.
     */
    public static MuteParticipantResult create(MuteParticipantsResultInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new MuteParticipantResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
