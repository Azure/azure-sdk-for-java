// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * Quadrangle bounding box, with coordinates specified relative to the top-left of the original image
 */
@Immutable
public class BoundingBox {
    /**
     * Coordinates specified relative to the top-left of the element in {@link Point}.
     */
    private List<Point> coordinates;

    /**
     * List of {@link Point points} specifying relative coordinates of the element.
     *
     * @param coordinates list of {@link Point points}
     */
    public BoundingBox(final List<Point> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Get the list of {@link Point points} specifying relative coordinates of the element.
     *
     * @return List of {@link Point points}.
     */
    public List<Point> getCoordinates() {
        return coordinates;
    }
}
