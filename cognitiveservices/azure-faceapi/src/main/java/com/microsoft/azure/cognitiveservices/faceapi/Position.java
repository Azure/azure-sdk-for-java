/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coordinates within an image.
 */
public class Position {
    /**
     * The horizontal component, in pixels.
     */
    @JsonProperty(value = "x", required = true)
    private double x;

    /**
     * The vertical component, in pixels.
     */
    @JsonProperty(value = "y", required = true)
    private double y;

    /**
     * Get the x value.
     *
     * @return the x value
     */
    public double x() {
        return this.x;
    }

    /**
     * Set the x value.
     *
     * @param x the x value to set
     * @return the Position object itself.
     */
    public Position withX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Get the y value.
     *
     * @return the y value
     */
    public double y() {
        return this.y;
    }

    /**
     * Set the y value.
     *
     * @param y the y value to set
     * @return the Position object itself.
     */
    public Position withY(double y) {
        this.y = y;
        return this;
    }

}
