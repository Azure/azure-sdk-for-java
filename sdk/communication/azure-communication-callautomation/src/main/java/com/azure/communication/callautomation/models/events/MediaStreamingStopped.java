// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingStopped model. */
@Fluent
public final class MediaStreamingStopped extends CallAutomationEventBase {
    /*
     * Defines the result for audio streaming update with the current status
     * and the details about the status
     */
    @JsonProperty(value = "mediaStreamingUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private MediaStreamingUpdate mediaStreamingUpdateResult;

     /**
     * Creates an instance of MediaStreamingStopped class.
     */
    public MediaStreamingStopped() {
        mediaStreamingUpdateResult = null;
    }

    /**
     * Get the getMediaStreamingUpdateResult property: Defines the result for audio streaming update with the current status and
     * the details about the status.
     *
     * @return the mediaStreamingUpdate value.
     */
    public MediaStreamingUpdate getMediaStreamingUpdateResult() {
        return this.mediaStreamingUpdateResult;
    }
}
