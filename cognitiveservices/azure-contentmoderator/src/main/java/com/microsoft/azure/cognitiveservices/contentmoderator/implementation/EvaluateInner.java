/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.KeyValuePair;
import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evaluate response object.
 */
public class EvaluateInner {
    /**
     * The cache id.
     */
    @JsonProperty(value = "CacheID")
    private String cacheID;

    /**
     * Evaluate result.
     */
    @JsonProperty(value = "Result")
    private Boolean result;

    /**
     * The tracking id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * The adult classification score.
     */
    @JsonProperty(value = "AdultClassificationScore")
    private Double adultClassificationScore;

    /**
     * Indicates if an image is classified as adult.
     */
    @JsonProperty(value = "IsImageAdultClassified")
    private Boolean isImageAdultClassified;

    /**
     * The racy classication score.
     */
    @JsonProperty(value = "RacyClassificationScore")
    private Double racyClassificationScore;

    /**
     * Indicates if the image is classified as racy.
     */
    @JsonProperty(value = "IsImageRacyClassified")
    private Boolean isImageRacyClassified;

    /**
     * The advanced info.
     */
    @JsonProperty(value = "AdvancedInfo")
    private List<KeyValuePair> advancedInfo;

    /**
     * The evaluate status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Get the cacheID value.
     *
     * @return the cacheID value
     */
    public String cacheID() {
        return this.cacheID;
    }

    /**
     * Set the cacheID value.
     *
     * @param cacheID the cacheID value to set
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withCacheID(String cacheID) {
        this.cacheID = cacheID;
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
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withResult(Boolean result) {
        this.result = result;
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
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    /**
     * Get the adultClassificationScore value.
     *
     * @return the adultClassificationScore value
     */
    public Double adultClassificationScore() {
        return this.adultClassificationScore;
    }

    /**
     * Set the adultClassificationScore value.
     *
     * @param adultClassificationScore the adultClassificationScore value to set
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withAdultClassificationScore(Double adultClassificationScore) {
        this.adultClassificationScore = adultClassificationScore;
        return this;
    }

    /**
     * Get the isImageAdultClassified value.
     *
     * @return the isImageAdultClassified value
     */
    public Boolean isImageAdultClassified() {
        return this.isImageAdultClassified;
    }

    /**
     * Set the isImageAdultClassified value.
     *
     * @param isImageAdultClassified the isImageAdultClassified value to set
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withIsImageAdultClassified(Boolean isImageAdultClassified) {
        this.isImageAdultClassified = isImageAdultClassified;
        return this;
    }

    /**
     * Get the racyClassificationScore value.
     *
     * @return the racyClassificationScore value
     */
    public Double racyClassificationScore() {
        return this.racyClassificationScore;
    }

    /**
     * Set the racyClassificationScore value.
     *
     * @param racyClassificationScore the racyClassificationScore value to set
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withRacyClassificationScore(Double racyClassificationScore) {
        this.racyClassificationScore = racyClassificationScore;
        return this;
    }

    /**
     * Get the isImageRacyClassified value.
     *
     * @return the isImageRacyClassified value
     */
    public Boolean isImageRacyClassified() {
        return this.isImageRacyClassified;
    }

    /**
     * Set the isImageRacyClassified value.
     *
     * @param isImageRacyClassified the isImageRacyClassified value to set
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withIsImageRacyClassified(Boolean isImageRacyClassified) {
        this.isImageRacyClassified = isImageRacyClassified;
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
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withAdvancedInfo(List<KeyValuePair> advancedInfo) {
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
     * @return the EvaluateInner object itself.
     */
    public EvaluateInner withStatus(Status status) {
        this.status = status;
        return this;
    }

}
