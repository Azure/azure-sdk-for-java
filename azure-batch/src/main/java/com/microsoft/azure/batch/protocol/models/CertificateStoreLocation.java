/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateStoreLocation.
 */
public enum CertificateStoreLocation {
    /** Enum value currentuser. */
    CURRENTUSER("currentuser"),

    /** Enum value localmachine. */
    LOCALMACHINE("localmachine"),

    /** Enum value unmapped. */
    UNMAPPED("unmapped");

    /** The actual serialized value for a CertificateStoreLocation instance. */
    private String value;

    CertificateStoreLocation(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a CertificateStoreLocation instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateStoreLocation object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateStoreLocation fromString(String value) {
        CertificateStoreLocation[] items = CertificateStoreLocation.values();
        for (CertificateStoreLocation item : items) {
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
