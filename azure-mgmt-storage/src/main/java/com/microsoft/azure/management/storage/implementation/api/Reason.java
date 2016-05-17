/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Reason.
 */
public enum Reason {
    /** Enum value AccountNameInvalid. */
    ACCOUNT_NAME_INVALID("AccountNameInvalid"),

    /** Enum value AlreadyExists. */
    ALREADY_EXISTS("AlreadyExists");

    /** The actual serialized value for a Reason instance. */
    private String value;

    Reason(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a Reason instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a Reason instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Reason object, or null if unable to parse.
     */
    @JsonCreator
    public static Reason fromValue(String value) {
        Reason[] items = Reason.values();
        for (Reason item : items) {
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
