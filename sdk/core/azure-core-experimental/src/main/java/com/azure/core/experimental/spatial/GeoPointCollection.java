// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a collection of {@link GeoPoint GeoPoints}.
 */
public final class GeoPointCollection extends GeoObject {
    private final List<GeoPoint> points;

    /**
     * Constructs a {@link GeoPointCollection}.
     *
     * @param points The points that define the multi-point.
     * @throws NullPointerException If {@code points} is {@code null}.
     */
    public GeoPointCollection(List<GeoPoint> points) {
        this(points, null, null);
    }

    /**
     * Constructs a {@link GeoPointCollection}.
     *
     * @param points The points that define the multi-point.
     * @param boundingBox Bounding box for the multi-point.
     * @param properties Additional properties of the multi-point.
     * @throws NullPointerException If {@code points} is {@code null}.
     */
    public GeoPointCollection(List<GeoPoint> points, GeoBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(points, "'points' cannot be null.");
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    /**
     * Unmodifiable representation of the {@link GeoPoint geometric points} representing this multi-point.
     *
     * @return An unmodifiable representation of the {@link GeoPoint geometric points} representing this
     * multi-point.
     */
    public List<GeoPoint> getPoints() {
        return points;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPointCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPointCollection other = (GeoPointCollection) obj;

        return super.equals(obj) && Objects.equals(points, other.points);
    }
}
