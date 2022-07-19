// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.RecordingStatusResponseInternal;
import com.azure.communication.callingserver.models.RecordingStatusResponse;

/**
 * Helper class to access private values of {@link RecordingStatusResponse} across package boundaries.
 */
public final class RecordingStatusResponseConstructorProxy {
    private static RecordingStatusResponseConstructorAccessor accessor;

    private RecordingStatusResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RecordingStatusResponseConstructorAccessor}
     * instance.
     */
    public interface RecordingStatusResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link RecordingStatusResponse} backed by an internal instance of
         * {@link RecordingStatusResponseInternal}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link RecordingStatusResponse}.
         */
        RecordingStatusResponse create(RecordingStatusResponseInternal internalResponse);
    }

    /**
     * The method called from {@link RecordingStatusResponse} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RecordingStatusResponseConstructorAccessor accessor) {
        RecordingStatusResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RecordingStatusResponse} backed by an internal instance of
     * {@link RecordingStatusResponseInternal}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link RecordingStatusResponse}.
     */
    public static RecordingStatusResponse create(RecordingStatusResponseInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RecordingStatusResponseInternal();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
