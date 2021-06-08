// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents the x and y coordinates of a point.
 */
@Immutable
public final class Point {

    /*
     * The x-axis point coordinate.
     */
    private final float xCoordinate;

    /*
     * The y-axis point coordinate.
     */
    private final float yCoordinate;

    /**
     * Creates a Point object.
     *
     * @param xCoordinate The x-axis point coordinate.
     * @param yCoordinate The y-axis point coordinate.
     */
    public Point(final float xCoordinate, final float yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    /**
     * Gets the x-coordinate value.
     *
     * @return The x-axis coordinate value.
     */
    public float getX() {
        return this.xCoordinate;
    }

    /**
     * Gets the y-coordinate value.
     *
     * @return The y-axis coordinate value.
     */
    public float getY() {
        return this.yCoordinate;
    }
}
