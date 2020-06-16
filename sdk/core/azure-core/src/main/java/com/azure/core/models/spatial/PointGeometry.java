// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

/**
 * Represents a geometric point.
 */
public final class PointGeometry extends Geometry {
    private final GeometryPosition position;

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeometryPosition geometric position} of the point.
     */
    public PointGeometry(GeometryPosition position) {
        this(position, null);
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeometryPosition geometric position} of the point.
     * @param properties Additional properties of the geometric point.
     */
    public PointGeometry(GeometryPosition position, GeometryProperties properties) {
        super(properties);

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
}
