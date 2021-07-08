// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Fluent
class KeyRequestParameters {
    /**
     * The type of key to create. For valid values, see KeyType. Possible values include: 'EC', 'EC-HSM', 'RSA',
     * 'RSA-HSM', 'oct', 'oct-HSM'.
     */
    @JsonProperty(value = "kty", required = true)
    private KeyType kty;

    /**
     * The key size in bits. For example: 2048, 3072, or 4096 for RSA.
     */
    @JsonProperty(value = "key_size")
    private Integer keySize;

    /**
     * The key operations.
     */
    @JsonProperty(value = "key_ops")
    private List<KeyOperation> keyOps;

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

    /**
     * Elliptic curve name. For valid values, see KeyCurveName. Possible values include: 'P-256', 'P-384', 'P-521',
     * 'P-256K'.
     */
    @JsonProperty(value = "crv")
    private KeyCurveName curve;

    /**
     * The public exponent for an RSA key.
     */
    @JsonProperty(value = "public_exponent")
    private int publicExponent;

    /**
     * Get the key type.
     *
     * @return The key type.
     */
    public KeyType getKty() {
        return this.kty;
    }

    /**
     * Set the key type.
     *
     * @param kty The key type to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyRequestParameters setKty(KeyType kty) {
        this.kty = kty;

        return this;
    }

    /**
     * Get the key size in bits.
     *
     * @return The key size.
     */
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * Set the key size in bits.
     *
     * @param keySize The key size value to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyRequestParameters setKeySize(Integer keySize) {
        this.keySize = keySize;

        return this;
    }

    /**
     * Get the key operations.
     *
     * @return The key operations.
     */
    public List<KeyOperation> getKeyOps() {
        return this.keyOps;
    }

    /**
     * Set the key operations.
     *
     * @param keyOps The key operations to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyRequestParameters setKeyOps(List<KeyOperation> keyOps) {
        this.keyOps = keyOps;

        return this;
    }

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
    public KeyRequestParameters setKeyAttributes(KeyRequestAttributes keyAttributes) {
        this.keyAttributes = keyAttributes;

        return this;
    }

    /**
     * Get the tags value.
     *
     * @return The tags value.
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
    public KeyRequestParameters setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Get the key curve.
     *
     * @return The key curve.
     */
    public KeyCurveName getCurve() {
        return this.curve;
    }

    /**
     * Set the key curve.
     *
     * @param curve The key curve to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyRequestParameters setCurve(KeyCurveName curve) {
        this.curve = curve;

        return this;
    }

    /**
     * Get the public exponent for the key.
     *
     * @return The public exponent.
     */
    public int getPublicExponent() {
        return publicExponent;
    }

    /**
     * Set the public exponent for the key.
     *
     * @param publicExponent The public exponent to set.
     *
     * @return The updated {@link KeyRequestParameters} object.
     */
    public KeyRequestParameters setPublicExponent(int publicExponent) {
        this.publicExponent = publicExponent;
        return this;
    }
}
