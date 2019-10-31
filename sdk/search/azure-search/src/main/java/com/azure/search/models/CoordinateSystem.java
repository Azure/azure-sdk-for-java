// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Fluent
public class CoordinateSystem {
    public static final String NAME_PROPERTY = "name";

    @JsonProperty
    private String type;

    @JsonProperty
    private Map<String, String> properties;

    /**
     * Creates a new instance for CoordinateSystem, with default values.
     * The EPSG:4326 is the coordination system used for GeographyPoints in Azure Search.
     *
     * @return a new instance of CoordinateSystem
     */
    public static CoordinateSystem create() {
        Map<String, String> props = new HashMap<>();
        props.put(NAME_PROPERTY, "EPSG:4326");
        return new CoordinateSystem()
            .setType(NAME_PROPERTY)
            .setProperties(props);
    }

    /**
     * Ensures that the values are valid as "crs" field in the GeoPoint for the Search Service:
     * \"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}
     *
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        return StringUtils.equals(NAME_PROPERTY, type)
            && properties != null
            && properties.keySet().size() == 1
            && properties.containsKey(NAME_PROPERTY)
            && properties.get(NAME_PROPERTY).startsWith("EPSG");
    }

    @Override
    public String toString() {
        if (isValid()) {
            return String.format("CRS%s", properties == null ? "" : properties.get(NAME_PROPERTY));
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoordinateSystem other = (CoordinateSystem) o;
        if (!this.isValid() || !other.isValid()) {
            return false;
        }
        return Objects.equals(type, other.type)
            && Objects.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties);
    }

    public String getType() {
        return type;
    }

    public CoordinateSystem setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public CoordinateSystem setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}
