// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;

/**
 * Represents a multi-polygon geometry.
 */
public final class MultiPolygonGeometry extends Geometry {
    private final List<PolygonGeometry> polygons;

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     */
    public MultiPolygonGeometry(List<PolygonGeometry> polygons) {
        this(polygons, null);
    }

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @param properties Additional properties of the multi-polygon.
     */
    public MultiPolygonGeometry(List<PolygonGeometry> polygons, GeometryProperties properties) {
        super(properties);

        this.polygons = polygons;
    }

    /**
     * Unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this
     * multi-polygon.
     */
    public List<PolygonGeometry> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }
}
