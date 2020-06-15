// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a geometric line.
 */
public final class LineGeometry extends Geometry {
    private final Collection<GeometryPosition> positions;

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     */
    public LineGeometry(Collection<GeometryPosition> positions) {
        this(positions, null);
    }

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     * @param properties Additional properties of the geometric line.
     */
    public LineGeometry(Collection<GeometryPosition> positions, GeometryProperties properties) {
        super(properties);
        this.positions = positions;
    }

    /**
     * Unmodifiable representation of the {@link GeometryPosition geometric positions} representing this line.
     *
     * @return An unmodifiable representation of the {@link GeometryPosition geometric positions} representing this
     * line.
     */
    public Collection<GeometryPosition> getPositions() {
        return Collections.unmodifiableCollection(positions);
    }
}
