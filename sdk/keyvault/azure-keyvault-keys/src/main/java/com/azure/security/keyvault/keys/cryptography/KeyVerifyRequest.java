// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.implementation.Base64Url;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key verify parameters.
 */
class KeyVerifyRequest {
    /**
     * The signing/verification algorithm. For more information on possible
     * algorithm types, see SignatureAlgorithm. Possible values
     * include: 'PS256', 'PS384', 'PS512', 'RS256', 'RS384', 'RS512', 'RSNULL',
     * 'ES256', 'ES384', 'ES512', 'ES256K'.
     */
    @JsonProperty(value = "alg", required = true)
    private SignatureAlgorithm algorithm;

    /**
     * The digest used for signing.
     */
    @JsonProperty(value = "digest", required = true)
    private Base64Url digest;

    /**
     * The signature to be verified.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url signature;

    /**
     * Get the algorithm value.
     *
     * @return the algorithm value
     */
    public SignatureAlgorithm algorithm() {
        return this.algorithm;
    }

    /**
     * Set the algorithm value.
     *
     * @param algorithm the algorithm value to set
     * @return the KeyVerifyParameters object itself.
     */
    public KeyVerifyRequest algorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Get the digest value.
     *
     * @return the digest value
     */
    public byte[] digest() {
        if (this.digest == null) {
            return new byte[0];
        }
        return this.digest.decodedBytes();
    }

    /**
     * Set the digest value.
     *
     * @param digest the digest value to set
     * @return the KeyVerifyParameters object itself.
     */
    public KeyVerifyRequest digest(byte[] digest) {
        if (digest == null) {
            this.digest = null;
        } else {
            this.digest = Base64Url.encode(digest);
        }
        return this;
    }

    /**
     * Get the signature value.
     *
     * @return the signature value
     */
    public byte[] signature() {
        if (this.signature == null) {
            return new byte[0];
        }
        return this.signature.decodedBytes();
    }

    /**
     * Set the signature value.
     *
     * @param signature the signature value to set
     * @return the KeyVerifyParameters object itself.
     */
    public KeyVerifyRequest signature(byte[] signature) {
        if (signature == null) {
            this.signature = null;
        } else {
            this.signature = Base64Url.encode(signature);
        }
        return this;
    }

}
