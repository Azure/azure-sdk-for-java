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
import com.azure.resourcemanager.datafactory.fluent.models.DataFlowResourceInner;
import java.io.IOException;
import java.util.List;

/**
 * A list of data flow resources.
 */
@Fluent
public final class DataFlowListResponse implements JsonSerializable<DataFlowListResponse> {
    /*
     * List of data flows.
     */
    private List<DataFlowResourceInner> value;

    /*
     * The link to the next page of results, if any remaining results exist.
     */
    private String nextLink;

    /**
     * Creates an instance of DataFlowListResponse class.
     */
    public DataFlowListResponse() {
    }

    /**
     * Get the value property: List of data flows.
     * 
     * @return the value value.
     */
    public List<DataFlowResourceInner> value() {
        return this.value;
    }

    /**
     * Set the value property: List of data flows.
     * 
     * @param value the value value to set.
     * @return the DataFlowListResponse object itself.
     */
    public DataFlowListResponse withValue(List<DataFlowResourceInner> value) {
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
     * @return the DataFlowListResponse object itself.
     */
    public DataFlowListResponse withNextLink(String nextLink) {
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
                .log(new IllegalArgumentException("Missing required property value in model DataFlowListResponse"));
        } else {
            value().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(DataFlowListResponse.class);

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
     * Reads an instance of DataFlowListResponse from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DataFlowListResponse if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DataFlowListResponse.
     */
    public static DataFlowListResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DataFlowListResponse deserializedDataFlowListResponse = new DataFlowListResponse();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<DataFlowResourceInner> value
                        = reader.readArray(reader1 -> DataFlowResourceInner.fromJson(reader1));
                    deserializedDataFlowListResponse.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedDataFlowListResponse.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDataFlowListResponse;
        });
    }
}
