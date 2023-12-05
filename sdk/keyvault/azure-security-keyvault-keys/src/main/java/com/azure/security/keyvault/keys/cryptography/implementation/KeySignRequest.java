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
public final class KeySignRequest {
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
     * The value to sign.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url value;

    /**
     * Get the algorithm to sign with.
     *
     * @return The algorithm to sign with.
     */
    public SignatureAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Set the algorithm to sign with.
     *
     * @param algorithm The algorithm to set.
     *
     * @return The updated {@link KeySignRequest} object.
     */
    public KeySignRequest setAlgorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Get the value to sign.
     *
     * @return The value to sign.
     */
    public byte[] getValue() {
        if (this.value == null) {
            return new byte[0];
        }

        return this.value.decodedBytes();
    }

    /**
     * Set value to sign.
     *
     * @param value The value to set.
     *
     * @return The updated {@link KeySignRequest} object.
     */
    public KeySignRequest setValue(byte[] value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = Base64Url.encode(value);
        }

        return this;
    }

}
