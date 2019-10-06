// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;

public class RsaKeyCreateOptions extends KeyCreateOptions {

    /**
     * The Rsa key size.
     */
    private Integer keySize;

    /**
     * The hsm indicator for the key.
     */
    private boolean hsm;

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
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize The keySize value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions setKeySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime getNotBefore} UTC time.
     *
     * @param notBefore The getNotBefore UTC time to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime getExpires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions setExpires(OffsetDateTime expires) {
        super.setExpires(expires);
        return this;
    }

    /**
     * Set the getTags to be associated with the key.
     *
     * @param tags The getTags to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public RsaKeyCreateOptions setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Set the setEnabled value.
     *
     * @param enabled The setEnabled value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Set whether the key being getCreated is of hsm type or not.
     * @param hsm The hsm value to set.
     * @return the RsaKeyCreateOptions object itself.
     */
    public RsaKeyCreateOptions setHsm(Boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.RSA_HSM : KeyType.RSA;
        return this;
    }

    /**
     * Get the hsm value of the key being getCreated.
     * @return the hsm value.
     */
    public Boolean isHsm() {
        return this.hsm;
    }
}
