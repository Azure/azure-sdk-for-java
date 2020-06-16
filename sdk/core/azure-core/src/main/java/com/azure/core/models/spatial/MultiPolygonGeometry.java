// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a multi-polygon geometry.
 */
public final class MultiPolygonGeometry extends Geometry {
    private final List<PolygonGeometry> polygons;

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public MultiPolygonGeometry(List<PolygonGeometry> polygons) {
        this(polygons, null, null);
    }

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @param boundingBox Bounding box for the multi-polygon.
     * @param properties Additional properties of the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public MultiPolygonGeometry(List<PolygonGeometry> polygons, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(polygons, "'polygons' cannot be null.");
        this.polygons = Collections.unmodifiableList(new ArrayList<>(polygons));
    }

    /**
     * Unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this
     * multi-polygon.
     */
    public List<PolygonGeometry> getPolygons() {
        return polygons;
    }
}
