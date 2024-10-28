// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Secret key
 */
public final class SecretKey implements JsonSerializable<SecretKey> {

    /*
     * The value of the secret.
     */
    private String value;

    /*
     * The secret properties.
     */
    private SecretProperties properties;

    /**
     * Creates an empty instance of the Secret.
     */
    public SecretKey() {
        properties = new SecretProperties();
    }

    /**
     * Creates a Secret with {@code name} and {@code value}.
     *
     * @param name The name of the secret.
     * @param value the value of the secret.
     */
    public SecretKey(String name, String value) {
        properties = new SecretProperties(name);
        this.value = value;
    }

    /**
     * Get the value of the secret.
     *
     * @return the secret value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the secret identifier.
     *
     * @return the secret identifier.
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the secret name.
     *
     * @return the secret name.
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the secret properties
     * @return the Secret properties
     */
    public SecretProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the secret properties
     * @param properties The Secret properties
     * @throws NullPointerException if {@code properties} is null.
     * @return the updated secret key object
     */
    public SecretKey setProperties(SecretProperties properties) {
        Objects.requireNonNull(properties);
        properties.name = this.properties.name;
        this.properties = properties;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject().writeStringField("value", this.value);

        if (properties != null) {
            jsonWriter.writeMapField("tags", properties.getTags(), JsonWriter::writeString)
                .writeStringField("contentType", properties.getContentType())
                .writeStartObject("attributes")
                .writeBooleanField("enabled", properties.isEnabled());
            if (properties.getNotBefore() != null) {

                jsonWriter.writeNumberField("nbf", properties.getNotBefore().toEpochSecond());
            }
            if (properties.getExpiresOn() != null) {
                jsonWriter.writeNumberField("exp", properties.getExpiresOn().toEpochSecond());
            }
            jsonWriter.writeEndObject();
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SecretKey from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SecretKey if the JsonReader was pointing to an instance of it, or null if it was
     *     pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SecretKey.
     */
    public static SecretKey fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SecretKey secretKey = new SecretKey();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    secretKey.value = reader.getString();
                } else if ("id".equals(fieldName)) {
                    secretKey.properties.id = reader.getString();
                    KeyVaultKeysUtils.unpackId(secretKey.properties.id, name -> secretKey.properties.name = name,
                        version -> secretKey.properties.version = version);
                } else if ("contentType".equals(fieldName)) {
                    secretKey.properties.contentType = reader.getString();
                } else if ("attributes".equals(fieldName)) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("enabled".equals(fieldName)) {
                            secretKey.properties.enabled = reader.getNullable(JsonReader::getBoolean);
                        } else if ("nbf".equals(fieldName)) {
                            secretKey.properties.notBefore = reader.getNullable(nonNull -> OffsetDateTime
                                .ofInstant(Instant.ofEpochSecond(nonNull.getLong()), ZoneOffset.UTC));
                        } else if ("exp".equals(fieldName)) {
                            secretKey.properties.expiresOn = reader.getNullable(nonNull -> OffsetDateTime
                                .ofInstant(Instant.ofEpochSecond(nonNull.getLong()), ZoneOffset.UTC));
                        } else if ("created".equals(fieldName)) {
                            secretKey.properties.createdOn = reader.getNullable(nonNull -> OffsetDateTime
                                .ofInstant(Instant.ofEpochSecond(nonNull.getLong()), ZoneOffset.UTC));
                        } else if ("updated".equals(fieldName)) {
                            secretKey.properties.updatedOn = reader.getNullable(nonNull -> OffsetDateTime
                                .ofInstant(Instant.ofEpochSecond(nonNull.getLong()), ZoneOffset.UTC));
                        } else if ("recoverableDays".equals(fieldName)) {
                            secretKey.properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                        } else if ("recoveryLevel".equals(fieldName)) {
                            secretKey.properties.recoveryLevel = reader.getString();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("tags".equals(fieldName)) {
                    secretKey.properties.tags = reader.readMap(JsonReader::getString);
                } else if ("kid".equals(fieldName)) {
                    secretKey.properties.keyId = reader.getString();
                } else if ("managed".equals(fieldName)) {
                    secretKey.properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return secretKey;
        });
    }
}
