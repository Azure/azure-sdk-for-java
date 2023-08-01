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
 * Represents a geometric polygon.
 */
@Immutable
public final class GeoPolygon extends GeoObject {
    private final List<GeoLinearRing> rings;

    /**
     * Constructs a geometric polygon.
     *
     * @param ring The {@link GeoLinearRing ring} that defines the polygon.
     * @throws NullPointerException If {@code ring} is {@code null}.
     */
    public GeoPolygon(GeoLinearRing ring) {
        this(ring, null, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param ring The {@link GeoLinearRing ring} that defines the polygon.
     * @param boundingBox Bounding box for the polygon.
     * @param customProperties Additional properties of the polygon.
     * @throws NullPointerException If {@code ring} is {@code null}.
     */
    public GeoPolygon(GeoLinearRing ring, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        this(Collections.singletonList(Objects.requireNonNull(ring, "'ring' cannot be null.")), boundingBox,
            customProperties);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The {@link GeoLinearRing rings} that define the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public GeoPolygon(List<GeoLinearRing> rings) {
        this(rings, null, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The {@link GeoLinearRing rings} that define the polygon.
     * @param boundingBox Bounding box for the polygon.
     * @param customProperties Additional properties of the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public GeoPolygon(List<GeoLinearRing> rings, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(rings, "'rings' cannot be null.");
        this.rings = Collections.unmodifiableList(new ArrayList<>(rings));
    }

    /**
     * Unmodifiable representation of the {@link GeoLinearRing geometric rings} representing this polygon.
     *
     * @return An unmodifiable representation of the {@link GeoLinearRing geometric rings} representing this polygon.
     */
    public List<GeoLinearRing> getRings() {
        return rings;
    }

    /**
     * Gets the outer ring of the polygon.
     *
     * @return Outer ring of the polygon.
     */
    public GeoLinearRing getOuterRing() {
        return rings.get(0);
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this polygon.
     *
     * @return Unmodifiable representation of the {@link GeoPosition geometric positions} representing this polygon.
     */
    GeoArray<GeoArray<GeoPosition>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.POLYGON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rings, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPolygon)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPolygon other = (GeoPolygon) obj;

        return super.equals(obj) && Objects.equals(rings, other.rings);
    }
}
