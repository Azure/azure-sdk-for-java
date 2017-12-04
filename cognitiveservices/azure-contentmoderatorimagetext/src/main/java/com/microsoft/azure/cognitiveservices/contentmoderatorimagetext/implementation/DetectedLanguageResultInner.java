/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detect language result.
 */
public class DetectedLanguageResultInner {
    /**
     * The detected language.
     */
    @JsonProperty(value = "detectedLanguage")
    private String detectedLanguage;

    /**
     * The detect language status.
     */
    @JsonProperty(value = "status")
    private ResponseStatus status;

    /**
     * The tracking id.
     */
    @JsonProperty(value = "trackingId")
    private String trackingId;

    /**
     * Get the detectedLanguage value.
     *
     * @return the detectedLanguage value
     */
    public String detectedLanguage() {
        return this.detectedLanguage;
    }

    /**
     * Set the detectedLanguage value.
     *
     * @param detectedLanguage the detectedLanguage value to set
     * @return the DetectedLanguageResultInner object itself.
     */
    public DetectedLanguageResultInner withDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public ResponseStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the DetectedLanguageResultInner object itself.
     */
    public DetectedLanguageResultInner withStatus(ResponseStatus status) {
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
     * @return the DetectedLanguageResultInner object itself.
     */
    public DetectedLanguageResultInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
