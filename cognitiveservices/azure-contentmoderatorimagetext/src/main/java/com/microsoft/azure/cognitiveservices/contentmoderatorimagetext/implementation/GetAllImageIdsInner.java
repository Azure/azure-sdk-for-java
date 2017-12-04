/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.AddGetRefreshStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gets all image Id response properties.
 */
public class GetAllImageIdsInner {
    /**
     * Source of thecontent.
     */
    @JsonProperty(value = "contentSource")
    private String contentSource;

    /**
     * Id of the contents.
     */
    @JsonProperty(value = "contentIds")
    private List<Double> contentIds;

    /**
     * Get Image status.
     */
    @JsonProperty(value = "status")
    private AddGetRefreshStatus status;

    /**
     * Tracking Id.
     */
    @JsonProperty(value = "trackingId")
    private String trackingId;

    /**
     * Get the contentSource value.
     *
     * @return the contentSource value
     */
    public String contentSource() {
        return this.contentSource;
    }

    /**
     * Set the contentSource value.
     *
     * @param contentSource the contentSource value to set
     * @return the GetAllImageIdsInner object itself.
     */
    public GetAllImageIdsInner withContentSource(String contentSource) {
        this.contentSource = contentSource;
        return this;
    }

    /**
     * Get the contentIds value.
     *
     * @return the contentIds value
     */
    public List<Double> contentIds() {
        return this.contentIds;
    }

    /**
     * Set the contentIds value.
     *
     * @param contentIds the contentIds value to set
     * @return the GetAllImageIdsInner object itself.
     */
    public GetAllImageIdsInner withContentIds(List<Double> contentIds) {
        this.contentIds = contentIds;
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
     * @return the GetAllImageIdsInner object itself.
     */
    public GetAllImageIdsInner withStatus(AddGetRefreshStatus status) {
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
     * @return the GetAllImageIdsInner object itself.
     */
    public GetAllImageIdsInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
