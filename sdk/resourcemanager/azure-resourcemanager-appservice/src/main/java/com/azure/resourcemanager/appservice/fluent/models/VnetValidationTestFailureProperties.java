// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appservice.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * VnetValidationTestFailure resource specific properties.
 */
@Fluent
public final class VnetValidationTestFailureProperties
    implements JsonSerializable<VnetValidationTestFailureProperties> {
    /*
     * The name of the test that failed.
     */
    private String testName;

    /*
     * The details of what caused the failure, e.g. the blocking rule name, etc.
     */
    private String details;

    /**
     * Creates an instance of VnetValidationTestFailureProperties class.
     */
    public VnetValidationTestFailureProperties() {
    }

    /**
     * Get the testName property: The name of the test that failed.
     * 
     * @return the testName value.
     */
    public String testName() {
        return this.testName;
    }

    /**
     * Set the testName property: The name of the test that failed.
     * 
     * @param testName the testName value to set.
     * @return the VnetValidationTestFailureProperties object itself.
     */
    public VnetValidationTestFailureProperties withTestName(String testName) {
        this.testName = testName;
        return this;
    }

    /**
     * Get the details property: The details of what caused the failure, e.g. the blocking rule name, etc.
     * 
     * @return the details value.
     */
    public String details() {
        return this.details;
    }

    /**
     * Set the details property: The details of what caused the failure, e.g. the blocking rule name, etc.
     * 
     * @param details the details value to set.
     * @return the VnetValidationTestFailureProperties object itself.
     */
    public VnetValidationTestFailureProperties withDetails(String details) {
        this.details = details;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("testName", this.testName);
        jsonWriter.writeStringField("details", this.details);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VnetValidationTestFailureProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of VnetValidationTestFailureProperties if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the VnetValidationTestFailureProperties.
     */
    public static VnetValidationTestFailureProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VnetValidationTestFailureProperties deserializedVnetValidationTestFailureProperties
                = new VnetValidationTestFailureProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("testName".equals(fieldName)) {
                    deserializedVnetValidationTestFailureProperties.testName = reader.getString();
                } else if ("details".equals(fieldName)) {
                    deserializedVnetValidationTestFailureProperties.details = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedVnetValidationTestFailureProperties;
        });
    }
}
