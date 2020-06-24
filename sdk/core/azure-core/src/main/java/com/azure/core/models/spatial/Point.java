// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import com.azure.core.annotation.Immutable;

/**
 * Represents a location in (x, y) coordinate space.
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
     * Constructs a Point object.
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Point point = (Point) o;

        if (Float.compare(point.xCoordinate, xCoordinate) != 0) return false;
        return Float.compare(point.yCoordinate, yCoordinate) == 0;
    }

    @Override
    public int hashCode() {
        int result = (xCoordinate != +0.0f ? Float.floatToIntBits(xCoordinate) : 0);
        result = 31 * result + (yCoordinate != +0.0f ? Float.floatToIntBits(yCoordinate) : 0);
        return result;
    }
}
