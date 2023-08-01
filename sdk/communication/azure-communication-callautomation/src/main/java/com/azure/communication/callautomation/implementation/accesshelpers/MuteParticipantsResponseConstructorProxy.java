// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.MuteParticipantsResponseInternal;
import com.azure.communication.callautomation.models.MuteParticipantsResult;

/**
 * Helper class to access private values of {@link MuteParticipantsResult} across package boundaries.
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
         * Creates a new instance of {@link MuteParticipantsResult} backed by an internal instance of
         * {@link MuteParticipantsResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link MuteParticipantsResult}.
         */
        MuteParticipantsResult create(MuteParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link MuteParticipantsResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final MuteParticipantsResponseConstructorAccessor accessor) {
        MuteParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link MuteParticipantsResult} backed by an internal instance of
     * {@link MuteParticipantsResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link MuteParticipantsResult}.
     */
    public static MuteParticipantsResult create(MuteParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new MuteParticipantsResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
