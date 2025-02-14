// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.RecordingResultResponse;
import com.azure.communication.callautomation.models.RecordingResult;

/**
 * Helper class to access private values of {@link RecordingResult} across package boundaries.
 */
public final class RecordingResultResponseConstructorProxy {
    private static RecordingResultResponseConstructorAccessor accessor;

    private RecordingResultResponseConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link RecordingResultResponseConstructorAccessor}
     * instance.
     */
    public interface RecordingResultResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link RecordingResult} backed by an internal instance of
         * {@link RecordingResultResponse}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link RecordingResult}.
         */
        RecordingResult create(RecordingResultResponse internalResponse);
    }

    /**
     * The method called from {@link RecordingResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RecordingResultResponseConstructorAccessor accessor) {
        RecordingResultResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RecordingResult} backed by an internal instance of
     * {@link RecordingResultResponse}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link RecordingResult}.
     */
    public static RecordingResult create(RecordingResultResponse internalResponse) {
        if (accessor == null) {
            new RecordingResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
