/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IP Address details.
 */
public class IPAProperties {
    /**
     * Subtype of the detected IP Address.
     */
    @JsonProperty(value = "subType")
    private String subType;

    /**
     * Detected IP Address.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Index(Location) of the IP Address in the input text content.
     */
    @JsonProperty(value = "index")
    private Double index;

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
     * @return the IPAProperties object itself.
     */
    public IPAProperties withSubType(String subType) {
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
     * @return the IPAProperties object itself.
     */
    public IPAProperties withText(String text) {
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
     * @return the IPAProperties object itself.
     */
    public IPAProperties withIndex(Double index) {
        this.index = index;
        return this;
    }

}
