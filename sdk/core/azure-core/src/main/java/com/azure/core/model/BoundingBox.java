// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.model;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * A list of points representing the quadrilateral bounding box. The points are listed in clockwise
 * order: top-left, top-right, bottom-right, bottom-left.
 */
@Immutable
public final class BoundingBox {

    /**
     * The list of coordinates of the bounding box.
     */
    private final List<Point> points;

    /**
     * Constructs a Bounding box object with the specified list of coordinates.
     *
     * @param points The list of coordinates of the Bounding box.
     */
    public BoundingBox(final List<Point> points) {
        if (points == null) {
            this.points = null;
        } else {
            this.points = Collections.unmodifiableList(points);
        }
    }

    /**
     * Gets the list of all point coordinates of the bounding box.
     *
     * @return The unmodifiable list of all point coordinates of the Bounding box.
     */
    public List<Point> getPoints() {
        return this.points;
    }
}
