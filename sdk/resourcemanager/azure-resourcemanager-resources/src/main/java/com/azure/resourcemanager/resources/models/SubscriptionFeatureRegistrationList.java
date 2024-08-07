// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.resources.fluent.models.SubscriptionFeatureRegistrationInner;
import java.io.IOException;
import java.util.List;

/**
 * The list of subscription feature registrations.
 */
@Fluent
public final class SubscriptionFeatureRegistrationList
    implements JsonSerializable<SubscriptionFeatureRegistrationList> {
    /*
     * The link used to get the next page of subscription feature registrations list.
     */
    private String nextLink;

    /*
     * The list of subscription feature registrations.
     */
    private List<SubscriptionFeatureRegistrationInner> value;

    /**
     * Creates an instance of SubscriptionFeatureRegistrationList class.
     */
    public SubscriptionFeatureRegistrationList() {
    }

    /**
     * Get the nextLink property: The link used to get the next page of subscription feature registrations list.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link used to get the next page of subscription feature registrations list.
     * 
     * @param nextLink the nextLink value to set.
     * @return the SubscriptionFeatureRegistrationList object itself.
     */
    public SubscriptionFeatureRegistrationList withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Get the value property: The list of subscription feature registrations.
     * 
     * @return the value value.
     */
    public List<SubscriptionFeatureRegistrationInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The list of subscription feature registrations.
     * 
     * @param value the value value to set.
     * @return the SubscriptionFeatureRegistrationList object itself.
     */
    public SubscriptionFeatureRegistrationList withValue(List<SubscriptionFeatureRegistrationInner> value) {
        this.value = value;
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
        jsonWriter.writeStringField("nextLink", this.nextLink);
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SubscriptionFeatureRegistrationList from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SubscriptionFeatureRegistrationList if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SubscriptionFeatureRegistrationList.
     */
    public static SubscriptionFeatureRegistrationList fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SubscriptionFeatureRegistrationList deserializedSubscriptionFeatureRegistrationList
                = new SubscriptionFeatureRegistrationList();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("nextLink".equals(fieldName)) {
                    deserializedSubscriptionFeatureRegistrationList.nextLink = reader.getString();
                } else if ("value".equals(fieldName)) {
                    List<SubscriptionFeatureRegistrationInner> value
                        = reader.readArray(reader1 -> SubscriptionFeatureRegistrationInner.fromJson(reader1));
                    deserializedSubscriptionFeatureRegistrationList.value = value;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSubscriptionFeatureRegistrationList;
        });
    }
}
