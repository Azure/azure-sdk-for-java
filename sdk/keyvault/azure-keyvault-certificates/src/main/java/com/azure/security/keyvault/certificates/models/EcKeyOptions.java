// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.KeyCurveName;
import com.azure.security.keyvault.certificates.models.webkey.KeyType;

import java.util.List;

/**
 * Represents EcKeyOptions configuration for Certificate policy.
 */
public final class EcKeyOptions extends KeyOptions {

    private boolean hsm;

    public EcKeyOptions() {
        this.keyType = KeyType.EC;
    }

    /**
     * Set the curve value.
     *
     * @param curve the curve value to set
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions curve(KeyCurveName curve) {
        super.curve = curve;
        return this;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the key usage value to set
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions keyUsage(KeyUsageType... keyUsage) {
        super.keyUsage(keyUsage);
        return this;
    }

    /**
     * Set the enhanced key usage.
     *
     * @param ekus the ekus value to set
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions enhancedKeyUsage(List<String> ekus) {
        super.enhancedKeyUsage(ekus);
        return this;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions exportable(Boolean exportable) {
        super.exportable(exportable);
        return this;
    }

    /**
     * Indicate whether the key should e reused or not.
     *
     * @param reuseKey the reuse key value to set.
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions reuseKey(Boolean reuseKey) {
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
     * @return the EcKeyOptions object itself.
     */
    public EcKeyOptions hsm(boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.EC_HSM : KeyType.EC;
        return this;
    }

    /*
     * Set the key usage.
     */
    EcKeyOptions keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }
}
