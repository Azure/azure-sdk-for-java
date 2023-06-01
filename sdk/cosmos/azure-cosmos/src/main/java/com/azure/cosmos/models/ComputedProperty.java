// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a computed property definition for a Cosmos DB container.
 */
public final class ComputedProperty {
    @JsonProperty("name")
    private String name;

    @JsonProperty("query")
    private String query;

    private JsonSerializable jsonSerializable;

    /**
     * Instantiates a new Computed properties.
     */
    ComputedProperty() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Instantiates a new Computed properties with name and query.
     * @param name the name of the computed property.
     * @param query the query used to evaluate the value for the computed property.
     */
    public ComputedProperty(String name, String query) {
        this.name = name;
        this.query = query;
    }

    /**
     * Gets the name of the computed property.
     *
     * @return the name of the computed property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the query used to evaluate the value for the computed property.
     *
     * @return the query for the computed property.
     */
    public String getQuery() {
        return query;
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }
}
