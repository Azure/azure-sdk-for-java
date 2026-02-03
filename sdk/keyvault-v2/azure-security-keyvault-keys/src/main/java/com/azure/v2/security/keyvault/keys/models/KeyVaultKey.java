// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeyHelper;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeysUtils;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * Key is the resource consisting of name, {@link JsonWebKey} and its attributes specified in {@link KeyProperties}. It
 * is managed by Keys service.
 *
 * @see KeyClient
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public class KeyVaultKey implements JsonSerializable<KeyVaultKey> {
    static {
        KeyVaultKeyHelper.setAccessor(KeyVaultKey::new);
    }

    /**
     * The Json Web Key.
     */
    private final JsonWebKey key;

    /**
     * The key properties.
     */
    final KeyProperties properties;

    /**
     * Creates an instance of {@link KeyVaultKey}.
     */
    public KeyVaultKey() {
        this.key = null;
        this.properties = new KeyProperties();
    }

    /**
     * Creates an instance of {@link KeyVaultKey}.
     *
     * @param jsonWebKey The {@link JsonWebKey} to be used for crypto operations.
     */
    KeyVaultKey(JsonWebKey jsonWebKey) {
        this.key = jsonWebKey;
        this.properties = new KeyProperties();
    }

    KeyVaultKey(JsonWebKey jsonWebKey, KeyProperties properties) {
        this.key = jsonWebKey;
        this.properties = properties;
    }

    /**
     * Get the JSON Web Key.
     *
     * @return The JSON Web Key.
     */
    public JsonWebKey getKey() {
        return this.key;
    }

    /**
     * Get the key properties.
     *
     * @return The key properties.
     */
    public KeyProperties getProperties() {
        return this.properties;
    }

    /**
     * Get the key identifier.
     *
     * @return The key identifier.
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the key name.
     *
     * @return The key name.
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the key type.
     *
     * @return The key type.
     */
    public KeyType getKeyType() {
        return key.getKeyType();
    }

    /**
     * Get the key operations.
     *
     * @return The key operations.
     */
    public List<KeyOperation> getKeyOperations() {
        return key.getKeyOps();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeJsonField("key", key).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyVaultKey}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link KeyVaultKey} that the JSON stream represented, may return null.
     * @throws IOException If a {@link KeyVaultKey} fails to be read from the {@code jsonReader}.
     */
    public static KeyVaultKey fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JsonWebKey webKey = null;
            KeyProperties properties = new KeyProperties();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("key".equals(fieldName)) {
                    webKey = JsonWebKey.fromJson(reader);
                    KeyVaultKeysUtils.unpackId(webKey.getId(), name -> properties.name = name,
                        version -> properties.version = version);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("enabled".equals(fieldName)) {
                            properties.enabled = reader.getNullable(JsonReader::getBoolean);
                        } else if ("exportable".equals(fieldName)) {
                            properties.exportable = reader.getNullable(JsonReader::getBoolean);
                        } else if ("nbf".equals(fieldName)) {
                            properties.notBefore = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("exp".equals(fieldName)) {
                            properties.expiresOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("created".equals(fieldName)) {
                            properties.createdOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("updated".equals(fieldName)) {
                            properties.updatedOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("recoveryLevel".equals(fieldName)) {
                            properties.recoveryLevel = reader.getString();
                        } else if ("recoverableDays".equals(fieldName)) {
                            properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("tags".equals(fieldName)) {
                    properties.setTags(reader.readMap(JsonReader::getString));
                } else if ("managed".equals(fieldName)) {
                    properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("release_policy".equals(fieldName)) {
                    properties.setReleasePolicy(KeyReleasePolicy.fromJson(reader));
                } else {
                    reader.skipChildren();
                }
            }

            return new KeyVaultKey(webKey, properties);
        });
    }
}
