// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingFailed model. */
@Fluent
public final class MediaStreamingFailed extends CallAutomationEventBase {

    /*
     * Defines the result for audio streaming update with the current status
     * and the details about the status
     */
    @JsonProperty(value = "mediaStreamingUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private MediaStreamingUpdate mediaStreamingUpdateResult;

      /**
     * Creates an instance of MediaStreamingFailed class.
     */
    public MediaStreamingFailed() {
        mediaStreamingUpdateResult = null;
    }

    /**
     * Get the mediaStreamingUpdateResult property: Defines the result for audio streaming update with the current status and
     * the details about the status.
     *
     * @return the mediaStreamingUpdate value.
     */
    public MediaStreamingUpdate getMediaStreamingUpdateResult() {
        return this.mediaStreamingUpdateResult;
    }
}
