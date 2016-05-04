/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateOrderActionType.
 */
public enum CertificateOrderActionType {
    /** Enum value CertificateIssued. */
    CERTIFICATEISSUED("CertificateIssued"),

    /** Enum value CertificateOrderCanceled. */
    CERTIFICATEORDERCANCELED("CertificateOrderCanceled"),

    /** Enum value CertificateOrderCreated. */
    CERTIFICATEORDERCREATED("CertificateOrderCreated"),

    /** Enum value CertificateRevoked. */
    CERTIFICATEREVOKED("CertificateRevoked"),

    /** Enum value DomainValidationComplete. */
    DOMAINVALIDATIONCOMPLETE("DomainValidationComplete"),

    /** Enum value FraudDetected. */
    FRAUDDETECTED("FraudDetected"),

    /** Enum value OrgNameChange. */
    ORGNAMECHANGE("OrgNameChange"),

    /** Enum value OrgValidationComplete. */
    ORGVALIDATIONCOMPLETE("OrgValidationComplete"),

    /** Enum value SanDrop. */
    SANDROP("SanDrop");

    /** The actual serialized value for a CertificateOrderActionType instance. */
    private String value;

    CertificateOrderActionType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CertificateOrderActionType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CertificateOrderActionType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateOrderActionType object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateOrderActionType fromValue(String value) {
        CertificateOrderActionType[] items = CertificateOrderActionType.values();
        for (CertificateOrderActionType item : items) {
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
