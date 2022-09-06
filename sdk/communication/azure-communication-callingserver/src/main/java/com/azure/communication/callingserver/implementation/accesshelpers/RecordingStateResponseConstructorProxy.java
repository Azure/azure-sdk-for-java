// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.RecordingStateResponseInternal;
import com.azure.communication.callingserver.models.RecordingStateResult;

/**
 * Helper class to access private values of {@link RecordingStateResult} across package boundaries.
 */
public final class RecordingStateResponseConstructorProxy {
    private static RecordingStateResponseConstructorAccessor accessor;

    private RecordingStateResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link RecordingStateResponseConstructorAccessor}
     * instance.
     */
    public interface RecordingStateResponseConstructorAccessor {
        /**
         * Creates a new instance of {@link RecordingStateResult} backed by an internal instance of
         * {@link RecordingStateResponseInternal}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link RecordingStateResult}.
         */
        RecordingStateResult create(RecordingStateResponseInternal internalResponse);
    }

    /**
     * The method called from {@link RecordingStateResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RecordingStateResponseConstructorAccessor accessor) {
        RecordingStateResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link RecordingStateResult} backed by an internal instance of
     * {@link RecordingStateResponseInternal}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link RecordingStateResult}.
     */
    public static RecordingStateResult create(RecordingStateResponseInternal internalResponse) {
        if (accessor == null) {
            new RecordingStateResult();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
