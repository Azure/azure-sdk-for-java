// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

/**
 * The Rectangle class is used to deliver the bounding rectangle of a recognition unit.
 */
public class Rectangle {

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    Rectangle() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    Rectangle(double x, double y, double width, double height) {
        this.x = (float) x;
        this.y = (float) y;
        this.width = (float) width;
        this.height = (float) height;
    }

    /**
     * Retrieves the x coordinate for the top left point of the rectangle.
     * @return The x coordinate value.
     */
    public float x() {
        return x;
    }

    /**
     * Retrieves the y coordinate for the top left point of the rectangle.
     * @return The y coordinate value.
     */
    public float y() {
        return y;
    }

    /**
     * Retrieves the width of the rectangle.
     * @return The width of the rectangle.
     */
    public float width() {
        return width;
    }

    /**
     * Retrieves the height of the rectangle.
     * @return The height of the rectangle.
     */
    public float height() {
        return height;
    }

}
