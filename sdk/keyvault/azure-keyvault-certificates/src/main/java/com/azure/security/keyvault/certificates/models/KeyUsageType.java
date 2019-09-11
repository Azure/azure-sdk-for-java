// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for KeyUsageType.
 */
public enum KeyUsageType {
    DIGITAL_SIGNATURE("digitalSignature"),

    NON_REPUDIATION("nonRepudiation"),

    KEY_ENCIPHERMENT("keyEncipherment"),

    DATA_ENCIPHERMENT("dataEncipherment"),

    KEY_AGREEMENT("keyAgreement"),

    KEY_CERT_SIGN("keyCertSign"),

    CRL_SIGN("cRLSign"),

    ENCIPHER_ONLY("encipherOnly"),

    DECIPHER_ONLY("decipherOnly");

    private String value;

    /**
     * Creates a custom value for KeyUsageType.
     * @param value the custom value
     */
    KeyUsageType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Return the KeyUsageType which maps to {@code value}.
     * @param value The value whose equivalent KeyUsageType is needed.
     * @return the KeyUsageType
     */
    public static KeyUsageType fromString(String value) {
        for (KeyUsageType keyUsageType : values()) {
            if (keyUsageType.value.equalsIgnoreCase(value)) {
                return keyUsageType;
            }
        }
        return null;
    }
}
