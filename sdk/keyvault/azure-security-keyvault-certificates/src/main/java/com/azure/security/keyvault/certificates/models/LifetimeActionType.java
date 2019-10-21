// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines action values for type of {@link LifetimeAction} in {@link CertificatePolicy}.
 */
public enum LifetimeActionType {

    /** Enum value EmailContacts. */
    EMAIL_CONTACTS("EmailContacts"),

    /** Enum value AutoRenew. */
    AUTO_RENEW("AutoRenew");

    /** The actual serialized value for a LifetimeActionType instance. */
    private String value;

    LifetimeActionType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a LifetimeActionType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed LifetimeActionType object, or null if unable to parse.
     */
    @JsonCreator
    public static LifetimeActionType fromString(String value) {
        LifetimeActionType[] items = LifetimeActionType.values();
        for (LifetimeActionType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get the string value of the enum.
     * @return the string value of enum.
     */
    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
