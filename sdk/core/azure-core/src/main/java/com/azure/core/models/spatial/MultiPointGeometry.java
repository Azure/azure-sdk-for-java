// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a multi-point geometry.
 */
public final class MultiPointGeometry extends Geometry {
    private final List<PointGeometry> points;

    /**
     * Constructs a multi-point geometry.
     *
     * @param points The points that define the multi-point.
     */
    public MultiPointGeometry(List<PointGeometry> points) {
        this(points, null, null);
    }

    /**
     * Constructs a multi-point geometry.
     *
     * @param points The points that define the multi-point.
     * @param boundingBox Bounding box for the multi-point.
     * @param properties Additional properties of the multi-point.
     */
    public MultiPointGeometry(List<PointGeometry> points, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        this.points = points;
    }

    /**
     * Unmodifiable representation of the {@link PointGeometry geometric points} representing this multi-point.
     *
     * @return An unmodifiable representation of the {@link PointGeometry geometric points} representing this
     * multi-point.
     */
    public List<PointGeometry> getPoints() {
        return Collections.unmodifiableList(points);
    }
}
