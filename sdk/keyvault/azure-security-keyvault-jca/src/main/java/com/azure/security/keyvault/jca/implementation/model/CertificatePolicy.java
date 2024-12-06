// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The CertificatePolicy REST model.
 */
public class CertificatePolicy implements JsonSerializable<CertificatePolicy> {
    /**
     * Stores the key properties.
     */
    private KeyProperties keyProperties;

    /**
     * Get the key properties.
     *
     * @return the key properties.
     */
    public KeyProperties getKeyProperties() {
        return keyProperties;
    }

    /**
     * Set the key properties.
     *
     * @param keyProperties the key properties.
     */
    public void setKeyProperties(KeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("key_props", this.keyProperties);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link CertificatePolicy} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link CertificatePolicy} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link CertificatePolicy}.
     */
    public static CertificatePolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CertificatePolicy deserializedCertificatePolicy = new CertificatePolicy();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("key_props".equals(fieldName)) {
                    deserializedCertificatePolicy.keyProperties = KeyProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCertificatePolicy;
        });
    }
}
