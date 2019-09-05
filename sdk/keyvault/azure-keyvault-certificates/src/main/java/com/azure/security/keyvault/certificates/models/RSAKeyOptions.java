// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.KeyType;

import java.util.List;

public final class RSAKeyOptions extends KeyOptions {

    private boolean hsm;

    public RSAKeyOptions() {
        this.keyType = KeyType.RSA;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the key usage value to set
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions keyUsage(KeyUsageType... keyUsage) {
        super.keyUsage(keyUsage);
        return this;
    }

    /**
     * Set the enhanced key usage.
     *
     * @param ekus the ekus value to set
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions enhancedKeyUsage(List<String> ekus) {
        super.enhancedKeyUsage(ekus);
        return this;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions exportable(Boolean exportable) {
        super.exportable(exportable);
        return this;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions keySize(Integer keySize) {
        super.keySize = keySize;
        return this;
    }

    /**
     * Indicate whether the key should e reused or not.
     *
     * @param reuseKey the reuse key value to set.
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions reuseKey(Boolean reuseKey) {
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
     * @return the RSAKeyOptions object itself.
     */
    public RSAKeyOptions hsm(boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.RSA_HSM : KeyType.RSA;
        return this;
    }

    /*
     * Set the key usage.
     */
    RSAKeyOptions keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }
}
