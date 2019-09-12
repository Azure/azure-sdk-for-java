// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models.webkey;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for KeyType.
 */
public enum KeyType {
     EC("EC"),

     EC_HSM("EC-HSM"),

     RSA("RSA"),

     RSA_HSM("RSA-HSM"),

     OCT("oct");

    private String value;

    /**
     * Creates a custom value for KeyType.
     * @param value The custom value
     */
    KeyType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Calculates the hashcode of this {@link KeyType} value
     * @return the hashcode for {@link KeyType} value
     */
    public int hashValue() {
        return value.hashCode();
    }

    /**
     * Return the KeyType which maps to {@code value}
     * @param value The value whose equivalent KeyType is needed.
     * @return the KeyType.
     */
    public static KeyType fromString(String value) {
        for (KeyType keyType : values()) {
            if (keyType.value.equalsIgnoreCase(value)) {
                return keyType;
            }
        }
        return null;
    }
}
