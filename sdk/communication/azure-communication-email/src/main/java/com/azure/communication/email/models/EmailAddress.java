// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.communication.email.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * An object representing the email address and its display name.
 */
@Fluent
public final class EmailAddress implements JsonSerializable<EmailAddress> {
    /*
     * Email address.
     */
    private final String address;

    /*
     * Email display name.
     */
    private String displayName;

    /**
     * Creates an instance of EmailAddress class.
     * 
     * @param address the address value to set.
     */
    public EmailAddress(String address) {
        this.address = address;
    }

    /**
     * Get the address property: Email address.
     * 
     * @return the address value.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Get the displayName property: Email display name.
     * 
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: Email display name.
     * 
     * @param displayName the displayName value to set.
     * @return the EmailAddress object itself.
     */
    public EmailAddress setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("address", this.address);
        jsonWriter.writeStringField("displayName", this.displayName);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EmailAddress from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of EmailAddress if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the EmailAddress.
     */
    public static EmailAddress fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            boolean addressFound = false;
            String address = null;
            String displayName = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("address".equals(fieldName)) {
                    address = reader.getString();
                    addressFound = true;
                } else if ("displayName".equals(fieldName)) {
                    displayName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            if (addressFound) {
                EmailAddress deserializedEmailAddress = new EmailAddress(address);
                deserializedEmailAddress.displayName = displayName;

                return deserializedEmailAddress;
            }
            throw new IllegalStateException("Missing required property: address");
        });
    }
}
