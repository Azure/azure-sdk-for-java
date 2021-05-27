// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The request payload start call recording operation. */
@Fluent
public final class StartCallRecordingRequest {
    /*
     * The uri to send notifications to.
     */
    @JsonProperty(value = "recordingStateCallbackUri")
    private String recordingStateCallbackUri;

    /**
     * Get the recordingStateCallbackUri property: The uri to send notifications to.
     *
     * @return the recordingStateCallbackUri value.
     */
    public String getRecordingStateCallbackUri() {
        return this.recordingStateCallbackUri;
    }

    /**
     * Set the recordingStateCallbackUri property: The uri to send notifications to.
     *
     * @param recordingStateCallbackUri the recordingStateCallbackUri value to set.
     * @return the StartCallRecordingRequestInternal object itself.
     */
    public StartCallRecordingRequest setRecordingStateCallbackUri(String recordingStateCallbackUri) {
        this.recordingStateCallbackUri = recordingStateCallbackUri;
        return this;
    }
}
