// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.spatial;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a multi-line geometry.
 */
public final class MultiLineGeometry extends Geometry {
    private final Collection<LineGeometry> lines;

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     */
    public MultiLineGeometry(Collection<LineGeometry> lines) {
        this(lines, null);
    }

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     * @param properties Additional properties of the multi-line.
     */
    public MultiLineGeometry(Collection<LineGeometry> lines, GeometryProperties properties) {
        super(properties);

        this.lines = lines;
    }

    /**
     * Unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     */
    public Collection<LineGeometry> getLines() {
        return Collections.unmodifiableCollection(lines);
    }
}
