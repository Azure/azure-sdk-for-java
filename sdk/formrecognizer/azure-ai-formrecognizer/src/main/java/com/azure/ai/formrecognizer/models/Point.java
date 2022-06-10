// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.PointHelper;

/**
 * Represents the x and y coordinates of a vertex.
 */
public final class Point {

    /*
     * The x-axis point coordinate.
     */
    private float xCoordinate;

    /*
     * The y-axis point coordinate.
     */
    private float yCoordinate;

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

    void setX(float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    void setY(float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    static {
        PointHelper.setAccessor(new PointHelper.PointAccessor() {
            @Override
            public void setX(Point point, float xCoordinate) {
                point.setX(xCoordinate);
            }

            @Override
            public void setY(Point point, float yCoordinate) {
                point.setY(yCoordinate);
            }
        });
    }
}
