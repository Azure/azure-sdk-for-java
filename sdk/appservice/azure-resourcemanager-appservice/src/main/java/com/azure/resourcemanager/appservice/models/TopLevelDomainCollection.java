// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.appservice.fluent.models.TopLevelDomainInner;
import java.io.IOException;
import java.util.List;

/**
 * Collection of Top-level domains.
 */
@Fluent
public final class TopLevelDomainCollection implements JsonSerializable<TopLevelDomainCollection> {
    /*
     * The TopLevelDomain items on this page
     */
    private List<TopLevelDomainInner> value;

    /*
     * The link to the next page of items
     */
    private String nextLink;

    /**
     * Creates an instance of TopLevelDomainCollection class.
     */
    public TopLevelDomainCollection() {
    }

    /**
     * Get the value property: The TopLevelDomain items on this page.
     * 
     * @return the value value.
     */
    public List<TopLevelDomainInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The TopLevelDomain items on this page.
     * 
     * @param value the value value to set.
     * @return the TopLevelDomainCollection object itself.
     */
    public TopLevelDomainCollection withValue(List<TopLevelDomainInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The link to the next page of items.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link to the next page of items.
     * 
     * @param nextLink the nextLink value to set.
     * @return the TopLevelDomainCollection object itself.
     */
    public TopLevelDomainCollection withNextLink(String nextLink) {
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
                .log(new IllegalArgumentException("Missing required property value in model TopLevelDomainCollection"));
        } else {
            value().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(TopLevelDomainCollection.class);

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
     * Reads an instance of TopLevelDomainCollection from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of TopLevelDomainCollection if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TopLevelDomainCollection.
     */
    public static TopLevelDomainCollection fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TopLevelDomainCollection deserializedTopLevelDomainCollection = new TopLevelDomainCollection();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<TopLevelDomainInner> value
                        = reader.readArray(reader1 -> TopLevelDomainInner.fromJson(reader1));
                    deserializedTopLevelDomainCollection.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedTopLevelDomainCollection.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedTopLevelDomainCollection;
        });
    }
}
