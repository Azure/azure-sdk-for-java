/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties describing present makeups on a given face.
 */
public class MakeupProperties {
    /**
     * A boolean value describing whether eye makeup is present on a face.
     */
    @JsonProperty(value = "eyeMakeup")
    private Boolean eyeMakeup;

    /**
     * A boolean value describing whether lip makeup is present on a face.
     */
    @JsonProperty(value = "lipMakeup")
    private Boolean lipMakeup;

    /**
     * Get the eyeMakeup value.
     *
     * @return the eyeMakeup value
     */
    public Boolean eyeMakeup() {
        return this.eyeMakeup;
    }

    /**
     * Set the eyeMakeup value.
     *
     * @param eyeMakeup the eyeMakeup value to set
     * @return the MakeupProperties object itself.
     */
    public MakeupProperties withEyeMakeup(Boolean eyeMakeup) {
        this.eyeMakeup = eyeMakeup;
        return this;
    }

    /**
     * Get the lipMakeup value.
     *
     * @return the lipMakeup value
     */
    public Boolean lipMakeup() {
        return this.lipMakeup;
    }

    /**
     * Set the lipMakeup value.
     *
     * @param lipMakeup the lipMakeup value to set
     * @return the MakeupProperties object itself.
     */
    public MakeupProperties withLipMakeup(Boolean lipMakeup) {
        this.lipMakeup = lipMakeup;
        return this;
    }

}
