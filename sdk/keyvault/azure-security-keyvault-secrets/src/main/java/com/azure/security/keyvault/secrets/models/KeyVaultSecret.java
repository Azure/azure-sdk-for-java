// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils;

import java.io.IOException;
import java.util.Objects;

/**
 *  Secret is the resource consisting of name, value and its attributes specified in {@link SecretProperties}.
 *  It is managed by Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
@Fluent
public class KeyVaultSecret implements JsonSerializable<KeyVaultSecret> {

    /**
     * The value of the secret.
     */
    String value;

    /**
     * The secret properties.
     */
    SecretProperties properties;

    /**
     * Creates an empty instance of KeyVaultSecret. This constructor is used by the deserializer.
     */
    KeyVaultSecret() {
        properties = new SecretProperties();
    }

    /**
     * Creates a Secret with {@code name} and {@code value}.
     *
     * @param name The name of the secret.
     * @param value the value of the secret.
     */
    public KeyVaultSecret(String name, String value) {
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
     * @return the updated secret object
     */
    public KeyVaultSecret setProperties(SecretProperties properties) {
        Objects.requireNonNull(properties);
        properties.name = this.properties.name;
        this.properties = properties;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("value", value).writeEndObject();
    }

    /**
     * Reads an instance of {@link KeyVaultSecret} from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of {@link KeyVaultSecret} if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the {@link KeyVaultSecret}.
     */
    public static KeyVaultSecret fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            KeyVaultSecret keyVaultSecret = new KeyVaultSecret();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    keyVaultSecret.value = reader.getString();
                } else if ("id".equals(fieldName)) {
                    keyVaultSecret.properties.id = reader.getString();
                    SecretsModelsUtils.unpackId(keyVaultSecret.properties.id,
                        name -> keyVaultSecret.properties.name = name,
                        version -> keyVaultSecret.properties.version = version);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    SecretProperties.deserializeAttributes(reader, keyVaultSecret.properties);
                } else if ("managed".equals(fieldName)) {
                    keyVaultSecret.properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("kid".equals(fieldName)) {
                    keyVaultSecret.properties.keyId = reader.getString();
                } else if ("contentType".equals(fieldName)) {
                    keyVaultSecret.properties.contentType = reader.getString();
                } else if ("tags".equals(fieldName)) {
                    keyVaultSecret.properties.tags = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }

            return keyVaultSecret;
        });
    }
}
