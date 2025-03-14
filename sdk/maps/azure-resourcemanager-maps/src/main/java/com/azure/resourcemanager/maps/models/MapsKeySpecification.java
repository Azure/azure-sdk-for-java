// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.maps.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Whether the operation refers to the primary or secondary key.
 */
@Fluent
public final class MapsKeySpecification implements JsonSerializable<MapsKeySpecification> {
    /*
     * Whether the operation refers to the primary or secondary key.
     */
    private KeyType keyType;

    /**
     * Creates an instance of MapsKeySpecification class.
     */
    public MapsKeySpecification() {
    }

    /**
     * Get the keyType property: Whether the operation refers to the primary or secondary key.
     * 
     * @return the keyType value.
     */
    public KeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the keyType property: Whether the operation refers to the primary or secondary key.
     * 
     * @param keyType the keyType value to set.
     * @return the MapsKeySpecification object itself.
     */
    public MapsKeySpecification withKeyType(KeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (keyType() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property keyType in model MapsKeySpecification"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(MapsKeySpecification.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("keyType", this.keyType == null ? null : this.keyType.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MapsKeySpecification from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MapsKeySpecification if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the MapsKeySpecification.
     */
    public static MapsKeySpecification fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MapsKeySpecification deserializedMapsKeySpecification = new MapsKeySpecification();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("keyType".equals(fieldName)) {
                    deserializedMapsKeySpecification.keyType = KeyType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedMapsKeySpecification;
        });
    }
}
