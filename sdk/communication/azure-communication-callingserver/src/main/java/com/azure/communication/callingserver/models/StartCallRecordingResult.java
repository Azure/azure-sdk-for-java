// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/** The response payload of start call recording operation. */
public final class StartCallRecordingResult {
    /*
     * The recording id of the started recording
     */
    private final String recordingId;

    /**
     * Get the recordingId property: The recording id of the started recording.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Initializes a new instance of StartCallRecordingResult.
     *
     * @param recordingId the recordingId value.
     */
    public StartCallRecordingResult(String recordingId) {
        this.recordingId = recordingId;
    }
}
