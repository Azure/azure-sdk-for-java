// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * A complex configuration object that has multiple properties stored in Azure App Configuration service.
 */
public class ComplexConfiguration implements JsonSerializable<ComplexConfiguration> {
    private String endpointUri;
    private String name;
    private int numberOfInstances;

    /**
     * Gets the endpoint URI.
     *
     * @return The endpoint URI.
     */
    public String endpointUri() {
        return endpointUri;
    }

    /**
     * Sets the endpoint URI.
     *
     * @param endpointUri The endpoint URI for this ComplexConfiguration.
     * @return The updated object.
     */
    public ComplexConfiguration endpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }

    /**
     * Gets the name of the ComplexConfiguration.
     *
     * @return The name of the complex object.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name of the ComplexConfiguration.
     *
     * @param name The name to set for this ComplexConfiguration.
     * @return The updated object.
     */
    public ComplexConfiguration name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the number of instances.
     *
     * @return The number of instances.
     */
    public int numberOfInstances() {
        return numberOfInstances;
    }

    /**
     * Sets the number of instances.
     *
     * @param numberOfInstances The number of instances for this ComplexConfiguration.
     * @return The updated object.
     */
    public ComplexConfiguration numberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Name: " + name() + ", Endpoint: " + endpointUri() + ", # of instances: " + numberOfInstances();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("endpointUri", endpointUri);
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeIntField("numberOfInstances", numberOfInstances);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ComplexConfiguration from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ComplexConfiguration if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ComplexConfiguration.
     */
    public static ComplexConfiguration fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ComplexConfiguration deserializedComplexConfiguration = new ComplexConfiguration();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("endpointUri".equals(fieldName)) {
                    deserializedComplexConfiguration.endpointUri(reader.getString());
                } else if ("name".equals(fieldName)) {
                    deserializedComplexConfiguration.name(reader.getString());
                } else if ("numberOfInstances".equals(fieldName)) {
                    deserializedComplexConfiguration.numberOfInstances(reader.getInt());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedComplexConfiguration;
        });
    }
}
