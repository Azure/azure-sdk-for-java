/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IP Address details.
 */
public class IPA {
    /**
     * Subtype of the detected IP Address.
     */
    @JsonProperty(value = "SubType")
    private String subType;

    /**
     * Detected IP Address.
     */
    @JsonProperty(value = "Text")
    private String text;

    /**
     * Index(Location) of the IP Address in the input text content.
     */
    @JsonProperty(value = "Index")
    private Integer index;

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
     * @return the IPA object itself.
     */
    public IPA withSubType(String subType) {
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
     * @return the IPA object itself.
     */
    public IPA withText(String text) {
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
     * @return the IPA object itself.
     */
    public IPA withIndex(Integer index) {
        this.index = index;
        return this;
    }

}
