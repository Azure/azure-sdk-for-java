/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hair color and associated confidence.
 */
public class ColorProperty {
    /**
     * Name of the color.
     */
    @JsonProperty(value = "color")
    private String color;

    /**
     * The confidence property.
     */
    @JsonProperty(value = "confidence")
    private double confidence;

    /**
     * Get the color value.
     *
     * @return the color value
     */
    public String color() {
        return this.color;
    }

    /**
     * Set the color value.
     *
     * @param color the color value to set
     * @return the ColorProperty object itself.
     */
    public ColorProperty withColor(String color) {
        this.color = color;
        return this;
    }

    /**
     * Get the confidence value.
     *
     * @return the confidence value
     */
    public double confidence() {
        return this.confidence;
    }

    /**
     * Set the confidence value.
     *
     * @param confidence the confidence value to set
     * @return the ColorProperty object itself.
     */
    public ColorProperty withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

}
