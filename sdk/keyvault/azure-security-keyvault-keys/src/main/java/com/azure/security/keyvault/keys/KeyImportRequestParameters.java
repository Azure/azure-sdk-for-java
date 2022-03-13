// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyReleasePolicy;
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
     * The JSON Web Key to import.
     */
    @JsonProperty(value = "key", required = true)
    private JsonWebKey key;

    /**
     * The key attributes.
     */
    @JsonProperty(value = "attributes")
    private KeyRequestAttributes keyAttributes;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /*
     * The policy rules under which the key can be exported.
     */
    @JsonProperty(value = "release_policy")
    private KeyReleasePolicy releasePolicy;

    /**
     * Get the key attributes.
     *
     * @return The key attributes.
     */
    public KeyRequestAttributes getKeyAttributes() {
        return this.keyAttributes;
    }

    /**
     * Set the key attributes.
     *
     * @param keyAttributes The key attributes to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyImportRequestParameters setKeyAttributes(KeyRequestAttributes keyAttributes) {
        this.keyAttributes = keyAttributes;

        return this;
    }

    /**
     * Get the tags associated with the key.
     *
     * @return The tag names and values.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyImportRequestParameters setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Set the HSM value.
     *
     * @param hsm The HSM value to set.
     *
     * @return The KeyImportParameters object itself.
     */
    public KeyImportRequestParameters setHsm(Boolean hsm) {
        this.hsm = hsm;

        return this;
    }

    /**
     * Get the HSM value.
     *
     * @return The HSM value
     */
    public Boolean getHsm() {
        return this.hsm;
    }

    /**
     * Get the JSON Web Key to import.
     *
     * @return The JSON Web Key.
     */
    public JsonWebKey getKey() {
        return this.key;
    }

    /**
     * Set the JSON Web Key to import.
     *
     * @param key The JSON Web Key to set.
     *
     * @return The updated {@link KeyImportRequestParameters} object.
     */
    public KeyImportRequestParameters setKey(JsonWebKey key) {
        this.key = key;

        return this;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The policy rules under which the key can be exported.
     */
    public KeyReleasePolicy getReleasePolicy() {
        return this.releasePolicy;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The policy rules under which the key can be exported.
     *
     * @return The updated {@link KeyImportRequestParameters} object.
     */
    public KeyImportRequestParameters setReleasePolicy(KeyReleasePolicy releasePolicy) {
        this.releasePolicy = releasePolicy;

        return this;
    }
}
