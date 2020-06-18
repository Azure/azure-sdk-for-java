// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quadrangle bounding box, with coordinates specified relative to the top-left of the original image
 */
@Immutable
public final class BoundingBox {

    /**
     * The list of coordinates of the Bounding box.
     */
    private final List<Point> points;

    /**
     * Constructs a Bounding box object.
     *
     * @param points The list of coordinates of the Bounding box.
     */
    public BoundingBox(final List<Point> points) {
        if (points == null) {
            this.points = null;
        } else {
            this.points = Collections.unmodifiableList(new ArrayList<>(points));
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
