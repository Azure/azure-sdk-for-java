// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

/**
 * Represents the configurable options to release a key.
 */
public class ReleaseKeyOptions {
    /*
     * A client provided nonce for freshness.
     */
    private String nonce;

    /*
     * The encryption algorithm to use to protected the exported key material
     */
    private KeyExportEncryptionAlgorithm encryptionAlgorithm;

    /**
     * Get a client provided nonce for freshness.
     *
     * @return A client provided nonce for freshness.
     */
    public String getNonce() {
        return this.nonce;
    }

    /**
     * Set a client provided nonce for freshness.
     *
     * @param nonce A client provided nonce for freshness.
     *
     * @return The updated {@link ReleaseKeyOptions} object.
     */
    public ReleaseKeyOptions setNonce(String nonce) {
        this.nonce = nonce;

        return this;
    }

    /**
     * Get the encryption algorithm to use to protected the exported key material.
     *
     * @return The encryption algorithm to use to protected the exported key material.
     */
    public KeyExportEncryptionAlgorithm getAlgorithm() {
        return this.encryptionAlgorithm;
    }

    /**
     * Set the encryption algorithm to use to protected the exported key material.
     *
     * @param encryptionAlgorithm The encryption algorithm to use to protected the exported key material.
     *
     * @return The updated {@link ReleaseKeyOptions} object.
     */
    public ReleaseKeyOptions setAlgorithm(KeyExportEncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;

        return this;
    }
}
