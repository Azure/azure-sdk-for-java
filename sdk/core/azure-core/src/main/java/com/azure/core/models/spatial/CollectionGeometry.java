// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a heterogeneous collection of {@link Geometry geometries}.
 */
public final class CollectionGeometry extends Geometry {
    private final List<Geometry> geometries;

    /**
     * Constructs a geometry collection.
     *
     * @param geometries The geometries in the collection.
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
     */
    public CollectionGeometry(List<Geometry> geometries, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        this.geometries = geometries;
    }

    /**
     * Unmodifiable representation of the {@link Geometry geometries} contained in this collection.
     *
     * @return An unmodifiable representation of the {@link Geometry geometries} in this collection.
     */
    public List<Geometry> getGeometries() {
        return Collections.unmodifiableList(geometries);
    }
}
