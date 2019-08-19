// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines values for EncryptionAlgorithm.
 */
public enum EncryptionAlgorithm {

    RSA_OAEP("RSA-OAEP"),
    RSA_OAEP_256("RSA-OAEP-256"),
    RSA1_5("RSA1_5"),
    A256CBC_HS512("A256CBC-HS512"),
    A128CBC_HS256("A128CBC-HS256"),
    A192CBC_HS384("A192CBC-HS384"),
    A256CBC("A256CBC"),
    A192CBC("A192CBC"),
    A128CBC("A128CBC");

    private String value;

    /**
     * Creates a custom value for EncryptionAlgorithm.
     *
     * @param value the custom value
     */
    EncryptionAlgorithm(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * All the JWK encryption implementation.
     */
    public static final List<EncryptionAlgorithm> ALL_ALGORITHMS = Collections
            .unmodifiableList(Arrays.asList(RSA_OAEP, RSA1_5, RSA_OAEP_256, A256CBC_HS512, A128CBC_HS256, A192CBC_HS384, A256CBC, A192CBC, A128CBC));
}
