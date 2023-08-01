// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a heterogeneous collection of {@link GeoObject GeoObjects}.
 */
@Immutable
public final class GeoCollection extends GeoObject {
    private final List<GeoObject> geometries;

    /**
     * Constructs a {@link GeoCollection}.
     *
     * @param geometries The geometries in the collection.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public GeoCollection(List<GeoObject> geometries) {
        this(geometries, null, null);
    }

    /**
     * Constructs a {@link GeoCollection}.
     *
     * @param geometries The geometries in the collection.
     * @param boundingBox Bounding box for the {@link GeoCollection}.
     * @param customProperties Additional properties of the {@link GeoCollection}.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public GeoCollection(List<GeoObject> geometries, GeoBoundingBox boundingBox,
        Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(geometries, "'geometries' cannot be null.");
        this.geometries = Collections.unmodifiableList(new ArrayList<>(geometries));
    }

    /**
     * Unmodifiable representation of the {@link GeoObject geometries} contained in this collection.
     *
     * @return An unmodifiable representation of the {@link GeoObject geometries} in this collection.
     */
    public List<GeoObject> getGeometries() {
        return geometries;
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.GEOMETRY_COLLECTION;
    }

    @Override
    public int hashCode() {
        return Objects.hash(geometries, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoCollection other = (GeoCollection) obj;
        return super.equals(other) && Objects.equals(geometries, other.geometries);
    }
}
