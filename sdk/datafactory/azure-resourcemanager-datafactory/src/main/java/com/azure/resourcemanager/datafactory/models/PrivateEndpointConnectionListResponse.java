// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.datafactory.fluent.models.PrivateEndpointConnectionResourceInner;
import java.io.IOException;
import java.util.List;

/**
 * A list of linked service resources.
 */
@Fluent
public final class PrivateEndpointConnectionListResponse
    implements JsonSerializable<PrivateEndpointConnectionListResponse> {
    /*
     * List of Private Endpoint Connections.
     */
    private List<PrivateEndpointConnectionResourceInner> value;

    /*
     * The link to the next page of results, if any remaining results exist.
     */
    private String nextLink;

    /**
     * Creates an instance of PrivateEndpointConnectionListResponse class.
     */
    public PrivateEndpointConnectionListResponse() {
    }

    /**
     * Get the value property: List of Private Endpoint Connections.
     * 
     * @return the value value.
     */
    public List<PrivateEndpointConnectionResourceInner> value() {
        return this.value;
    }

    /**
     * Set the value property: List of Private Endpoint Connections.
     * 
     * @param value the value value to set.
     * @return the PrivateEndpointConnectionListResponse object itself.
     */
    public PrivateEndpointConnectionListResponse withValue(List<PrivateEndpointConnectionResourceInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The link to the next page of results, if any remaining results exist.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link to the next page of results, if any remaining results exist.
     * 
     * @param nextLink the nextLink value to set.
     * @return the PrivateEndpointConnectionListResponse object itself.
     */
    public PrivateEndpointConnectionListResponse withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property value in model PrivateEndpointConnectionListResponse"));
        } else {
            value().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(PrivateEndpointConnectionListResponse.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PrivateEndpointConnectionListResponse from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PrivateEndpointConnectionListResponse if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the PrivateEndpointConnectionListResponse.
     */
    public static PrivateEndpointConnectionListResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PrivateEndpointConnectionListResponse deserializedPrivateEndpointConnectionListResponse
                = new PrivateEndpointConnectionListResponse();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<PrivateEndpointConnectionResourceInner> value
                        = reader.readArray(reader1 -> PrivateEndpointConnectionResourceInner.fromJson(reader1));
                    deserializedPrivateEndpointConnectionListResponse.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedPrivateEndpointConnectionListResponse.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPrivateEndpointConnectionListResponse;
        });
    }
}
