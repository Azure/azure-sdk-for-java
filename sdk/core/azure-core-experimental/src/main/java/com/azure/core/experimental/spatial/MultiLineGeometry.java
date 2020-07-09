// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a multi-line geometry.
 */
public final class MultiLineGeometry extends Geometry {
    private final List<LineGeometry> lines;

    /**
     * Constructs a multi-line geometry.
     *
     * @param lines The geometric lines that define the multi-line.
     * @throws NullPointerException If {@code lines} is {@code null}.
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
     * @throws NullPointerException If {@code lines} is {@code null}.
     */
    public MultiLineGeometry(List<LineGeometry> lines, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(lines, "'lines' cannot be null.");
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /**
     * Unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link LineGeometry geometric lines} representing this multi-line.
     */
    public List<LineGeometry> getLines() {
        return lines;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MultiLineGeometry)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        MultiLineGeometry other = (MultiLineGeometry) obj;

        return super.equals(obj) && Objects.equals(lines, other.lines);
    }
}
