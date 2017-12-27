/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OCR candidate text.
 */
public class Candidate {
    /**
     * The text found.
     */
    @JsonProperty(value = "Text")
    private String text;

    /**
     * The confidence level.
     */
    @JsonProperty(value = "Confidence")
    private Double confidence;

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
     * @return the Candidate object itself.
     */
    public Candidate withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the confidence value.
     *
     * @return the confidence value
     */
    public Double confidence() {
        return this.confidence;
    }

    /**
     * Set the confidence value.
     *
     * @param confidence the confidence value to set
     * @return the Candidate object itself.
     */
    public Candidate withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

}
