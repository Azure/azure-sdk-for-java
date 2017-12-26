/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.RefreshIndexAdvancedInfoItem;
import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Refresh Index Response.
 */
public class RefreshIndexInner {
    /**
     * Content source Id.
     */
    @JsonProperty(value = "ContentSourceId")
    private String contentSourceId;

    /**
     * Update success status.
     */
    @JsonProperty(value = "IsUpdateSuccess")
    private Boolean isUpdateSuccess;

    /**
     * Advanced info list.
     */
    @JsonProperty(value = "AdvancedInfo")
    private List<RefreshIndexAdvancedInfoItem> advancedInfo;

    /**
     * Refresh index status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * Get the contentSourceId value.
     *
     * @return the contentSourceId value
     */
    public String contentSourceId() {
        return this.contentSourceId;
    }

    /**
     * Set the contentSourceId value.
     *
     * @param contentSourceId the contentSourceId value to set
     * @return the RefreshIndexInner object itself.
     */
    public RefreshIndexInner withContentSourceId(String contentSourceId) {
        this.contentSourceId = contentSourceId;
        return this;
    }

    /**
     * Get the isUpdateSuccess value.
     *
     * @return the isUpdateSuccess value
     */
    public Boolean isUpdateSuccess() {
        return this.isUpdateSuccess;
    }

    /**
     * Set the isUpdateSuccess value.
     *
     * @param isUpdateSuccess the isUpdateSuccess value to set
     * @return the RefreshIndexInner object itself.
     */
    public RefreshIndexInner withIsUpdateSuccess(Boolean isUpdateSuccess) {
        this.isUpdateSuccess = isUpdateSuccess;
        return this;
    }

    /**
     * Get the advancedInfo value.
     *
     * @return the advancedInfo value
     */
    public List<RefreshIndexAdvancedInfoItem> advancedInfo() {
        return this.advancedInfo;
    }

    /**
     * Set the advancedInfo value.
     *
     * @param advancedInfo the advancedInfo value to set
     * @return the RefreshIndexInner object itself.
     */
    public RefreshIndexInner withAdvancedInfo(List<RefreshIndexAdvancedInfoItem> advancedInfo) {
        this.advancedInfo = advancedInfo;
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
     * @return the RefreshIndexInner object itself.
     */
    public RefreshIndexInner withStatus(Status status) {
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
     * @return the RefreshIndexInner object itself.
     */
    public RefreshIndexInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
