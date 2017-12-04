/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Accessory item and corresponding confidence level.
 */
public class AccessoryItem {
    /**
     * Description of an accessory.
     */
    @JsonProperty(value = "type")
    private String type;

    /**
     * The confidence property.
     */
    @JsonProperty(value = "confidence")
    private double confidence;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the AccessoryItem object itself.
     */
    public AccessoryItem withType(String type) {
        this.type = type;
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
     * @return the AccessoryItem object itself.
     */
    public AccessoryItem withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

}
