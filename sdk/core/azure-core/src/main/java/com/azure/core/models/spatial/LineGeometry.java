// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a geometric line.
 */
public final class LineGeometry extends Geometry {
    private final List<GeometryPosition> positions;

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     */
    public LineGeometry(List<GeometryPosition> positions) {
        this(positions, null, null);
    }

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     * @param boundingBox Bounding box for the line.
     * @param properties Additional properties of the geometric line.
     */
    public LineGeometry(List<GeometryPosition> positions, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        this.positions = positions;
    }

    /**
     * Unmodifiable representation of the {@link GeometryPosition geometric positions} representing this line.
     *
     * @return An unmodifiable representation of the {@link GeometryPosition geometric positions} representing this
     * line.
     */
    public List<GeometryPosition> getPositions() {
        return Collections.unmodifiableList(positions);
    }
}
