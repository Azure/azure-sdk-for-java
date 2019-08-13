// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SignatureAlgorithm.
 */
public enum SignatureAlgorithm {

    PS256("PS256"),
    PS384("PS384"),
    PS512("PS512"),
    RS256("RS256"),
    RS384("RS384"),
    RS512("RS512"),
    ES256("ES256"),
    ES384("ES384"),
    ES512("ES512"),
    ES256K("ES256K");

    private String value;

    /**
     * Creates a custom value for SignatureAlgorithm.
     *
     * @param value
     *            the custom value
     */
    SignatureAlgorithm(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * All the JWK signature implementation.
     */
    public static final List<SignatureAlgorithm> ALL_ALGORITHMS = Collections.unmodifiableList(
            Arrays.asList(RS256, RS384, RS512, PS256, PS384, PS512, ES256, ES384, ES512, ES256K));
}
