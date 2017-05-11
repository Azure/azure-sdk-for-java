/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for certificate types.
 */
public enum CertificateType {
    /** Enum value AsymmetricX509Cert. */
    ASYMMETRIC_X509_CERT("AsymmetricX509Cert"),

    /** Enum value Symmetric. */
    SYMMETRIC("Symmetric");

    /** The actual serialized value for a AppServiceOperatingSystem instance. */
    private String value;

    CertificateType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a certificate type instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateType object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateType fromString(String value) {
        CertificateType[] items = CertificateType.values();
        for (CertificateType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
