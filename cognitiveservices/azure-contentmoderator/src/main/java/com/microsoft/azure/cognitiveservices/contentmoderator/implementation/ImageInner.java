/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.ImageAdditionalInfoItem;
import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Image Properties.
 */
public class ImageInner {
    /**
     * Content Id.
     */
    @JsonProperty(value = "ContentId")
    private String contentId;

    /**
     * Advanced info list.
     */
    @JsonProperty(value = "AdditionalInfo")
    private List<ImageAdditionalInfoItem> additionalInfo;

    /**
     * Status details.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * Get the contentId value.
     *
     * @return the contentId value
     */
    public String contentId() {
        return this.contentId;
    }

    /**
     * Set the contentId value.
     *
     * @param contentId the contentId value to set
     * @return the ImageInner object itself.
     */
    public ImageInner withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    /**
     * Get the additionalInfo value.
     *
     * @return the additionalInfo value
     */
    public List<ImageAdditionalInfoItem> additionalInfo() {
        return this.additionalInfo;
    }

    /**
     * Set the additionalInfo value.
     *
     * @param additionalInfo the additionalInfo value to set
     * @return the ImageInner object itself.
     */
    public ImageInner withAdditionalInfo(List<ImageAdditionalInfoItem> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public Status status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the ImageInner object itself.
     */
    public ImageInner withStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Get the trackingId value.
     *
     * @return the trackingId value
     */
    public String trackingId() {
        return this.trackingId;
    }

    /**
     * Set the trackingId value.
     *
     * @param trackingId the trackingId value to set
     * @return the ImageInner object itself.
     */
    public ImageInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
