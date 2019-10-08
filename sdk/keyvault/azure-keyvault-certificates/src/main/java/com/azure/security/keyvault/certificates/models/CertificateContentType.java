// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SecretContentType.
 */
public enum CertificateContentType {
    PKCS12("application/x-pkcs12"),

    PEM("application/x-pem-file");

    private String value;

    /**
     * Creates a custom value for KeyUsageType.
     * @param value the custom value
     */
    CertificateContentType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    /**
     * Parse content type from string.
     * @param contentType The string value to parse content type from.
     * @return the SecretContentType
     */
    public static CertificateContentType fromString(String contentType) {
        for (CertificateContentType secretContentType : values()) {
            if (secretContentType.value.equals(contentType)) {
                return secretContentType;
            }
        }
        return null;
    }
}
