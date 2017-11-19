/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An image caption, i.e. a brief description of what the image depicts.
 */
public class ImageCaption {
    /**
     * The text of the caption.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * The level of confidence the service has in the caption.
     */
    @JsonProperty(value = "confidence")
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
     * @return the ImageCaption object itself.
     */
    public ImageCaption withText(String text) {
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
     * @return the ImageCaption object itself.
     */
    public ImageCaption withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

}
