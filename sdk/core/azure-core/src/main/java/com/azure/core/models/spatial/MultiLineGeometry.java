// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a multi-line geometry.
 */
public final class MultiLineGeometry extends Geometry {
    private final List<LineGeometry> lines;

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     */
    public MultiLineGeometry(List<LineGeometry> lines) {
        this(lines, null, null);
    }

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     * @param boundingBox Bounding box for the multi-line.
     * @param properties Additional properties of the multi-line.
     */
    public MultiLineGeometry(List<LineGeometry> lines, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        this.lines = lines;
    }

    /**
     * Unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     */
    public List<LineGeometry> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
