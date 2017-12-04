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
public class ImageTag {
    /**
     * The tag value.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * The level of confidence the service has in the caption.
     */
    @JsonProperty(value = "confidence")
    private Double confidence;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the ImageTag object itself.
     */
    public ImageTag withName(String name) {
        this.name = name;
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
     * @return the ImageTag object itself.
     */
    public ImageTag withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

}
