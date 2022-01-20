// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import javax.crypto.SecretKey;
import java.security.PrivateKey;

/**
 * KeyVault fake private which work when key less
 */
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

    private final KeyVaultClient keyVaultClient;

    /**
     * Builder for key vault private key
     * @param algorithm algorithm
     * @param kid The key id
     * @param keyVaultClient related keyVaultClient
     */
    public KeyVaultPrivateKey(String algorithm, String kid, KeyVaultClient keyVaultClient) {
        this.algorithm = algorithm;
        this.kid = kid;
        this.keyVaultClient = keyVaultClient;
    }

    /**
     * Builder for key vault private key
     * @param algorithm algorithm
     * @param kid The key id
     */
    public KeyVaultPrivateKey(String algorithm, String kid) {
        this(algorithm, kid, null);
    }

    /**
     * Get related keyVaultClient, which will be used when signature
     * @return related keyVaultClient
     */
    public KeyVaultClient getKeyVaultClient() {
        return keyVaultClient;
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
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[2048];
    }
}
