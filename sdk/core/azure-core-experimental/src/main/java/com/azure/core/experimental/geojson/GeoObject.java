// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

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
    private final Map<String, Object> customProperties;

    protected GeoObject(GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        this.boundingBox = boundingBox;

        if (customProperties == null) {
            this.customProperties = null;
        } else {
            this.customProperties = Collections.unmodifiableMap(new HashMap<>(customProperties));
        }
    }

    /**
     * Gets the GeoJSON type for this object.
     *
     * @return The GeoJSON type for this object.
     */
    public abstract GeoObjectType getType();

    /**
     * Bounding box for this {@link GeoObject}.
     *
     * @return The bounding box for this {@link GeoObject}.
     */
    public final GeoBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Gets a custom property with the given name.
     * <p>
     * If the property doesn't exist null will be returned.
     *
     * @param name Name of the custom property.
     * @return The custom property value.
     */
    public final Object getCustomProperty(String name) { // Change this to Option<Object> once it is available.
        return (customProperties == null)
            ? null
            : customProperties.get(name);
    }

    /**
     * Additional properties about this {@link GeoObject}.
     *
     * @return An unmodifiable representation of the additional properties associated with this {@link GeoObject}.
     */
    final Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, customProperties);
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

        return Objects.equals(boundingBox, other.boundingBox) && Objects.equals(
            customProperties, other.customProperties);
    }
}
