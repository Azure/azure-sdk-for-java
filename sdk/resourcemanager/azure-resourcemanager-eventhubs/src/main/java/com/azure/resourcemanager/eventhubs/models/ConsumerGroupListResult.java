// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.eventhubs.fluent.models.ConsumerGroupInner;
import java.io.IOException;
import java.util.List;

/**
 * The result to the List Consumer Group operation.
 */
@Fluent
public final class ConsumerGroupListResult implements JsonSerializable<ConsumerGroupListResult> {
    /*
     * Result of the List Consumer Group operation.
     */
    private List<ConsumerGroupInner> value;

    /*
     * Link to the next set of results. Not empty if Value contains incomplete list of Consumer Group
     */
    private String nextLink;

    /**
     * Creates an instance of ConsumerGroupListResult class.
     */
    public ConsumerGroupListResult() {
    }

    /**
     * Get the value property: Result of the List Consumer Group operation.
     * 
     * @return the value value.
     */
    public List<ConsumerGroupInner> value() {
        return this.value;
    }

    /**
     * Set the value property: Result of the List Consumer Group operation.
     * 
     * @param value the value value to set.
     * @return the ConsumerGroupListResult object itself.
     */
    public ConsumerGroupListResult withValue(List<ConsumerGroupInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: Link to the next set of results. Not empty if Value contains incomplete list of
     * Consumer Group.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: Link to the next set of results. Not empty if Value contains incomplete list of
     * Consumer Group.
     * 
     * @param nextLink the nextLink value to set.
     * @return the ConsumerGroupListResult object itself.
     */
    public ConsumerGroupListResult withNextLink(String nextLink) {
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
     * Reads an instance of ConsumerGroupListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConsumerGroupListResult if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ConsumerGroupListResult.
     */
    public static ConsumerGroupListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ConsumerGroupListResult deserializedConsumerGroupListResult = new ConsumerGroupListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<ConsumerGroupInner> value = reader.readArray(reader1 -> ConsumerGroupInner.fromJson(reader1));
                    deserializedConsumerGroupListResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedConsumerGroupListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedConsumerGroupListResult;
        });
    }
}
