/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Input model.
 */
public class Input {
    /**
     * Unique, non-empty document identifier.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * The text property.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the Input object itself.
     */
    public Input withId(String id) {
        this.id = id;
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
     * @return the Input object itself.
     */
    public Input withText(String text) {
        this.text = text;
        return this;
    }

}
