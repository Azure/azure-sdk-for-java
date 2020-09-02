// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * Defines values for PiiEntityDomainType.
 */
public enum PiiEntityDomainType {
    /**
     * Protected health information (PHI) as the PiiEntityDomainType.
     */
    PROTECTED_HEALTH_INFORMATION("PHI");

    /** The actual serialized value for a PiiEntityDomainType instance. */
    private final String value;

    PiiEntityDomainType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a PiiEntityDomainType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed PiiEntityDomainType object, or null if unable to parse.
     */
    public static PiiEntityDomainType fromString(String value) {
        PiiEntityDomainType[] items = PiiEntityDomainType.values();
        for (PiiEntityDomainType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * The string representation of the enum value.
     *
     * @return the string representation of the enum value.
     */
    public String toString() {
        return this.value;
    }
}
