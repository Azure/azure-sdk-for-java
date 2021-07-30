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
    private static final long serialVersionUID = 30_10_00;

    private String kid;

    private String algorithm;

    /**
     * Builder for key vault private key
     * @param algorithm algorithm
     * @param kid The key id
     */
    public KeyVaultPrivateKey(String algorithm, String kid) {
        this.algorithm = algorithm;
        this.kid = kid;
    }

    /**
     * Get the KeyId
     * @return the KeyId
     */
    public String getKid() {
        return kid;
    }

    /**
     * Store the KeyId
     * @param kid the KeyId
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * Store key vault certificate algorithm
     * @param algorithm algorithm
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
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
