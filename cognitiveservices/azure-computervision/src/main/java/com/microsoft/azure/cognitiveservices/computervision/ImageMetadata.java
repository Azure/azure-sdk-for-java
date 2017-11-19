/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Image metadata.
 */
public class ImageMetadata {
    /**
     * Image width.
     */
    @JsonProperty(value = "width")
    private Integer width;

    /**
     * Image height.
     */
    @JsonProperty(value = "height")
    private Integer height;

    /**
     * Image format.
     */
    @JsonProperty(value = "format")
    private String format;

    /**
     * Get the width value.
     *
     * @return the width value
     */
    public Integer width() {
        return this.width;
    }

    /**
     * Set the width value.
     *
     * @param width the width value to set
     * @return the ImageMetadata object itself.
     */
    public ImageMetadata withWidth(Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Get the height value.
     *
     * @return the height value
     */
    public Integer height() {
        return this.height;
    }

    /**
     * Set the height value.
     *
     * @param height the height value to set
     * @return the ImageMetadata object itself.
     */
    public ImageMetadata withHeight(Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Get the format value.
     *
     * @return the format value
     */
    public String format() {
        return this.format;
    }

    /**
     * Set the format value.
     *
     * @param format the format value to set
     * @return the ImageMetadata object itself.
     */
    public ImageMetadata withFormat(String format) {
        this.format = format;
        return this;
    }

}
