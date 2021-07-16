// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.PrivateKey;

/**
 * KeyVault fake private which work when key less
 */
public class KeyVaultPrivateKey implements PrivateKey {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private String algorithm;

    private String version;

    private String alias;

    /**
     * builder for key vault private key
     * @param algorithm algorithm
     * @param version certificate version
     * @param alias certificate alias
     */
    public KeyVaultPrivateKey(String algorithm, String version, String alias) {
        this.algorithm = algorithm;
        this.version = version;
        this.alias = alias;
    }

    /**
     * set key vault certificate algorithm
     * @param algorithm algorithm
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Get key vault certificate alias
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set key vault certificate version
     * @param version version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get key vault certificate alias
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Set key vault certificate alias
     * @param alias alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
