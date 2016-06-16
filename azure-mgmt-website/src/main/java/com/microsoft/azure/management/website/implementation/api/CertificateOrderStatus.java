/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateOrderStatus.
 */
public enum CertificateOrderStatus {
    /** Enum value Pendingissuance. */
    PENDINGISSUANCE("Pendingissuance"),

    /** Enum value Issued. */
    ISSUED("Issued"),

    /** Enum value Revoked. */
    REVOKED("Revoked"),

    /** Enum value Canceled. */
    CANCELED("Canceled"),

    /** Enum value Denied. */
    DENIED("Denied"),

    /** Enum value Pendingrevocation. */
    PENDINGREVOCATION("Pendingrevocation"),

    /** Enum value PendingRekey. */
    PENDING_REKEY("PendingRekey"),

    /** Enum value Unused. */
    UNUSED("Unused"),

    /** Enum value Expired. */
    EXPIRED("Expired"),

    /** Enum value NotSubmitted. */
    NOT_SUBMITTED("NotSubmitted");

    /** The actual serialized value for a CertificateOrderStatus instance. */
    private String value;

    CertificateOrderStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CertificateOrderStatus instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CertificateOrderStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateOrderStatus object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateOrderStatus fromValue(String value) {
        CertificateOrderStatus[] items = CertificateOrderStatus.values();
        for (CertificateOrderStatus item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
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
