// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The MediaStreamingStarted model.
 */
@Fluent
public final class MediaStreamingStarted extends CallAutomationEventBase {
    /*
     * Defines the result for MediaStreamingUpdate with the current status and the details about the status
     */
    @JsonProperty(value = "mediaStreamingUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private final MediaStreamingUpdate mediaStreamingUpdateResult;

    /**
     * Creates an instance of MediaStreamingStarted class.
     */
    public MediaStreamingStarted() {
        mediaStreamingUpdateResult = null;
    }

     /**
     * Get the mediaStreamingUpdateResult property: Defines the result for audio streaming update with the current status
     * and the details about the status.
     *
     * @return the mediaStreamingUpdateResult value.
     */
    public MediaStreamingUpdate getMediaStreamingUpdateResult() {
        return this.mediaStreamingUpdateResult;
    }

}
