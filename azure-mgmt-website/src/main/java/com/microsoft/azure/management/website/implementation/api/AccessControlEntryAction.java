/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AccessControlEntryAction.
 */
public enum AccessControlEntryAction {
    /** Enum value Permit. */
    PERMIT("Permit"),

    /** Enum value Deny. */
    DENY("Deny");

    /** The actual serialized value for a AccessControlEntryAction instance. */
    private String value;

    AccessControlEntryAction(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a AccessControlEntryAction instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a AccessControlEntryAction instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AccessControlEntryAction object, or null if unable to parse.
     */
    @JsonCreator
    public static AccessControlEntryAction fromValue(String value) {
        AccessControlEntryAction[] items = AccessControlEntryAction.values();
        for (AccessControlEntryAction item : items) {
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
