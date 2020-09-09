// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quadrangle bounding box, with coordinates specified relative to the top-left of the original image
 */
@Immutable
public final class FieldBoundingBox {

    /**
     * The list of coordinates of the field's bounding box.
     */
    private final List<Point> points;

    /**
     * Constructs a Field Bounding box object.
     *
     * @param points The list of coordinates of the field's bounding box.
     */
    public FieldBoundingBox(final List<Point> points) {
        if (points == null) {
            this.points = null;
        } else {
            this.points = Collections.unmodifiableList(points);
        }
    }

    /**
     * Gets the list of all point coordinates of the bounding box.
     *
     * @return The unmodifiable list of all point coordinates of the field's bounding box.
     */
    public List<Point> getPoints() {
        return this.points;
    }

    /**
     * Returns a string representation of the {@link FieldBoundingBox}.
     *
     * @return the string representation of the {@link FieldBoundingBox}.
     */
    @Override
    public String toString() {
        return points.stream().map(point -> String.format("[%.2f, %.2f]", point.getX(),
            point.getY())).collect(Collectors.joining(", "));
    }
}
