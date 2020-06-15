// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a geometric polygon.
 */
public final class PolygonGeometry extends Geometry {
    private final Collection<LineGeometry> rings;

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The lines that define the polygon.
     */
    public PolygonGeometry(Collection<LineGeometry> rings) {
        this(rings, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The lines that define the polygon.
     * @param properties Additional properties of the polygon.
     */
    public PolygonGeometry(Collection<LineGeometry> rings, GeometryProperties properties) {
        super(properties);

        this.rings = rings;
    }

    /**
     * Unmodifiable representation of the {@link LineGeometry geometric lines} representing this polygon.
     *
     * @return An unmodifiable representation of the {@link LineGeometry geometric lines} representing this polygon.
     */
    public Collection<LineGeometry> getRings() {
        return Collections.unmodifiableCollection(rings);
    }
}
