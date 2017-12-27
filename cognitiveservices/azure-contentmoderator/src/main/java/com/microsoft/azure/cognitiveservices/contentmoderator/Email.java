/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Email Address details.
 */
public class Email {
    /**
     * Detected Email Address from the input text content.
     */
    @JsonProperty(value = "Detected")
    private String detected;

    /**
     * Subtype of the detected Email Address.
     */
    @JsonProperty(value = "SubType")
    private String subType;

    /**
     * Email Address in the input text content.
     */
    @JsonProperty(value = "Text")
    private String text;

    /**
     * Index(Location) of the Email address in the input text content.
     */
    @JsonProperty(value = "Index")
    private Integer index;

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
     * @return the Email object itself.
     */
    public Email withDetected(String detected) {
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
     * @return the Email object itself.
     */
    public Email withSubType(String subType) {
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
     * @return the Email object itself.
     */
    public Email withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the index value.
     *
     * @return the index value
     */
    public Integer index() {
        return this.index;
    }

    /**
     * Set the index value.
     *
     * @param index the index value to set
     * @return the Email object itself.
     */
    public Email withIndex(Integer index) {
        this.index = index;
        return this;
    }

}
