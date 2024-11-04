// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The object attributes managed by the Cryptography service.
 */
@Fluent
public final class SecretRequestAttributes implements JsonSerializable<SecretRequestAttributes> {

    /**
     * Creates an instance of SecretRequestAttributes. Reads secretProperties.notBefore, secretProperties.expires and
     * secretProperties.enabled fields from {@code secretProperties}
     * @param secretProperties the {@link SecretProperties} object with populated attributes
     */
    public SecretRequestAttributes(SecretProperties secretProperties) {
        if (secretProperties.getNotBefore() != null) {
            this.notBefore = secretProperties.getNotBefore().toEpochSecond();
        }
        if (secretProperties.getExpiresOn() != null) {
            this.expires = secretProperties.getExpiresOn().toEpochSecond();
        }
        this.enabled = secretProperties.isEnabled();
    }

    private SecretRequestAttributes() {
    }

    /*
     * The secret value.
     */
    private String value;

    /*
     * The secret id.
     */
    private String id;

    /*
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /*
     * Not before date in UTC.
     */
    private Long notBefore;

    /*
     * Expiry date in UTC.
     */
    private Long expires;

    /*
     * Creation time in UTC.
     */
    private Long created;

    /*
     * Last updated time in UTC.
     */
    private Long updated;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes getEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore value.
     *
     * @return the notBefore value
     */
    public OffsetDateTime getNotBefore() {
        if (this.notBefore == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.notBefore * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes setNotBefore(OffsetDateTime notBefore) {
        if (notBefore == null) {
            this.notBefore = null;
        } else {
            this.notBefore = OffsetDateTime.ofInstant(notBefore.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /**
     * Get the expires value.
     *
     * @return the expires value
     */
    public OffsetDateTime getExpires() {
        if (this.expires == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.expires * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the expires value.
     *
     * @param expires the expires value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes setExpires(OffsetDateTime expires) {
        if (expires == null) {
            this.expires = null;
        } else {
            this.expires = OffsetDateTime.ofInstant(expires.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /**
     * Get the created value.
     *
     * @return the created value
     */
    public OffsetDateTime getCreated() {
        if (this.created == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.created * 1000L), ZoneOffset.UTC);
    }

    /**
     * Get the updated value.
     *
     * @return the updated value
     */
    public OffsetDateTime getUpdated() {
        if (this.updated == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("value", value)
            .writeStringField("id", id)
            .writeBooleanField("enabled", enabled)
            .writeNumberField("nbf", notBefore)
            .writeNumberField("exp", expires)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SecretRequestAttributes}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link SecretRequestAttributes} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SecretRequestAttributes} fails to be read from the {@code jsonReader}.
     */
    public static SecretRequestAttributes fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SecretRequestAttributes attributes = new SecretRequestAttributes();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    attributes.value = reader.getString();
                } else if ("id".equals(fieldName)) {
                    attributes.id = reader.getString();
                } else if ("enabled".equals(fieldName)) {
                    attributes.enabled = reader.getNullable(JsonReader::getBoolean);
                } else if ("nbf".equals(fieldName)) {
                    attributes.notBefore = reader.getNullable(JsonReader::getLong);
                } else if ("exp".equals(fieldName)) {
                    attributes.expires = reader.getNullable(JsonReader::getLong);
                } else if ("created".equals(fieldName)) {
                    attributes.created = reader.getNullable(JsonReader::getLong);
                } else if ("updated".equals(fieldName)) {
                    attributes.updated = reader.getNullable(JsonReader::getLong);
                } else {
                    reader.skipChildren();
                }
            }

            return attributes;
        });
    }
}
