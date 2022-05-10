// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.KeyExportEncryptionAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The release key parameters.
 */
@Fluent
public final class KeyReleaseParameters {
    /*
     * The attestation assertion for the target of the key release.
     */
    @JsonProperty(value = "target", required = true)
    private String targetAttestationToken;

    /*
     * A client provided nonce for freshness.
     */
    @JsonProperty(value = "nonce")
    private String nonce;

    /*
     * The encryption algorithm to use to protected the exported key material
     */
    @JsonProperty(value = "enc")
    private KeyExportEncryptionAlgorithm encryptionAlgorithm;

    /**
     * Get the attestation assertion for the target of the key release.
     *
     * @return The target value.
     */
    public String getTargetAttestationToken() {
        return this.targetAttestationToken;
    }

    /**
     * Set the attestation assertion for the target of the key release.
     *
     * @param targetAttestationToken The attestation assertion for the target of the key release.
     *
     * @return The updated {@link KeyReleaseParameters} object.
     */
    public KeyReleaseParameters setTargetAttestationToken(String targetAttestationToken) {
        this.targetAttestationToken = targetAttestationToken;

        return this;
    }

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
     * @return The updated {@link KeyReleaseParameters} object.
     */
    public KeyReleaseParameters setNonce(String nonce) {
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
     * @return The updated {@link KeyReleaseParameters} object.
     */
    public KeyReleaseParameters setAlgorithm(KeyExportEncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;

        return this;
    }
}
