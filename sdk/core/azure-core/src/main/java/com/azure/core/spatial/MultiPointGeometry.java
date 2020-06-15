// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a multi-point geometry.
 */
public final class MultiPointGeometry extends Geometry {
    private final Collection<PointGeometry> points;

    /**
     * Constructs a multi-point geometry.
     *
     * @param points The points that define the multi-point.
     */
    public MultiPointGeometry(Collection<PointGeometry> points) {
        this(points, null);
    }

    /**
     * Constructs a multi-point geometry.
     *
     * @param points The points that define the multi-point.
     * @param properties Additional properties of the multi-point.
     */
    public MultiPointGeometry(Collection<PointGeometry> points, GeometryProperties properties) {
        super(properties);

        this.points = points;
    }

    /**
     * Unmodifiable representation of the {@link PointGeometry geometric points} representing this multi-point.
     *
     * @return An unmodifiable representation of the {@link PointGeometry geometric points} representing this
     * multi-point.
     */
    public Collection<PointGeometry> getPoints() {
        return Collections.unmodifiableCollection(points);
    }
}
