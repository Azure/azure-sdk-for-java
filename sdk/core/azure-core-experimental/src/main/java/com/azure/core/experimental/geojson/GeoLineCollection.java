// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a collection of {@link GeoLine GeoLines}.
 */
public final class GeoLineCollection extends GeoObject {
    private final List<GeoLine> lines;

    /**
     * Constructs a {@link GeoLineCollection}.
     *
     * @param lines The geometric lines that define the multi-line.
     * @throws NullPointerException If {@code lines} is {@code null}.
     */
    public GeoLineCollection(List<GeoLine> lines) {
        this(lines, null, null);
    }

    /**
     * Constructs a {@link GeoLineCollection}.
     *
     * @param lines The geometric lines that define the multi-line.
     * @param boundingBox Bounding box for the multi-line.
     * @param customProperties Additional properties of the multi-line.
     * @throws NullPointerException If {@code lines} is {@code null}.
     */
    public GeoLineCollection(List<GeoLine> lines, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(lines, "'lines' cannot be null.");

        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /**
     * Unmodifiable representation of the {@link GeoLine geometric lines} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link GeoLine geometric lines} representing this multi-line.
     */
    public List<GeoLine> getLines() {
        return lines;
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this
     * multi-line.
     */
    public GeoArray<GeoArray<GeoPosition>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.MULTI_LINE_STRING;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoLineCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoLineCollection other = (GeoLineCollection) obj;

        return super.equals(obj) && Objects.equals(lines, other.lines);
    }
}
