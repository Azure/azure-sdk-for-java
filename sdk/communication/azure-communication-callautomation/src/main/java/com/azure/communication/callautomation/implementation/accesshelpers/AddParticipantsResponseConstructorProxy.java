// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.AddParticipantsResponseInternal;
import com.azure.communication.callautomation.models.AddParticipantResult;

/**
 * Helper class to access private values of {@link AddParticipantResult} across package boundaries.
 */
public final class AddParticipantsResponseConstructorProxy {
    private static AddParticipantsResponseConstructorAccessor accessor;

    private AddParticipantsResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link AddParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface AddParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link AddParticipantResult} backed by an internal instance of
         * {@link AddParticipantResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link AddParticipantResult}.
         */
        AddParticipantResult create(AddParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link AddParticipantResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final AddParticipantsResponseConstructorAccessor accessor) {
        AddParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link AddParticipantResult} backed by an internal instance of
     * {@link AddParticipantResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link AddParticipantResult}.
     */
    public static AddParticipantResult create(AddParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new AddParticipantResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
