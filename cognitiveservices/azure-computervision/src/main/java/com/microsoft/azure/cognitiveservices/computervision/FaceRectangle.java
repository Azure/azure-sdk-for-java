/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object describing face rectangle.
 */
public class FaceRectangle {
    /**
     * X-coordinate of the top left point of the face.
     */
    @JsonProperty(value = "left")
    private Integer left;

    /**
     * Y-coordinate of the top left point of the face.
     */
    @JsonProperty(value = "top")
    private Integer top;

    /**
     * Width measured from the top-left point of the face.
     */
    @JsonProperty(value = "width")
    private Integer width;

    /**
     * Height measured from the top-left point of the face.
     */
    @JsonProperty(value = "height")
    private Integer height;

    /**
     * Get the left value.
     *
     * @return the left value
     */
    public Integer left() {
        return this.left;
    }

    /**
     * Set the left value.
     *
     * @param left the left value to set
     * @return the FaceRectangle object itself.
     */
    public FaceRectangle withLeft(Integer left) {
        this.left = left;
        return this;
    }

    /**
     * Get the top value.
     *
     * @return the top value
     */
    public Integer top() {
        return this.top;
    }

    /**
     * Set the top value.
     *
     * @param top the top value to set
     * @return the FaceRectangle object itself.
     */
    public FaceRectangle withTop(Integer top) {
        this.top = top;
        return this;
    }

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
     * @return the FaceRectangle object itself.
     */
    public FaceRectangle withWidth(Integer width) {
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
     * @return the FaceRectangle object itself.
     */
    public FaceRectangle withHeight(Integer height) {
        this.height = height;
        return this;
    }

}
