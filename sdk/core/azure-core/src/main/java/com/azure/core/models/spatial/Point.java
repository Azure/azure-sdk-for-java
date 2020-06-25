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
    private final double xCoordinate;

    /*
     * The y-axis point coordinate.
     */
    private final double yCoordinate;

    /**
     * Constructs a Point object.
     *
     * @param xCoordinate The x-axis point coordinate.
     * @param yCoordinate The y-axis point coordinate.
     */
    public Point(final double xCoordinate, final double yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    /**
     * Gets the x-coordinate value.
     *
     * @return The x-axis coordinate value.
     */
    public double getX() {
        return this.xCoordinate;
    }

    /**
     * Gets the y-coordinate value.
     *
     * @return The y-axis coordinate value.
     */
    public double getY() {
        return this.yCoordinate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }

        final Point point = (Point) o;

        return xCoordinate == point.xCoordinate
            && yCoordinate == point.yCoordinate;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(xCoordinate);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yCoordinate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
