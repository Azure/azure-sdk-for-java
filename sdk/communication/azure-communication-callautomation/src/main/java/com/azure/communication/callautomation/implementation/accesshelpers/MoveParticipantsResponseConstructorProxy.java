// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.MoveParticipantsResponse;
import com.azure.communication.callautomation.models.MoveParticipantsResult;

/**
 * Helper class to access private values of {@link MoveParticipantsResult} across package boundaries.
 */
public final class MoveParticipantsResponseConstructorProxy {
    private static MoveParticipantsResponseConstructorAccessor accessor;

    private MoveParticipantsResponseConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link MoveParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface MoveParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link MoveParticipantsResult} backed by an internal instance of
         * {@link MoveParticipantsResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link MoveParticipantsResult}.
         */
        MoveParticipantsResult create(MoveParticipantsResponse internalResponse);
    }

    /**
     * The method called from {@link MoveParticipantsResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final MoveParticipantsResponseConstructorAccessor accessor) {
        MoveParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link MoveParticipantsResult} backed by an internal instance of
     * {@link MoveParticipantsResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link MoveParticipantsResult}.
     */
    public static MoveParticipantsResult create(MoveParticipantsResponse internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses MoveParticipantsResult which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new MoveParticipantsResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
