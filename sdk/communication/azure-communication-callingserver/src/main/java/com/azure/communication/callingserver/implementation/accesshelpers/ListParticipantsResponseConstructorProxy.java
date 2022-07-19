// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.GetParticipantsResponseInternal;
import com.azure.communication.callingserver.models.ListParticipantsResponse;

public class ListParticipantsResponseConstructorProxy {
    private static ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor accessor;

    private ListParticipantsResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor}
     * instance.
     */
    public interface ListParticipantsResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link ListParticipantsResponse} backed by an internal instance of
         * {@link ListParticipantsResponse}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link ListParticipantsResponse}.
         */
        ListParticipantsResponse create(GetParticipantsResponseInternal internalResponse);
    }

    /**
     * The method called from {@link ListParticipantsResponse} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor accessor) {
        ListParticipantsResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ListParticipantsResponse} backed by an internal instance of
     * {@link ListParticipantsResponse}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link ListParticipantsResponse}.
     */
    public static ListParticipantsResponse create(GetParticipantsResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ListParticipantsResponse();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
