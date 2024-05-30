// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingUpdate model. */
@Fluent
public final class MediaStreamingUpdate {
    /*
     * The contentType property.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /*
     * The mediaStreamingStatus property.
     */
    @JsonProperty(value = "mediaStreamingStatus")
    private MediaStreamingStatus mediaStreamingStatus;

    /*
     * The mediaStreamingStatusDetails property.
     */
    @JsonProperty(value = "mediaStreamingStatusDetails")
    private MediaStreamingStatusDetails mediaStreamingStatusDetails;

    /**
     * Get the contentType property: The contentType property.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Get the mediaStreamingStatus property: The mediaStreamingStatus property.
     *
     * @return the mediaStreamingStatus value.
     */
    public MediaStreamingStatus getMediaStreamingStatus() {
        return this.mediaStreamingStatus;
    }

    /**
     * Get the mediaStreamingStatusDetails property: The mediaStreamingStatusDetails property.
     *
     * @return the mediaStreamingStatusDetails value.
     */
    public MediaStreamingStatusDetails getMediaStreamingStatusDetails() {
        return this.mediaStreamingStatusDetails;
    }
}
