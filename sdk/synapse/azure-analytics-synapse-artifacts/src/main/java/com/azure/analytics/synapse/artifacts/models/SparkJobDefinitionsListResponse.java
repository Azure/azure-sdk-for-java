// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.synapse.artifacts.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * A list of spark job definitions resources.
 */
@Fluent
public final class SparkJobDefinitionsListResponse implements JsonSerializable<SparkJobDefinitionsListResponse> {
    /*
     * List of spark job definitions.
     */
    @Generated
    private List<SparkJobDefinitionResource> value;

    /*
     * The link to the next page of results, if any remaining results exist.
     */
    @Generated
    private String nextLink;

    /**
     * Creates an instance of SparkJobDefinitionsListResponse class.
     */
    @Generated
    public SparkJobDefinitionsListResponse() {
    }

    /**
     * Get the value property: List of spark job definitions.
     * 
     * @return the value value.
     */
    @Generated
    public List<SparkJobDefinitionResource> getValue() {
        return this.value;
    }

    /**
     * Set the value property: List of spark job definitions.
     * 
     * @param value the value value to set.
     * @return the SparkJobDefinitionsListResponse object itself.
     */
    @Generated
    public SparkJobDefinitionsListResponse setValue(List<SparkJobDefinitionResource> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The link to the next page of results, if any remaining results exist.
     * 
     * @return the nextLink value.
     */
    @Generated
    public String getNextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link to the next page of results, if any remaining results exist.
     * 
     * @param nextLink the nextLink value to set.
     * @return the SparkJobDefinitionsListResponse object itself.
     */
    @Generated
    public SparkJobDefinitionsListResponse setNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SparkJobDefinitionsListResponse from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SparkJobDefinitionsListResponse if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SparkJobDefinitionsListResponse.
     */
    @Generated
    public static SparkJobDefinitionsListResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SparkJobDefinitionsListResponse deserializedSparkJobDefinitionsListResponse
                = new SparkJobDefinitionsListResponse();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<SparkJobDefinitionResource> value
                        = reader.readArray(reader1 -> SparkJobDefinitionResource.fromJson(reader1));
                    deserializedSparkJobDefinitionsListResponse.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedSparkJobDefinitionsListResponse.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSparkJobDefinitionsListResponse;
        });
    }
}
