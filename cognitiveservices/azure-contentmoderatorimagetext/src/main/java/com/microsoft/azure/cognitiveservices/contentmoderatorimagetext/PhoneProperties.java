/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Phone Property details.
 */
public class PhoneProperties {
    /**
     * CountryCode of the detected Phone number.
     */
    @JsonProperty(value = "countryCode")
    private String countryCode;

    /**
     * Detected Phone number.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Index(Location) of the Phone number in the input text content.
     */
    @JsonProperty(value = "index")
    private Double index;

    /**
     * Get the countryCode value.
     *
     * @return the countryCode value
     */
    public String countryCode() {
        return this.countryCode;
    }

    /**
     * Set the countryCode value.
     *
     * @param countryCode the countryCode value to set
     * @return the PhoneProperties object itself.
     */
    public PhoneProperties withCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
     * @return the PhoneProperties object itself.
     */
    public PhoneProperties withText(String text) {
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
     * @return the PhoneProperties object itself.
     */
    public PhoneProperties withIndex(Double index) {
        this.index = index;
        return this;
    }

}
