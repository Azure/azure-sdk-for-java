// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Base64Url;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key verify request parameters.
 */
@Fluent
public final class KeyVerifyRequest {
    /**
     * The signing/verification algorithm. For more information on possible
     * algorithm types, see {@link SignatureAlgorithm}. Possible values
     * include: {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#PS384 PS384},
     * {@link SignatureAlgorithm#PS512 PS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384}, {@link SignatureAlgorithm#RS512 RS512}, 'RSNULL',
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 ES384},
     * {@link SignatureAlgorithm#RS512 RS512} and {@link SignatureAlgorithm#ES256K ES256K}.
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
     * @return The algorithm value.
     */
    public SignatureAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Set the algorithm to verify with.
     *
     * @param algorithm The algorithm to set.
     *
     * @return The updated {@link KeyVerifyRequest} object.
     */
    public KeyVerifyRequest setAlgorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Get the digest used for signing.
     *
     * @return The digest used for signing.
     */
    public byte[] getDigest() {
        if (this.digest == null) {
            return new byte[0];
        }

        return this.digest.decodedBytes();
    }

    /**
     * Set the digest used for signing.
     *
     * @param digest The digest to set.
     *
     * @return The updated {@link KeyVerifyRequest} object.
     */
    public KeyVerifyRequest setDigest(byte[] digest) {
        if (digest == null) {
            this.digest = null;
        } else {
            this.digest = Base64Url.encode(digest);
        }

        return this;
    }

    /**
     * Get the signature to be verified.
     *
     * @return The signature to be verified.
     */
    public byte[] getSignature() {
        if (this.signature == null) {
            return new byte[0];
        }

        return this.signature.decodedBytes();
    }

    /**
     * Set the signature to be verified.
     *
     * @param signature The signature to set.
     *
     * @return The updated {@link KeyVerifyRequest} object.
     */
    public KeyVerifyRequest setSignature(byte[] signature) {
        if (signature == null) {
            this.signature = null;
        } else {
            this.signature = Base64Url.encode(signature);
        }

        return this;
    }

}
