// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The response payload of get call recording state operation. */
@Fluent
public final class CallRecordingStateResult {
    /*
     * The recording state of the recording
     */
    @JsonProperty(value = "recordingState")
    private CallRecordingState recordingState;

    /**
     * Get the recordingState property: The recording state of the recording.
     *
     * @return the recordingState value.
     */
    public CallRecordingState getRecordingState() {
        return recordingState;
    }

    /**
     * Initializes a new instance of CallRecordingStateResult.
     *
     * @param recordingState the recordingState value.
     */
    public CallRecordingStateResult(CallRecordingState recordingState) {
        this.recordingState = recordingState;
    }
}
