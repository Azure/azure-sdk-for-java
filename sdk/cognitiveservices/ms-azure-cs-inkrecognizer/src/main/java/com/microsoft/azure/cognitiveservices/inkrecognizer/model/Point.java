// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

/**
 * The Point class unlike the InkPoint interface represents a single geometric position on a plane. The point is used to
 * specify the center point of the bounding rectangle of a recognition unit as well as the well formed points of a
 * recognized shape.
 * @author Microsoft
 * @version 1.0
 */
public class Point {

    private final float x;
    private final float y;

    Point() {
        this(0, 0);
    }

    Point(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    /**
     * Retrieves the x coordinate for the point.
     * @return The x coordinate.
     */
    public float x() {
        return x;
    }

    /**
     * Retrieves the y coordinate for the point.
     * @return The y coordinate.
     */
    public float y() {
        return y;
    }

}
