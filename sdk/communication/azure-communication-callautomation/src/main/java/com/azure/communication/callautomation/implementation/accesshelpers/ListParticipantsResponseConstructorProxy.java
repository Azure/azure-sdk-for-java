// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.GetParticipantsResponseInternal;
import com.azure.communication.callautomation.models.ListParticipantsResult;

public final class ListParticipantsResponseConstructorProxy {
    private static ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor accessor;

    private ListParticipantsResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface ListParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link ListParticipantsResult} backed by an internal instance of
         * {@link ListParticipantsResult}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link ListParticipantsResult}.
         */
        ListParticipantsResult create(GetParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link ListParticipantsResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor accessor) {
        ListParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ListParticipantsResult} backed by an internal instance of
     * {@link ListParticipantsResult}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link ListParticipantsResult}.
     */
    public static ListParticipantsResult create(GetParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ListParticipantsResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
