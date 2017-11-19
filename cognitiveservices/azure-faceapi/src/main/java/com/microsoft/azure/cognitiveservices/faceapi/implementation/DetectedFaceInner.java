/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.microsoft.azure.cognitiveservices.faceapi.FaceRectangle;
import com.microsoft.azure.cognitiveservices.faceapi.FaceLandmarks;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detected Face object.
 */
public class DetectedFaceInner {
    /**
     * The faceId property.
     */
    @JsonProperty(value = "faceId")
    private String faceId;

    /**
     * The faceRectangle property.
     */
    @JsonProperty(value = "faceRectangle", required = true)
    private FaceRectangle faceRectangle;

    /**
     * The faceLandmarks property.
     */
    @JsonProperty(value = "faceLandmarks")
    private FaceLandmarks faceLandmarks;

    /**
     * Get the faceId value.
     *
     * @return the faceId value
     */
    public String faceId() {
        return this.faceId;
    }

    /**
     * Set the faceId value.
     *
     * @param faceId the faceId value to set
     * @return the DetectedFaceInner object itself.
     */
    public DetectedFaceInner withFaceId(String faceId) {
        this.faceId = faceId;
        return this;
    }

    /**
     * Get the faceRectangle value.
     *
     * @return the faceRectangle value
     */
    public FaceRectangle faceRectangle() {
        return this.faceRectangle;
    }

    /**
     * Set the faceRectangle value.
     *
     * @param faceRectangle the faceRectangle value to set
     * @return the DetectedFaceInner object itself.
     */
    public DetectedFaceInner withFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
        return this;
    }

    /**
     * Get the faceLandmarks value.
     *
     * @return the faceLandmarks value
     */
    public FaceLandmarks faceLandmarks() {
        return this.faceLandmarks;
    }

    /**
     * Set the faceLandmarks value.
     *
     * @param faceLandmarks the faceLandmarks value to set
     * @return the DetectedFaceInner object itself.
     */
    public DetectedFaceInner withFaceLandmarks(FaceLandmarks faceLandmarks) {
        this.faceLandmarks = faceLandmarks;
        return this;
    }

}
