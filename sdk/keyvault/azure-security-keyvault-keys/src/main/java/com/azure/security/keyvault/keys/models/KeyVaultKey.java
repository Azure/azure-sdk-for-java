// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeyHelper;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils;

import java.io.IOException;
import java.util.List;

/**
 * Key is the resource consisting of name, {@link JsonWebKey} and its attributes specified in {@link KeyProperties}.
 * It is managed by Key Service.
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
@Fluent
public class KeyVaultKey implements JsonSerializable<KeyVaultKey> {
    static {
        KeyVaultKeyHelper.setAccessor(new KeyVaultKeyHelper.KeyVaultKeyAccessor() {
            @Override
            public KeyVaultKey createKeyVaultKey() {
                return new KeyVaultKey();
            }

            @Override
            public void setKey(KeyVaultKey keyVaultKey, JsonWebKey jsonWebKey) {
                keyVaultKey.key = jsonWebKey;
            }
        });
    }

    /**
     * The Json Web Key.
     */
    private JsonWebKey key;

    /**
     * The key properties.
     */
    final KeyProperties properties;

    KeyVaultKey() {
        this.properties = new KeyProperties();
    }

    /**
     * Creates an instance of {@link KeyVaultKey}.
     *
     * @param keyProperties The {@link KeyProperties}.
     * @param jsonWebKey The {@link JsonWebKey} to be used for crypto operations.
     */
    KeyVaultKey(KeyProperties keyProperties, JsonWebKey jsonWebKey) {
        this.properties = keyProperties;
        this.key = jsonWebKey;
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
        return jsonWriter.writeStartObject()
            .writeJsonField("key", key)
            .writeEndObject();
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
            KeyVaultKey key = new KeyVaultKey();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("key".equals(fieldName)) {
                    key.key = JsonWebKey.fromJson(reader);
                    KeyVaultKeysUtils.unpackId(key.key.getId(), name -> key.properties.name = name,
                        version -> key.properties.version = version);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("enabled".equals(fieldName)) {
                            key.properties.enabled = reader.getNullable(JsonReader::getBoolean);
                        } else if ("exportable".equals(fieldName)) {
                            key.properties.exportable = reader.getNullable(JsonReader::getBoolean);
                        } else if ("nbf".equals(fieldName)) {
                            key.properties.notBefore = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("exp".equals(fieldName)) {
                            key.properties.expiresOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("created".equals(fieldName)) {
                            key.properties.createdOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("updated".equals(fieldName)) {
                            key.properties.updatedOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("recoveryLevel".equals(fieldName)) {
                            key.properties.recoveryLevel = reader.getString();
                        } else if ("recoverableDays".equals(fieldName)) {
                            key.properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("tags".equals(fieldName)) {
                    key.properties.setTags(reader.readMap(JsonReader::getString));
                } else if ("managed".equals(fieldName)) {
                    key.properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("release_policy".equals(fieldName)) {
                    key.properties.setReleasePolicy(KeyReleasePolicy.fromJson(reader));
                } else {
                    reader.skipChildren();
                }
            }

            return key;
        });
    }
}
