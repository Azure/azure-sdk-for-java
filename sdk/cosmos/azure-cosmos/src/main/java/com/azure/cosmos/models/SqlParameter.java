// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents a SQL parameter in the SqlQuerySpec used for queries in the Azure Cosmos DB database service.
 */
public final class SqlParameter {
    private JsonSerializable jsonSerializable;

    /**
     * Initializes a new instance of the SqlParameter class.
     */
    public SqlParameter() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Initializes a new instance of the SqlParameter class.
     *
     * @param objectNode the object node that represents the included path.
     */
    SqlParameter(ObjectNode objectNode) {

        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Initializes a new instance of the SqlParameter class with the name and value of the parameter.
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     */
    public SqlParameter(String name, Object value) {
        this.jsonSerializable = new JsonSerializable();
        this.setName(name);
        this.setValue(value);
    }

    /**
     * Gets the name of the parameter.
     *
     * @return the name of the parameter.
     */
    public String getName() {
        return this.jsonSerializable.getString("name");
    }

    /**
     * Sets the name of the parameter.
     *
     * @param name the name of the parameter.
     * @return the SqlParameter.
     */
    public SqlParameter setName(String name) {
        this.jsonSerializable.set("name", name);
        return this;
    }

    /**
     * Gets the value of the parameter.
     *
     * @param classType the class of the parameter value.
     * @param <T> the type of the parameter
     * @return the value of the parameter.
     */
    public <T> T getValue(Class<T> classType) {
        return this.jsonSerializable.getObject("value", classType);
    }

    /**
     * Sets the value of the parameter.
     *
     * @param value the value of the parameter.
     * @return the SqlParameter.
     */
    public SqlParameter setValue(Object value) {
        this.jsonSerializable.set("value", value);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SqlParameter that = (SqlParameter) o;
        return Objects.equals(jsonSerializable, that.jsonSerializable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonSerializable);
    }
}
