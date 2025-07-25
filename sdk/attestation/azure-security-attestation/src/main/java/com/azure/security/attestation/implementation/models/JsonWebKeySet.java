// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The JsonWebKeySet model.
 */
@Fluent
public final class JsonWebKeySet implements JsonSerializable<JsonWebKeySet> {
    /*
     * The value of the "keys" parameter is an array of JWK values. By
     * default, the order of the JWK values within the array does not imply
     * an order of preference among them, although applications of JWK Sets
     * can choose to assign a meaning to the order for their purposes, if
     * desired.
     */
    @Generated
    private List<JsonWebKey> keys;

    /**
     * Creates an instance of JsonWebKeySet class.
     */
    @Generated
    public JsonWebKeySet() {
    }

    /**
     * Get the keys property: The value of the "keys" parameter is an array of JWK values. By
     * default, the order of the JWK values within the array does not imply
     * an order of preference among them, although applications of JWK Sets
     * can choose to assign a meaning to the order for their purposes, if
     * desired.
     * 
     * @return the keys value.
     */
    @Generated
    public List<JsonWebKey> getKeys() {
        return this.keys;
    }

    /**
     * Set the keys property: The value of the "keys" parameter is an array of JWK values. By
     * default, the order of the JWK values within the array does not imply
     * an order of preference among them, although applications of JWK Sets
     * can choose to assign a meaning to the order for their purposes, if
     * desired.
     * 
     * @param keys the keys value to set.
     * @return the JsonWebKeySet object itself.
     */
    @Generated
    public JsonWebKeySet setKeys(List<JsonWebKey> keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (getKeys() != null) {
            getKeys().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("keys", this.keys, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of JsonWebKeySet from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of JsonWebKeySet if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the JsonWebKeySet.
     */
    @Generated
    public static JsonWebKeySet fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JsonWebKeySet deserializedJsonWebKeySet = new JsonWebKeySet();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("keys".equals(fieldName)) {
                    List<JsonWebKey> keys = reader.readArray(reader1 -> JsonWebKey.fromJson(reader1));
                    deserializedJsonWebKeySet.keys = keys;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedJsonWebKeySet;
        });
    }
}
