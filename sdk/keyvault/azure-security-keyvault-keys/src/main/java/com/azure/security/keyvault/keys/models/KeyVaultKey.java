// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Key is the resource consisting of name, {@link JsonWebKey} and its attributes specified in {@link KeyProperties}.
 * It is managed by Key Service.
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
@Fluent
public class KeyVaultKey {
    /**
     * The Json Web Key.
     */
    @JsonProperty(value = "key")
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

    /**
     * Unpacks the key material JSON response and updates the variables in the key base object.
     *
     * @param key The key value mapping of the key material.
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        this.key = properties.createKeyMaterialFromJson(key);
    }

    @JsonProperty("attributes")
    @SuppressWarnings("unchecked")
    private void unpackAttributes(Map<String, Object> attributes) {
        properties.unpackAttributes(attributes);
    }

    @JsonProperty("tags")
    private void setTags(Map<String, String> tags) {
        properties.setTags(tags);
    }

    @JsonProperty("managed")
    private void setManaged(boolean managed) {
        properties.setManaged(managed);
    }
}
