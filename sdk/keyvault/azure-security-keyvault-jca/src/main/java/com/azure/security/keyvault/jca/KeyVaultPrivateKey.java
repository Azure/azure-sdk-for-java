// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import javax.crypto.SecretKey;
import java.security.PrivateKey;

/**
 * KeyVault fake private which work when key less
 *
 * @deprecated Should not use this class outside of azure-security-keyvault-jca.jar.
 * Move this class to implementation package.
 *
 * @see PrivateKey
 * @see SecretKey
 */
@Deprecated
public class KeyVaultPrivateKey implements PrivateKey, SecretKey {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 30_10_00;

    /**
     * Key ID.
     */
    private String kid;

    /**
     * Algorithm.
     */
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

    /**
     * Get the algorithm
     * @return the algorithm
     */
    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the format
     * @return the format
     */
    @Override
    public String getFormat() {
        return "RAW";
    }

    /**
     * Get the encoded
     * @return the encoded
     */
    @Override
    public byte[] getEncoded() {
        return new byte[2048];
    }
}
