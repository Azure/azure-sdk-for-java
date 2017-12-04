/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties describing occulusions on a given face.
 */
public class OcclusionProperties {
    /**
     * A boolean value indicating whether forehead is occluded.
     */
    @JsonProperty(value = "foreheadOccluded")
    private Boolean foreheadOccluded;

    /**
     * A boolean value indicating whether eyes are occluded.
     */
    @JsonProperty(value = "eyeOccluded")
    private Boolean eyeOccluded;

    /**
     * A boolean value indicating whether the mouth is occluded.
     */
    @JsonProperty(value = "mouthOccluded")
    private Boolean mouthOccluded;

    /**
     * Get the foreheadOccluded value.
     *
     * @return the foreheadOccluded value
     */
    public Boolean foreheadOccluded() {
        return this.foreheadOccluded;
    }

    /**
     * Set the foreheadOccluded value.
     *
     * @param foreheadOccluded the foreheadOccluded value to set
     * @return the OcclusionProperties object itself.
     */
    public OcclusionProperties withForeheadOccluded(Boolean foreheadOccluded) {
        this.foreheadOccluded = foreheadOccluded;
        return this;
    }

    /**
     * Get the eyeOccluded value.
     *
     * @return the eyeOccluded value
     */
    public Boolean eyeOccluded() {
        return this.eyeOccluded;
    }

    /**
     * Set the eyeOccluded value.
     *
     * @param eyeOccluded the eyeOccluded value to set
     * @return the OcclusionProperties object itself.
     */
    public OcclusionProperties withEyeOccluded(Boolean eyeOccluded) {
        this.eyeOccluded = eyeOccluded;
        return this;
    }

    /**
     * Get the mouthOccluded value.
     *
     * @return the mouthOccluded value
     */
    public Boolean mouthOccluded() {
        return this.mouthOccluded;
    }

    /**
     * Set the mouthOccluded value.
     *
     * @param mouthOccluded the mouthOccluded value to set
     * @return the OcclusionProperties object itself.
     */
    public OcclusionProperties withMouthOccluded(Boolean mouthOccluded) {
        this.mouthOccluded = mouthOccluded;
        return this;
    }

}
