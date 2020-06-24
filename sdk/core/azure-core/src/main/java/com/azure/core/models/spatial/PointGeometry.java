// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric point.
 */
public final class PointGeometry extends Geometry {
    private final GeometryPosition position;

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeometryPosition geometric position} of the point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public PointGeometry(GeometryPosition position) {
        this(position, null, null);
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeometryPosition geometric position} of the point.
     * @param boundingBox Bounding box for the point.
     * @param properties Additional properties of the geometric point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public PointGeometry(GeometryPosition position, GeometryBoundingBox boundingBox, Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(position, "'position' cannot be null.");
        this.position = position;
    }

    /**
     * The {@link GeometryPosition geometric position} of the point.
     *
     * @return The {@link GeometryPosition geometric position} of the point.
     */
    public GeometryPosition getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointGeometry)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        PointGeometry other = (PointGeometry) obj;

        return super.equals(obj) && Objects.equals(position, other.position);
    }
}
