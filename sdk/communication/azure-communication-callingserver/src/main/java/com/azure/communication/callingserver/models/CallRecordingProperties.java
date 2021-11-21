// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The result payload of get call recording state operation. */
@Immutable
public final class CallRecordingProperties {
    /*
     * The state of the recording
     */
    @JsonProperty(value = "recordingState")
    private CallRecordingState recordingState;

    /**
     * Get the recordingState property: The state of the recording.
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
    public CallRecordingProperties(CallRecordingState recordingState) {
        this.recordingState = recordingState;
    }
}
