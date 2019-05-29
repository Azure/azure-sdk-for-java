// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models;

import com.azure.keyvault.keys.models.webkey.KeyOperation;
import com.azure.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class RsaKeyCreateOptions extends KeyCreateOptions {

    /**
     * The Rsa key size.
     */
    private Integer keySize;

    /**
     * Creates a RsaKeyCreateOptions with {@code name} as name of the Rsa key.
     * @param name The name of the key.
     */
    public RsaKeyCreateOptions(String name) {
        super.name = name;
        this.keyType = KeyType.RSA;
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
     * @param keySize The keySize value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions keySize(Integer keySize) {
        this.keySize = keySize;
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
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions keyOperations(List<KeyOperation> keyOperations) {
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
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions enabled(Boolean enabled) {
        super.enabled(enabled);
        return this;
    }

    /**
     * Set whether the key being created is of hsm type or not.
     * @param hsm The hsm value to set.
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions hsm(Boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.RSA_HSM : KeyType.RSA;
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
