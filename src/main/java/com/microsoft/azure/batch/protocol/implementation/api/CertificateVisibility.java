/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateVisibility.
 */
public enum CertificateVisibility {
    /** Enum value starttask. */
    STARTTASK("starttask"),

    /** Enum value task. */
    TASK("task"),

    /** Enum value remoteuser. */
    REMOTEUSER("remoteuser"),

    /** Enum value unmapped. */
    UNMAPPED("unmapped");

    /** The actual serialized value for a CertificateVisibility instance. */
    private String value;

    CertificateVisibility(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CertificateVisibility instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CertificateVisibility instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateVisibility object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateVisibility fromValue(String value) {
        CertificateVisibility[] items = CertificateVisibility.values();
        for (CertificateVisibility item : items) {
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
