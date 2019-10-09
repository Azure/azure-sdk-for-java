// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models.webkey;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateKeyType.
 */
public enum CertificateKeyType {
     EC("EC"),

     EC_HSM("EC-HSM"),

     RSA("RSA"),

     RSA_HSM("RSA-HSM"),

     OCT("oct");

    private String value;

    /**
     * Creates a custom value for CertificateKeyType.
     * @param value The custom value
     */
    CertificateKeyType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Calculates the hashcode of this {@link CertificateKeyType} value
     * @return the hashcode for {@link CertificateKeyType} value
     */
    public int hashValue() {
        return value.hashCode();
    }

    /**
     * Return the CertificateKeyType which maps to {@code value}
     * @param value The value whose equivalent CertificateKeyType is needed.
     * @return the CertificateKeyType.
     */
    public static CertificateKeyType fromString(String value) {
        for (CertificateKeyType keyType : values()) {
            if (keyType.value.equalsIgnoreCase(value)) {
                return keyType;
            }
        }
        return null;
    }
}
