/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.KeyValuePair;
import com.microsoft.azure.cognitiveservices.contentmoderator.Face;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object the contains found faces.
 */
public class FoundFacesInner {
    /**
     * The evaluate status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * The tracking id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * The cache id.
     */
    @JsonProperty(value = "CacheId")
    private String cacheId;

    /**
     * True if result was found.
     */
    @JsonProperty(value = "Result")
    private Boolean result;

    /**
     * Number of faces found.
     */
    @JsonProperty(value = "Count")
    private Integer count;

    /**
     * The advanced info.
     */
    @JsonProperty(value = "AdvancedInfo")
    private List<KeyValuePair> advancedInfo;

    /**
     * The list of faces.
     */
    @JsonProperty(value = "Faces")
    private List<Face> faces;

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
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withStatus(Status status) {
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
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    /**
     * Get the cacheId value.
     *
     * @return the cacheId value
     */
    public String cacheId() {
        return this.cacheId;
    }

    /**
     * Set the cacheId value.
     *
     * @param cacheId the cacheId value to set
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withCacheId(String cacheId) {
        this.cacheId = cacheId;
        return this;
    }

    /**
     * Get the result value.
     *
     * @return the result value
     */
    public Boolean result() {
        return this.result;
    }

    /**
     * Set the result value.
     *
     * @param result the result value to set
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withResult(Boolean result) {
        this.result = result;
        return this;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Get the advancedInfo value.
     *
     * @return the advancedInfo value
     */
    public List<KeyValuePair> advancedInfo() {
        return this.advancedInfo;
    }

    /**
     * Set the advancedInfo value.
     *
     * @param advancedInfo the advancedInfo value to set
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withAdvancedInfo(List<KeyValuePair> advancedInfo) {
        this.advancedInfo = advancedInfo;
        return this;
    }

    /**
     * Get the faces value.
     *
     * @return the faces value
     */
    public List<Face> faces() {
        return this.faces;
    }

    /**
     * Set the faces value.
     *
     * @param faces the faces value to set
     * @return the FoundFacesInner object itself.
     */
    public FoundFacesInner withFaces(List<Face> faces) {
        this.faces = faces;
        return this;
    }

}
