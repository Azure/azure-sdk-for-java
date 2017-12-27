/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties describing hair attributes.
 */
public class HairProperties {
    /**
     * A number describing confidence level of whether the person is bald.
     */
    @JsonProperty(value = "bald")
    private double bald;

    /**
     * A boolean value describing whether the hair is visible in the image.
     */
    @JsonProperty(value = "invisible")
    private boolean invisible;

    /**
     * The hairColor property.
     */
    @JsonProperty(value = "hairColor")
    private List<ColorProperty> hairColor;

    /**
     * Get the bald value.
     *
     * @return the bald value
     */
    public double bald() {
        return this.bald;
    }

    /**
     * Set the bald value.
     *
     * @param bald the bald value to set
     * @return the HairProperties object itself.
     */
    public HairProperties withBald(double bald) {
        this.bald = bald;
        return this;
    }

    /**
     * Get the invisible value.
     *
     * @return the invisible value
     */
    public boolean invisible() {
        return this.invisible;
    }

    /**
     * Set the invisible value.
     *
     * @param invisible the invisible value to set
     * @return the HairProperties object itself.
     */
    public HairProperties withInvisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    /**
     * Get the hairColor value.
     *
     * @return the hairColor value
     */
    public List<ColorProperty> hairColor() {
        return this.hairColor;
    }

    /**
     * Set the hairColor value.
     *
     * @param hairColor the hairColor value to set
     * @return the HairProperties object itself.
     */
    public HairProperties withHairColor(List<ColorProperty> hairColor) {
        this.hairColor = hairColor;
        return this;
    }

}
