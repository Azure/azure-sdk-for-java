// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.StartRecordingResponse;

/**
 * Helper class to access private values of {@link RecordingStatusResponse} across package boundaries.
 */
public final class StartRecordingResponseConstructorProxy {
    private static StartRecordingResponseConstructorAccessor accessor;

    private StartRecordingResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link StartRecordingResponseConstructorAccessor}
     * instance.
     */
    public interface StartRecordingResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link StartRecordingResponse} backed by an internal instance of
         * {@link RecordingStatusResponse}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link StartRecordingResponse}.
         */
        StartRecordingResponse create(RecordingStatusResponse internalResponse);
    }

    /**
     * The method called from {@link StartRecordingResponse} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final StartRecordingResponseConstructorAccessor accessor) {
        StartRecordingResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link StartRecordingResponse} backed by an internal instance of
     * {@link RecordingStatusResponse}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link StartRecordingResponse}.
     */
    public static StartRecordingResponse create(RecordingStatusResponse internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new RecordingStatusResponse();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
