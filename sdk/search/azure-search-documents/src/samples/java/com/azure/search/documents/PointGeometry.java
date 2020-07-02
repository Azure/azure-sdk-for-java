// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.search.documents;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric point.
 */
public final class PointGeometry {
    private final GeometryBoundingBox boundingBox;
    private final Map<String, Object> properties;

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
        Objects.requireNonNull(position, "'position' cannot be null.");

        this.boundingBox = boundingBox;
        this.properties = properties;
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

    /**
     * Bounding box for this geometry.
     *
     * @return The bounding box for this geometry.
     */
    public GeometryBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Additional properties about this geometry.
     *
     * @return An unmodifiable representation of the additional properties associated with this geometry.
     */
    public Map<String, Object> getProperties() {
        return properties;
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

        return Objects.equals(boundingBox, other.boundingBox)
            && Objects.equals(properties, other.properties)
            && Objects.equals(position, other.position);
    }
}
