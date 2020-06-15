// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a heterogeneous collection of {@link Geometry geometries}.
 */
public final class CollectionGeometry extends Geometry {
    private final Collection<Geometry> geometries;

    /**
     * Constructs a geometry collection.
     *
     * @param geometries The geometries in the collection.
     */
    public CollectionGeometry(Collection<Geometry> geometries) {
        this(geometries, null);
    }

    /**
     * Constructs a geometry collection.
     *
     * @param geometries The geometries in the collection.
     * @param properties Additional properties of the geometry collection.
     */
    public CollectionGeometry(Collection<Geometry> geometries, GeometryProperties properties) {
        super(properties);
        this.geometries = geometries;
    }

    /**
     * Unmodifiable representation of the {@link Geometry geometries} contained in this collection.
     *
     * @return An unmodifiable representation of the {@link Geometry geometries} in this collection.
     */
    public Collection<Geometry> getGeometries() {
        return Collections.unmodifiableCollection(geometries);
    }
}
