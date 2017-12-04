/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.AddImageAdditionalInfoItem;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.AddGetRefreshStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Add Image Response.
 */
public class AddImageInner {
    /**
     * Content Id.
     */
    @JsonProperty(value = "contentId")
    private String contentId;

    /**
     * Advanced info list.
     */
    @JsonProperty(value = "additionalInfo")
    private List<AddImageAdditionalInfoItem> additionalInfo;

    /**
     * Add image response status.
     */
    @JsonProperty(value = "status")
    private AddGetRefreshStatus status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "trackingId")
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
     * @return the AddImageInner object itself.
     */
    public AddImageInner withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    /**
     * Get the additionalInfo value.
     *
     * @return the additionalInfo value
     */
    public List<AddImageAdditionalInfoItem> additionalInfo() {
        return this.additionalInfo;
    }

    /**
     * Set the additionalInfo value.
     *
     * @param additionalInfo the additionalInfo value to set
     * @return the AddImageInner object itself.
     */
    public AddImageInner withAdditionalInfo(List<AddImageAdditionalInfoItem> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public AddGetRefreshStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the AddImageInner object itself.
     */
    public AddImageInner withStatus(AddGetRefreshStatus status) {
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
     * @return the AddImageInner object itself.
     */
    public AddImageInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
