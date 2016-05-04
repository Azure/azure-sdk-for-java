/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateProductType.
 */
public enum CertificateProductType {
    /** Enum value StandardDomainValidatedSsl. */
    STANDARDDOMAINVALIDATEDSSL("StandardDomainValidatedSsl"),

    /** Enum value StandardDomainValidatedWildCardSsl. */
    STANDARDDOMAINVALIDATEDWILDCARDSSL("StandardDomainValidatedWildCardSsl");

    /** The actual serialized value for a CertificateProductType instance. */
    private String value;

    CertificateProductType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CertificateProductType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CertificateProductType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateProductType object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateProductType fromValue(String value) {
        CertificateProductType[] items = CertificateProductType.values();
        for (CertificateProductType item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
