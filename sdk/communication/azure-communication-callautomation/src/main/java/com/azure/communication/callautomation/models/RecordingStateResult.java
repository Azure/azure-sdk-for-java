// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.RecordingStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.RecordingStateResponseInternal;
import com.azure.core.annotation.Immutable;

/** The response payload of start call recording operation. */
@Immutable
public final class RecordingStateResult {
    static {
        RecordingStateResponseConstructorProxy.setAccessor(RecordingStateResult::new);
    }
    /*
     * The recording id of the started recording
     */
    private final String recordingId;

    private final RecordingKind recordingKind;

    private final RecordingState recordingState;

    /**
     * Get the recordingId property: The recording id of the started recording.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Get the RecordingState property: The recording kind status of the recording.
     *
     * @return the recordingKind value.
     */
    public RecordingKind getRecordingKind() {
        return this.recordingKind;
    }

    /**
     * Get the RecordingState property: The recording status of the recording.
     *
     * @return the recordingState value.
     */
    public RecordingState getRecordingState() {
        return this.recordingState;
    }

    /**
     * Public constructor.
     *
     */
    public RecordingStateResult() {
        this.recordingId = null;
        this.recordingKind = null;
        this.recordingState = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  recordingStateResponseInternal The response from the service
     */
    RecordingStateResult(RecordingStateResponseInternal recordingStateResponseInternal) {
        this.recordingId = recordingStateResponseInternal.getRecordingId();
        this.recordingKind = RecordingKind.fromString(recordingStateResponseInternal.getRecordingKind().toString());
        this.recordingState = RecordingState.fromString(recordingStateResponseInternal.getRecordingState().toString());
    }
}
