/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Address details.
 */
public class AddressProperties {
    /**
     * Detected Address.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Index(Location) of the Address in the input text content.
     */
    @JsonProperty(value = "index")
    private Double index;

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
     * @return the AddressProperties object itself.
     */
    public AddressProperties withText(String text) {
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
     * @return the AddressProperties object itself.
     */
    public AddressProperties withIndex(Double index) {
        this.index = index;
        return this;
    }

}
