/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object providing possible image types and matching confidence levels.
 */
public class ImageType {
    /**
     * Confidence level that the image is a clip art.
     */
    @JsonProperty(value = "clipArtType")
    private Double clipArtType;

    /**
     * Confidence level that the image is a line drawing.
     */
    @JsonProperty(value = "lineDrawingType")
    private Double lineDrawingType;

    /**
     * Get the clipArtType value.
     *
     * @return the clipArtType value
     */
    public Double clipArtType() {
        return this.clipArtType;
    }

    /**
     * Set the clipArtType value.
     *
     * @param clipArtType the clipArtType value to set
     * @return the ImageType object itself.
     */
    public ImageType withClipArtType(Double clipArtType) {
        this.clipArtType = clipArtType;
        return this;
    }

    /**
     * Get the lineDrawingType value.
     *
     * @return the lineDrawingType value
     */
    public Double lineDrawingType() {
        return this.lineDrawingType;
    }

    /**
     * Set the lineDrawingType value.
     *
     * @param lineDrawingType the lineDrawingType value to set
     * @return the ImageType object itself.
     */
    public ImageType withLineDrawingType(Double lineDrawingType) {
        this.lineDrawingType = lineDrawingType;
        return this;
    }

}
