// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The SecretBundle REST model.
 */
public class SecretBundle implements JsonSerializable<SecretBundle> {

    /**
     * Stores the content type.
     */
    private String contentType;

    /**
     * Stores the value.
     */
    private String value;

    /**
     * Get the content type.
     *
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the content type.
     *
     * @param contentType the content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the value.
     *
     * @param value the value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("contentType", this.contentType);
        jsonWriter.writeStringField("value", this.value);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link SecretBundle} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link SecretBundle} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link SecretBundle}.
     */
    public static SecretBundle fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SecretBundle deserializedSecretBundle = new SecretBundle();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("contentType".equals(fieldName)) {
                    deserializedSecretBundle.contentType = reader.getString();
                } else if ("value".equals(fieldName)) {
                    deserializedSecretBundle.value = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSecretBundle;
        });
    }
}
