// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The GetCallRecordingStateResult model. */
@Fluent
public final class GetCallRecordingStateResult {
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
        return this.recordingState;
    }

    /**
     * Set the recordingState property: The recording state of the recording.
     *
     * @param recordingState the recordingState value to set.
     * @return the GetCallRecordingStateResult object itself.
     */
    public GetCallRecordingStateResult setRecordingState(CallRecordingState recordingState) {
        this.recordingState = recordingState;
        return this;
    }
}
