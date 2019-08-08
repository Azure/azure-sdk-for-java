// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.implementation.Base64Url;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key operations parameters.
 */
class KeyWrapUnwrapRequest {
    /**
     * algorithm identifier. Possible values include: 'RSA-OAEP',
     * 'RSA-OAEP-256', 'RSA1_5'.
     */
    @JsonProperty(value = "alg", required = true)
    private KeyWrapAlgorithm algorithm;

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
    public KeyWrapAlgorithm algorithm() {
        return this.algorithm;
    }

    /**
     * Set the algorithm value.
     *
     * @param algorithm the algorithm value to set
     * @return the KeyOperationsParameters object itself.
     */
    public KeyWrapUnwrapRequest algorithm(KeyWrapAlgorithm algorithm) {
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
     * @return the KeyOperationsParameters object itself.
     */
    public KeyWrapUnwrapRequest value(byte[] value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = Base64Url.encode(value);
        }
        return this;
    }

}
