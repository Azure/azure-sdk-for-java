// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.List;

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
        this(lines, null);
    }

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     * @param properties Additional properties of the multi-line.
     */
    public MultiLineGeometry(List<LineGeometry> lines, GeometryProperties properties) {
        super(properties);

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
