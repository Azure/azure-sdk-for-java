// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a heterogeneous collection of {@link Geometry geometries}.
 */
public final class CollectionGeometry extends Geometry {
    private final List<Geometry> geometries;

    /**
     * Constructs a geometry collection.
     *
     * @param geometries The geometries in the collection.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public CollectionGeometry(List<Geometry> geometries) {
        this(geometries, null, null);
    }

    /**
     * Constructs a geometry collection.
     *
     * @param geometries The geometries in the collection.
     * @param boundingBox Bounding box for the geometry collection.
     * @param properties Additional properties of the geometry collection.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public CollectionGeometry(List<Geometry> geometries, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(geometries, "'geometries' cannot be null.");
        this.geometries = Collections.unmodifiableList(new ArrayList<>(geometries));
    }

    /**
     * Unmodifiable representation of the {@link Geometry geometries} contained in this collection.
     *
     * @return An unmodifiable representation of the {@link Geometry geometries} in this collection.
     */
    public List<Geometry> getGeometries() {
        return geometries;
    }

    @Override
    public int hashCode() {
        return Objects.hash(geometries, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CollectionGeometry)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        CollectionGeometry other = (CollectionGeometry) obj;

        return super.equals(other) && Objects.equals(geometries, other.geometries);
    }
}
