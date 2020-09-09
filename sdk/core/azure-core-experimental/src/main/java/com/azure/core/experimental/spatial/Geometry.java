// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract representation of a geometry.
 */
@JsonSubTypes({
    @JsonSubTypes.Type(PointGeometry.class),
    @JsonSubTypes.Type(LineGeometry.class),
    @JsonSubTypes.Type(PolygonGeometry.class),
    @JsonSubTypes.Type(MultiPointGeometry.class),
    @JsonSubTypes.Type(MultiLineGeometry.class),
    @JsonSubTypes.Type(MultiPolygonGeometry.class),
    @JsonSubTypes.Type(CollectionGeometry.class)
})
public abstract class Geometry {
    private final GeometryBoundingBox boundingBox;
    private final Map<String, Object> properties;

    protected Geometry(GeometryBoundingBox boundingBox, Map<String, Object> properties) {
        this.boundingBox = boundingBox;

        if (properties == null) {
            this.properties = null;
        } else {
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
        }
    }

    /**
     * Bounding box for this geometry.
     *
     * @return The bounding box for this geometry.
     */
    public GeometryBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Additional properties about this geometry.
     *
     * @return An unmodifiable representation of the additional properties associated with this geometry.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Geometry)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Geometry other = (Geometry) obj;

        return Objects.equals(boundingBox, other.boundingBox) && Objects.equals(properties, other.properties);
    }
}
