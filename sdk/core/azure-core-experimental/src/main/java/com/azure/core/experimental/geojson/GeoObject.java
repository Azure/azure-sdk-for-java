// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract geo object.
 */
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Point", value = GeoPoint.class),
    @JsonSubTypes.Type(name = "LineString", value = GeoLineString.class),
    @JsonSubTypes.Type(name = "Polygon", value = GeoPolygon.class),
    @JsonSubTypes.Type(name = "MultiPoint", value = GeoPointCollection.class),
    @JsonSubTypes.Type(name = "MultiLineString", value = GeoLineStringCollection.class),
    @JsonSubTypes.Type(name = "MultiPolygon", value = GeoPolygonCollection.class),
    @JsonSubTypes.Type(name = "GeometryCollection", value = GeoCollection.class)
})
@JsonDeserialize(using = GeoJsonDeserializer.class)
@JsonSerialize(using = GeoJsonSerializer.class)
@Immutable
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
    @JsonProperty("type")
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
