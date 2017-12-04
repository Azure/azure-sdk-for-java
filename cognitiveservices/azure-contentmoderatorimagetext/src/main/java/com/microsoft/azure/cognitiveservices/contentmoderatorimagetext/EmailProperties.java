/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Email Address details.
 */
public class EmailProperties {
    /**
     * Detected Email Address from the input text content.
     */
    @JsonProperty(value = "detected")
    private String detected;

    /**
     * Subtype of the detected Email Address.
     */
    @JsonProperty(value = "subType")
    private String subType;

    /**
     * Email Address in the input text content.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Index(Location) of the Email address in the input text content.
     */
    @JsonProperty(value = "index")
    private Double index;

    /**
     * Get the detected value.
     *
     * @return the detected value
     */
    public String detected() {
        return this.detected;
    }

    /**
     * Set the detected value.
     *
     * @param detected the detected value to set
     * @return the EmailProperties object itself.
     */
    public EmailProperties withDetected(String detected) {
        this.detected = detected;
        return this;
    }

    /**
     * Get the subType value.
     *
     * @return the subType value
     */
    public String subType() {
        return this.subType;
    }

    /**
     * Set the subType value.
     *
     * @param subType the subType value to set
     * @return the EmailProperties object itself.
     */
    public EmailProperties withSubType(String subType) {
        this.subType = subType;
        return this;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the EmailProperties object itself.
     */
    public EmailProperties withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the index value.
     *
     * @return the index value
     */
    public Double index() {
        return this.index;
    }

    /**
     * Set the index value.
     *
     * @param index the index value to set
     * @return the EmailProperties object itself.
     */
    public EmailProperties withIndex(Double index) {
        this.index = index;
        return this;
    }

}
