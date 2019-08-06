// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.implementation.Base64Url;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key verify parameters.
 */
class KeySignRequest {
    /**
     * The signing/verification algorithm. For more information on possible
     * algorithm types, see SignatureAlgorithm. Possible values
     * include: 'PS256', 'PS384', 'PS512', 'RS256', 'RS384', 'RS512', 'RSNULL',
     * 'ES256', 'ES384', 'ES512', 'ES256K'.
     */
    @JsonProperty(value = "alg", required = true)
    private SignatureAlgorithm algorithm;

    /**
     * The value property.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url value;


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
    public KeySignRequest algorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public byte[] value() {
        if (this.value == null) {
            return new byte[0];
        }
        return this.value.decodedBytes();
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the KeySignParameters object itself.
     */
    public KeySignRequest value(byte[] value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = Base64Url.encode(value);
        }
        return this;
    }

}
