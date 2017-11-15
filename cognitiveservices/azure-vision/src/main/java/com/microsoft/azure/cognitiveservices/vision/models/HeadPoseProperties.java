/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties indicating head pose of the face.
 */
public class HeadPoseProperties {
    /**
     * The roll property.
     */
    @JsonProperty(value = "roll")
    private Double roll;

    /**
     * The yaw property.
     */
    @JsonProperty(value = "yaw")
    private Double yaw;

    /**
     * The pitch property.
     */
    @JsonProperty(value = "pitch")
    private Double pitch;

    /**
     * Get the roll value.
     *
     * @return the roll value
     */
    public Double roll() {
        return this.roll;
    }

    /**
     * Set the roll value.
     *
     * @param roll the roll value to set
     * @return the HeadPoseProperties object itself.
     */
    public HeadPoseProperties withRoll(Double roll) {
        this.roll = roll;
        return this;
    }

    /**
     * Get the yaw value.
     *
     * @return the yaw value
     */
    public Double yaw() {
        return this.yaw;
    }

    /**
     * Set the yaw value.
     *
     * @param yaw the yaw value to set
     * @return the HeadPoseProperties object itself.
     */
    public HeadPoseProperties withYaw(Double yaw) {
        this.yaw = yaw;
        return this;
    }

    /**
     * Get the pitch value.
     *
     * @return the pitch value
     */
    public Double pitch() {
        return this.pitch;
    }

    /**
     * Set the pitch value.
     *
     * @param pitch the pitch value to set
     * @return the HeadPoseProperties object itself.
     */
    public HeadPoseProperties withPitch(Double pitch) {
        this.pitch = pitch;
        return this;
    }

}
