// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@Fluent
class KeyImportRequestParameters {

    /**
     * Whether to import as a hardware key (HSM) or software key.
     */
    @JsonProperty(value = "Hsm")
    private Boolean hsm;

    /**
     * The Json web key.
     */
    @JsonProperty(value = "key", required = true)
    private JsonWebKey key;

    /**
     * The keyAttributes property.
     */
    @JsonProperty(value = "attributes")
    private KeyRequestAttributes keyAttributes;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * Get the keyAttributes value.
     *
     * @return the keyAttributes value
     */
    public KeyRequestAttributes getKeyAttributes() {
        return this.keyAttributes;
    }

    /**
     * Set the keyAttributes value.
     *
     * @param keyAttributes the keyAttributes value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyImportRequestParameters setKeyAttributes(KeyRequestAttributes keyAttributes) {
        this.keyAttributes = keyAttributes;
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
     * @return the KeyRequestParameters object itself.
     */
    public KeyImportRequestParameters setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Set the hsm value.
     *
     * @param hsm the hsm value to set
     * @return the KeyImportParameters object itself.
     */
    public KeyImportRequestParameters setHsm(Boolean hsm) {
        this.hsm = hsm;
        return this;
    }

    /**
     * Get the hsm value.
     *
     * @return the hsm value
     */
    public Boolean getHsm() {
        return this.hsm;
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
     * Set the key value.
     *
     * @param key the key value to set
     * @return the KeyImportParameters object itself.
     */
    public KeyImportRequestParameters setKey(JsonWebKey key) {
        this.key = key;
        return this;
    }
}
