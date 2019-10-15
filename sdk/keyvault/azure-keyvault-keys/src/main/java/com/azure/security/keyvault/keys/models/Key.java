// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Key {

    /**
     * The Json Web Key
     */
    @JsonProperty(value = "key")
    private JsonWebKey keyMaterial;

    Key() {
        properties = new KeyProperties();
    }

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey getKeyMaterial() {
        return this.keyMaterial;
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
     * Unpacks the key material json response and updates the variables in the Key Base object.
     * @param key The key value mapping of the key material
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        keyMaterial = properties.createKeyMaterialFromJson(key);
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
