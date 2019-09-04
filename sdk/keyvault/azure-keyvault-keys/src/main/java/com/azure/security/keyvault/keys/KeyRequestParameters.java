// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Fluent
class KeyRequestParameters {
    /**
     * The type of key to create. For valid values, see KeyType.
     * Possible values include: 'EC', 'EC-HSM', 'RSA', 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty", required = true)
    private KeyType kty;

    /**
     * The key size in bits. For example: 2048, 3072, or 4096 for RSA.
     */
    @JsonProperty(value = "key_size")
    private Integer keySize;

    /**
     * The keyOps property.
     */
    @JsonProperty(value = "key_ops")
    private List<KeyOperation> keyOps;

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
     * Elliptic curve name. For valid values, see KeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'P-256K'.
     */
    @JsonProperty(value = "crv")
    private KeyCurveName curve;

    /**
     * Get the keyType value.
     *
     * @return the keyType value
     */
    public KeyType kty() {
        return this.kty;
    }

    /**
     * Set the keyType value.
     *
     * @param kty the keyType value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters kty(KeyType kty) {
        this.kty = kty;
        return this;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    public Integer keySize() {
        return this.keySize;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters keySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Get the keyOps value.
     *
     * @return the keyOps value
     */
    public List<KeyOperation> keyOps() {
        return this.keyOps;
    }

    /**
     * Set the keyOps value.
     *
     * @param keyOps the keyOps value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters keyOps(List<KeyOperation> keyOps) {
        this.keyOps = keyOps;
        return this;
    }

    /**
     * Get the keyAttributes value.
     *
     * @return the keyAttributes value
     */
    public KeyRequestAttributes keyAttributes() {
        return this.keyAttributes;
    }

    /**
     * Set the keyAttributes value.
     *
     * @param keyAttributes the keyAttributes value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters keyAttributes(KeyRequestAttributes keyAttributes) {
        this.keyAttributes = keyAttributes;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the curve value.
     *
     * @return the curve value
     */
    public KeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve value.
     *
     * @param curve the curve value to set
     * @return the KeyRequestParameters object itself.
     */
    public KeyRequestParameters curve(KeyCurveName curve) {
        this.curve = curve;
        return this;
    }
}
