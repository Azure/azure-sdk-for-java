// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a set of request options used in REST requests intitiated by Cryptography service.
 */
@Fluent
public final class SecretRequestParameters implements JsonSerializable<SecretRequestParameters> {
    /*
     * The value of the secret.
     */
    private String value;

    /*
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /*
     * Type of the secret value such as a password.
     */
    private String contentType;

    /*
     * The secret management attributes.
     */
    private SecretRequestAttributes secretRequestAttributes;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the secretRequestAttributes value.
     *
     * @return the SecretRequestAttributes value
     */
    public SecretRequestAttributes getSecretAttributes() {
        return this.secretRequestAttributes;
    }

    /**
     * Set the secretRequestAttributes value.
     *
     * @param secretRequestAttributes the secretRequestAttributes to set
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters setSecretAttributes(SecretRequestAttributes secretRequestAttributes) {
        this.secretRequestAttributes = secretRequestAttributes;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("value", value)
            .writeMapField("tags", tags, JsonWriter::writeString)
            .writeStringField("contentType", contentType)
            .writeJsonField("attributes", secretRequestAttributes)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SecretRequestParameters}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link SecretRequestParameters} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SecretRequestParameters} fails to be read from the {@code jsonReader}.
     */
    public static SecretRequestParameters fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SecretRequestParameters attributes = new SecretRequestParameters();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    attributes.value = reader.getString();
                } else if ("tags".equals(fieldName)) {
                    attributes.tags = reader.readMap(JsonReader::getString);
                } else if ("contentType".equals(fieldName)) {
                    attributes.contentType = reader.getString();
                } else if ("attributes".equals(fieldName)) {
                    attributes.secretRequestAttributes = SecretRequestAttributes.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return attributes;
        });
    }
}
