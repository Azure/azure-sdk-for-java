// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric polygon.
 */
public final class GeoPolygon extends GeoObject {
    private final List<GeoLine> rings;

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The lines that define the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public GeoPolygon(List<GeoLine> rings) {
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
    public GeoPolygon(List<GeoLine> rings, GeoBoundingBox boundingBox, Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(rings, "'rings' cannot be null.");
        this.rings = Collections.unmodifiableList(new ArrayList<>(rings));
    }

    /**
     * Unmodifiable representation of the {@link GeoLine geometric lines} representing this polygon.
     *
     * @return An unmodifiable representation of the {@link GeoLine geometric lines} representing this polygon.
     */
    public List<GeoLine> getRings() {
        return rings;
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
