// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents a SQL parameter in the SqlQuerySpec used for queries in the Azure Cosmos DB database service.
 */
public final class SqlParameter {
    private JsonSerializable jsonSerializable;
    private Object rawValue;

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
        // Note: rawValue here may not preserve the original Java type (e.g. Instant)
        // since it is read back from the JSON property bag after deserialization.
        this.rawValue = this.jsonSerializable.getObject("value", Object.class);
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
        this.jsonSerializable.set(
            "name",
            name
        );
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
        this.rawValue = value;
        this.jsonSerializable.set(
            "value",
            value
        );
        return this;
    }

    /**
     * Returns the raw value captured before serialization, when available.
     * Package-private — used internally to clone parameters while preserving the
     * original Java type (for example, {@code Instant}) when that value originated
     * from direct Java assignment such as {@link #setValue(Object)}.
     * <p>
     * Values populated from the JSON property bag may already have lost their
     * original Java type fidelity during JSON serialization/deserialization.
     */
    Object getRawValue() {
        return this.rawValue;
    }

    /**
     * Re-serializes the parameter value using the given custom item serializer.
     * This is called internally during query execution to ensure parameter values
     * are serialized consistently with document values when a custom serializer is configured.
     *
     * @param serializer the custom item serializer to apply.
     */
    void applySerializer(CosmosItemSerializer serializer) {
        if (this.rawValue != null
            && serializer != null
            && cosmosItemSerializerAccessor().canSerialize(serializer)) {

            this.jsonSerializable.set("value", this.rawValue, serializer, true);
        }
    }

    /**
     * Creates a copy of this SqlParameter, preserving the original raw value
     * so that custom serializers can be applied to the clone independently.
     *
     * @return a new SqlParameter with the same name and raw value.
     */
    private SqlParameter createCopy() {
        return new SqlParameter(this.getName(), this.rawValue);
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    private static CosmosItemSerializerAccessor cosmosItemSerializerAccessor() {
        return ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor();
    }

    static void initialize() {
        ImplementationBridgeHelpers.SqlParameterHelper.setSqlParameterAccessor(
            SqlParameter::createCopy
        );
    }

    static { initialize(); }

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
