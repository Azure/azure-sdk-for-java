// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric polygon.
 */
public final class PolygonGeometry extends Geometry {
    private final List<LineGeometry> rings;

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The lines that define the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public PolygonGeometry(List<LineGeometry> rings) {
        this(rings, null, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The lines that define the polygon.
     * @param boundingBox Bounding box for the polygon.
     * @param properties Additional properties of the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public PolygonGeometry(List<LineGeometry> rings, GeometryBoundingBox boundingBox, Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(rings, "'rings' cannot be null.");
        this.rings = Collections.unmodifiableList(new ArrayList<>(rings));
    }

    /**
     * Unmodifiable representation of the {@link LineGeometry geometric lines} representing this polygon.
     *
     * @return An unmodifiable representation of the {@link LineGeometry geometric lines} representing this polygon.
     */
    public List<LineGeometry> getRings() {
        return rings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rings, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PolygonGeometry)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        PolygonGeometry other = (PolygonGeometry) obj;

        return super.equals(obj) && Objects.equals(rings, other.rings);
    }
}
