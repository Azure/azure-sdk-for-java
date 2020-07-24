// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract geo object.
 */
@JsonSubTypes({
    @JsonSubTypes.Type(GeoPoint.class),
    @JsonSubTypes.Type(GeoLine.class),
    @JsonSubTypes.Type(GeoPolygon.class),
    @JsonSubTypes.Type(GeoPointCollection.class),
    @JsonSubTypes.Type(GeoLineCollection.class),
    @JsonSubTypes.Type(GeoPolygonCollection.class),
    @JsonSubTypes.Type(GeoCollection.class)
})
public abstract class GeoObject {
    private final GeoBoundingBox boundingBox;
    private final Map<String, Object> properties;

    protected GeoObject(GeoBoundingBox boundingBox, Map<String, Object> properties) {
        this.boundingBox = boundingBox;

        if (properties == null) {
            this.properties = null;
        } else {
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
        }
    }

    /**
     * Bounding box for this {@link GeoObject}.
     *
     * @return The bounding box for this {@link GeoObject}.
     */
    public GeoBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Additional properties about this {@link GeoObject}.
     *
     * @return An unmodifiable representation of the additional properties associated with this {@link GeoObject}.
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
        if (!(obj instanceof GeoObject)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoObject other = (GeoObject) obj;

        return Objects.equals(boundingBox, other.boundingBox) && Objects.equals(properties, other.properties);
    }
}
