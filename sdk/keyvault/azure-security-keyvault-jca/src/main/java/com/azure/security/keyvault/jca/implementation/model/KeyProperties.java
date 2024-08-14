// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * The KeyProperties REST model.
 */
public class KeyProperties implements JsonSerializable<KeyProperties> {
    /**
     * Stores if the key is exportable.
     */
    private boolean exportable;

    private String kty;

    /**
     * Get key type
     *
     * @return The key type.
     */
    public String getKty() {
        return kty;
    }

    /**
     * Set key type.
     *
     * @param kty The key type.
     */
    public void setKty(String kty) {
        this.kty = kty;
    }

    /**
     * Indicates whether the key is exportable or not.
     *
     * @return A value indicating whether the key is exportable or not.
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Set a value indicating whether the key is exportable or not.
     *
     * @param exportable A value indicating whether the key is exportable or not.
     */
    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("exportable", this.exportable);
        jsonWriter.writeStringField("kty", Objects.toString(this.kty, null));

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link KeyProperties} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link KeyProperties} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link KeyProperties}.
     */
    public static KeyProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            KeyProperties deserializedKeyProperties = new KeyProperties();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("exportable".equals(fieldName)) {
                    deserializedKeyProperties.exportable = reader.getNullable(JsonReader::getBoolean);
                } else if ("kty".equals(fieldName)) {
                    deserializedKeyProperties.kty = reader.getNullable(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedKeyProperties;
        });
    }
}
