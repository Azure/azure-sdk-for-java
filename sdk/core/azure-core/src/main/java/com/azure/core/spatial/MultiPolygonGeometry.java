// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a multi-polygon geometry.
 */
public final class MultiPolygonGeometry extends Geometry {
    private final Collection<PolygonGeometry> polygons;

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     */
    public MultiPolygonGeometry(Collection<PolygonGeometry> polygons) {
        this(polygons, null);
    }

    /**
     * Constructs a multi-polygon geometry.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @param properties Additional properties of the multi-polygon.
     */
    public MultiPolygonGeometry(Collection<PolygonGeometry> polygons, GeometryProperties properties) {
        super(properties);

        this.polygons = polygons;
    }

    /**
     * Unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link PolygonGeometry geometric polygons} representing this
     * multi-polygon.
     */
    public Collection<PolygonGeometry> getPolygons() {
        return Collections.unmodifiableCollection(polygons);
    }
}
