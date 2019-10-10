// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.implementation.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Fluent
public class CoordinateSystem {
    @JsonProperty
    private String type;

    @JsonProperty
    private Map<String, String> properties;

    public String type() {
        return this.type;
    }

    public CoordinateSystem type(String type) {
        this.type = type;
        return this;
    }

    public Map<String, String> properties() {
        return this.properties;
    }

    public CoordinateSystem properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Creates a new instance for CoordinateSystem, with default values.
     * The EPSG:4326 is the coordination system used for GeographyPoints in Azure Search.
     *
     * @return a new instance of CoordinateSystem
     */
    public static CoordinateSystem create() {
        Map<String, String> props = new HashMap<>();
        props.put("name", "EPSG:4326");
        return new CoordinateSystem().type("name").properties(props);
    }

    /**
     * Ensures that the values are valid as "crs" field in the GeoPoint for the Search Service:
     * \"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}
     *
     * @return true if valid, false if invalid
     */
    public boolean validate() {
        return StringUtils.equals("name", type)
            && properties != null
            && properties.keySet().size() == 1
            && properties.containsKey("name")
            && properties.get("name").startsWith("EPSG");
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
        if (!this.validate() || !other.validate()) {
            return false;
        }
        return Objects.equals(type, other.type)
            && Objects.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties);
    }

}
