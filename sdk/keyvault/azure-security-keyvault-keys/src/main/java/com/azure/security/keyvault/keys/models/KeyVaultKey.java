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
 *  Key is the resource consisting of name, {@link JsonWebKey} and its attributes specified in {@link KeyProperties}.
 *  It is managed by Key Service.
 *
 *  @see KeyClient
 *  @see KeyAsyncClient
 */
@Fluent
public class KeyVaultKey {

    /**
     * The Json Web Key
     */
    @JsonProperty(value = "key")
    private JsonWebKey key;

    KeyVaultKey() {
        properties = new KeyProperties();
    }

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey getKey() {
        return this.key;
    }

    /**
     * The key properties
     */
    final KeyProperties properties;

    /**
     * Get the key identifier.
     *
     * @return the key identifier.
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the key name.
     *
     * @return the key name.
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the key properties
     * @return the Key properties
     */
    public KeyProperties getProperties() {
        return this.properties;
    }

    /**
     * Get the key type of the key
     * @return the key type
     */
    public KeyType getKeyType() {
        return key.getKeyType();
    }

    /**
     * Get the key operations of the key
     * @return the key operations
     */
    public List<KeyOperation> getKeyOperations() {
        return key.getKeyOps();
    }

    /**
     * Unpacks the key material json response and updates the variables in the Key Base object.
     * @param key The key value mapping of the key material
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        this.key = properties.createKeyMaterialFromJson(key);
    }

    @JsonProperty(value = "kid")
    private void unpackKid(String kid) {
        properties.unpackId(kid);
    }

    @JsonProperty("attributes")
    @SuppressWarnings("unchecked")
    private void unpackAttributes(Map<String, Object> attributes) {
        properties.unpackAttributes(attributes);
    }
}
