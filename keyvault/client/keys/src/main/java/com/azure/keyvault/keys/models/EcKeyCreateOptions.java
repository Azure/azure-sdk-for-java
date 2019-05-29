// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models;

import com.azure.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.keyvault.keys.models.webkey.KeyOperation;
import com.azure.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class EcKeyCreateOptions extends KeyCreateOptions {

    /**
     * The Ec key curve.
     */
    private KeyCurveName curve;

    /**
     * Creates a EcKeyCreateOptions with {@code name} as name of the Ec key.
     * @param name The name of the Ec key.
     */
    public EcKeyCreateOptions(String name) {
        super.name = name;
        this.keyType = KeyType.EC;
    }

    /**
     * Get the curve.
     *
     * @return the curve.
     */
    public KeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve.
     *
     * @param curve The curve to set
     * @return the EcKeyCreateOptions object itself.
     */
    public EcKeyCreateOptions curve(KeyCurveName curve) {
        this.curve = curve;
        return this;
    }

    /**
     * Get the key operations.
     *
     * @return the key operations.
     */
    public List<KeyOperation> keyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions keyOperations(List<KeyOperation> keyOperations) {
        this.keyOperations = keyOperations;
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type.
     */
    public KeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the EcKeyCreateOptions object itself.
     */
    public KeyCreateOptions enabled(Boolean enabled) {
        super.enabled(enabled);
        return this;
    }

    /**
     * Set whether the key being created is of hsm type or not.
     * @param hsm The hsm value to set.
     * @return the EcKeyCreateOptions object itself.
     */
    public EcKeyCreateOptions hsm(Boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.EC_HSM : KeyType.EC;
        return this;
    }

    /**
     * Get the hsm value of the key being created.
     * @return the hsm value.
     */
    public Boolean hsm() {
        return this.hsm;
    }
}
