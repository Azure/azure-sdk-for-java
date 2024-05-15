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
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    @JsonProperty(value = "resultInformation", access = JsonProperty.Access.WRITE_ONLY)
    private final ResultInformation resultInformation;

    /*
     * Defines the result for MediaStreamingUpdate with the current status and the details about the status
     */
    @JsonProperty(value = "mediaStreamingUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private final MediaStreamingUpdate mediaStreamingUpdateResult;

    /**
     * Creates an instance of MediaStreamingStarted class.
     */
    public MediaStreamingStarted() {
        resultInformation = null;
        mediaStreamingUpdateResult = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code/sub-code and message from NGC services.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
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
