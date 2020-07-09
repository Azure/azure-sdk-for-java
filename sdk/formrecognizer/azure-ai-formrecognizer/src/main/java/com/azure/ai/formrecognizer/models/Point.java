// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The Point model.
 */
@Immutable
public final class Point {

    /*
     * The x-axis point coordinate.
     */
    private final Float xCoordinate;

    /*
     * The y-axis point coordinate.
     */
    private final Float yCoordinate;

    /**
     * Creates a Point object.
     *
     * @param xCoordinate The x-axis point coordinate.
     * @param yCoordinate The y-axis point coordinate.
     */
    public Point(final Float xCoordinate, final Float yCoordinate) {
        if (xCoordinate != null && yCoordinate != null) {
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
        } else {
            this.xCoordinate = null;
            this.yCoordinate = null;
        }
    }

    /**
     * Gets the x-coordinate value.
     *
     * @return The x-axis coordinate value.
     */
    public Float getX() {
        return this.xCoordinate;
    }

    /**
     * Gets the y-coordinate value.
     *
     * @return The y-axis coordinate value.
     */
    public Float getY() {
        return this.yCoordinate;
    }
}
