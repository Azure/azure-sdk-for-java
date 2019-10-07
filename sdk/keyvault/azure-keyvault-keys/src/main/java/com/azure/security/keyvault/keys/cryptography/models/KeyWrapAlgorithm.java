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
public enum KeyWrapAlgorithm {

    RSA_OAEP("RSA-OAEP"),
    RSA_OAEP_256("RSA-OAEP-256"),
    RSA1_5("RSA1_5"),
    A192KW("A192KW"),
    A128KW("A128KW"),
    A256KW("A256KW");

    private String value;

    /**
     * Creates a custom value for KeyWrapAlgorithm.
     *
     * @param value the custom value
     */
    KeyWrapAlgorithm(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Return the KeyWrapAlgorithm which maps to {@code value}
     * @param value The value whose equivalent KeyWrapAlgorithm is needed.
     * @return the KeyOperation
     */
    public static KeyWrapAlgorithm fromString(String value) {
        for (KeyWrapAlgorithm algorithm : values()) {
            if (algorithm.value.equalsIgnoreCase(value)) {
                return algorithm;
            }
        }
        return null;
    }
}
