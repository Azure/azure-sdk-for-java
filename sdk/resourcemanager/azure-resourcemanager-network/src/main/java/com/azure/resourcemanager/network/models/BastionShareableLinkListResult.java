// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.network.fluent.models.BastionShareableLinkInner;
import java.io.IOException;
import java.util.List;

/**
 * Response for all the Bastion Shareable Link endpoints.
 */
@Fluent
public final class BastionShareableLinkListResult implements JsonSerializable<BastionShareableLinkListResult> {
    /*
     * List of Bastion Shareable Links for the request.
     */
    private List<BastionShareableLinkInner> value;

    /*
     * The URL to get the next set of results.
     */
    private String nextLink;

    /**
     * Creates an instance of BastionShareableLinkListResult class.
     */
    public BastionShareableLinkListResult() {
    }

    /**
     * Get the value property: List of Bastion Shareable Links for the request.
     * 
     * @return the value value.
     */
    public List<BastionShareableLinkInner> value() {
        return this.value;
    }

    /**
     * Set the value property: List of Bastion Shareable Links for the request.
     * 
     * @param value the value value to set.
     * @return the BastionShareableLinkListResult object itself.
     */
    public BastionShareableLinkListResult withValue(List<BastionShareableLinkInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The URL to get the next set of results.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The URL to get the next set of results.
     * 
     * @param nextLink the nextLink value to set.
     * @return the BastionShareableLinkListResult object itself.
     */
    public BastionShareableLinkListResult withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }

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
     * Reads an instance of BastionShareableLinkListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of BastionShareableLinkListResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BastionShareableLinkListResult.
     */
    public static BastionShareableLinkListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BastionShareableLinkListResult deserializedBastionShareableLinkListResult
                = new BastionShareableLinkListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<BastionShareableLinkInner> value
                        = reader.readArray(reader1 -> BastionShareableLinkInner.fromJson(reader1));
                    deserializedBastionShareableLinkListResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedBastionShareableLinkListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedBastionShareableLinkListResult;
        });
    }
}
