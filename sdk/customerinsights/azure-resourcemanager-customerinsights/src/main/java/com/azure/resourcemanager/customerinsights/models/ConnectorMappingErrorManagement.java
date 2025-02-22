// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The error management.
 */
@Fluent
public final class ConnectorMappingErrorManagement implements JsonSerializable<ConnectorMappingErrorManagement> {
    /*
     * The type of error management to use for the mapping.
     */
    private ErrorManagementTypes errorManagementType;

    /*
     * The error limit allowed while importing data.
     */
    private Integer errorLimit;

    /**
     * Creates an instance of ConnectorMappingErrorManagement class.
     */
    public ConnectorMappingErrorManagement() {
    }

    /**
     * Get the errorManagementType property: The type of error management to use for the mapping.
     * 
     * @return the errorManagementType value.
     */
    public ErrorManagementTypes errorManagementType() {
        return this.errorManagementType;
    }

    /**
     * Set the errorManagementType property: The type of error management to use for the mapping.
     * 
     * @param errorManagementType the errorManagementType value to set.
     * @return the ConnectorMappingErrorManagement object itself.
     */
    public ConnectorMappingErrorManagement withErrorManagementType(ErrorManagementTypes errorManagementType) {
        this.errorManagementType = errorManagementType;
        return this;
    }

    /**
     * Get the errorLimit property: The error limit allowed while importing data.
     * 
     * @return the errorLimit value.
     */
    public Integer errorLimit() {
        return this.errorLimit;
    }

    /**
     * Set the errorLimit property: The error limit allowed while importing data.
     * 
     * @param errorLimit the errorLimit value to set.
     * @return the ConnectorMappingErrorManagement object itself.
     */
    public ConnectorMappingErrorManagement withErrorLimit(Integer errorLimit) {
        this.errorLimit = errorLimit;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (errorManagementType() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property errorManagementType in model ConnectorMappingErrorManagement"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ConnectorMappingErrorManagement.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("errorManagementType",
            this.errorManagementType == null ? null : this.errorManagementType.toString());
        jsonWriter.writeNumberField("errorLimit", this.errorLimit);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ConnectorMappingErrorManagement from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConnectorMappingErrorManagement if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ConnectorMappingErrorManagement.
     */
    public static ConnectorMappingErrorManagement fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ConnectorMappingErrorManagement deserializedConnectorMappingErrorManagement
                = new ConnectorMappingErrorManagement();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("errorManagementType".equals(fieldName)) {
                    deserializedConnectorMappingErrorManagement.errorManagementType
                        = ErrorManagementTypes.fromString(reader.getString());
                } else if ("errorLimit".equals(fieldName)) {
                    deserializedConnectorMappingErrorManagement.errorLimit = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedConnectorMappingErrorManagement;
        });
    }
}
