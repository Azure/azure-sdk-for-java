// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.KeyType;

import java.util.List;

/**
 * Represents RsaKeyOptions configuration for Certificate policy.
 */
public final class RsaKeyOptions extends KeyOptions {

    private boolean hsm;

    public RsaKeyOptions() {
        this.keyType = KeyType.RSA;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the key usage value to set
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions keyUsage(KeyUsageType... keyUsage) {
        super.keyUsage(keyUsage);
        return this;
    }

    /**
     * Set the enhanced key usage.
     *
     * @param ekus the ekus value to set
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions enhancedKeyUsage(List<String> ekus) {
        super.enhancedKeyUsage(ekus);
        return this;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions exportable(Boolean exportable) {
        super.exportable(exportable);
        return this;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions keySize(Integer keySize) {
        super.keySize = keySize;
        return this;
    }

    /**
     * Indicate whether the key should e reused or not.
     *
     * @param reuseKey the reuse key value to set.
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions reuseKey(Boolean reuseKey) {
        super.reuseKey(reuseKey);
        return this;
    }

    /**
     * Get the hsm indicator of the key.
     * @return the hsm indicator.
     */
    public boolean hsm() {
        return this.hsm;
    }

    /**
     * Indicate if the Rsa key is of hsm type or not.
     * @param hsm The hsm indicator value to set.
     * @return the RsaKeyOptions object itself.
     */
    public RsaKeyOptions hsm(boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.RSA_HSM : KeyType.RSA;
        return this;
    }

    /*
     * Set the key usage.
     */
    RsaKeyOptions keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }
}
