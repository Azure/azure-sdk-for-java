/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ProtocolTypes.
 */
public enum ProtocolTypes {
    /** Enum value Http. */
    HTTP("Http"),

    /** Enum value Https. */
    HTTPS("Https");

    /** The actual serialized value for a ProtocolTypes instance. */
    private String value;

    ProtocolTypes(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a ProtocolTypes instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a ProtocolTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ProtocolTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static ProtocolTypes fromValue(String value) {
        ProtocolTypes[] items = ProtocolTypes.values();
        for (ProtocolTypes item : items) {
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
