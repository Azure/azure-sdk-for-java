// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents the configurable options to create an Rsa key.
 */
@Fluent
public class CreateRsaKeyOptions extends CreateKeyOptions {

    /**
     * The Rsa key size.
     */
    private Integer keySize;

    /**
     * The hardware protected indicator for the key.
     */
    private boolean hardwareProtected;

    /**
     * The public exponent for the key.
     */
    private int publicExponent;

    /**
     * Creates a RsaKeyCreateOptions with {@code name} as name of the Rsa key.
     * @param name The name of the key.
     */
    public CreateRsaKeyOptions(String name) {
        super(name, KeyType.RSA);
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
    public CreateRsaKeyOptions setKeySize(Integer keySize) {
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
    public CreateRsaKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        super.setKeyOperations(keyOperations);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public CreateRsaKeyOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public CreateRsaKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        super.setExpiresOn(expiresOn);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the RsaKeyCreateOptions object itself.
     */
    @Override
    public CreateRsaKeyOptions setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the RsaKeyCreateOptions object itself.
     */
    public CreateRsaKeyOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Set whether the key being created is of hsm type or not.
     * @param hardwareProtected The hsm value to set.
     * @return the RsaKeyCreateOptions object itself.
     */
    public CreateRsaKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;
        KeyType keyType = hardwareProtected ? KeyType.RSA_HSM : KeyType.RSA;
        setKeyType(keyType);
        return this;
    }

    /**
     * Get the hsm value of the key being created.
     * @return the hsm value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
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
     * @return The updated {@link CreateRsaKeyOptions} object.
     */
    public CreateRsaKeyOptions setPublicExponent(int publicExponent) {
        this.publicExponent = publicExponent;
        return this;
    }
}
