/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coordinates to the found face.
 */
public class FoundFace {
    /**
     * The bottom coordinate.
     */
    @JsonProperty(value = "bottom")
    private Integer bottom;

    /**
     * The left coordinate.
     */
    @JsonProperty(value = "left")
    private Integer left;

    /**
     * The right coordinate.
     */
    @JsonProperty(value = "right")
    private Integer right;

    /**
     * The top coordinate.
     */
    @JsonProperty(value = "top")
    private Integer top;

    /**
     * Get the bottom value.
     *
     * @return the bottom value
     */
    public Integer bottom() {
        return this.bottom;
    }

    /**
     * Set the bottom value.
     *
     * @param bottom the bottom value to set
     * @return the FoundFace object itself.
     */
    public FoundFace withBottom(Integer bottom) {
        this.bottom = bottom;
        return this;
    }

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
     * @return the FoundFace object itself.
     */
    public FoundFace withLeft(Integer left) {
        this.left = left;
        return this;
    }

    /**
     * Get the right value.
     *
     * @return the right value
     */
    public Integer right() {
        return this.right;
    }

    /**
     * Set the right value.
     *
     * @param right the right value to set
     * @return the FoundFace object itself.
     */
    public FoundFace withRight(Integer right) {
        this.right = right;
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
     * @return the FoundFace object itself.
     */
    public FoundFace withTop(Integer top) {
        this.top = top;
        return this;
    }

}
