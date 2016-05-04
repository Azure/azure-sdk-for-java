/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Channels.
 */
public enum Channels {
    /** Enum value Notification. */
    NOTIFICATION("Notification"),

    /** Enum value Api. */
    API("Api"),

    /** Enum value Email. */
    EMAIL("Email"),

    /** Enum value All. */
    ALL("All");

    /** The actual serialized value for a Channels instance. */
    private String value;

    Channels(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a Channels instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a Channels instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed Channels object, or null if unable to parse.
     */
    @JsonCreator
    public static Channels fromValue(String value) {
        Channels[] items = Channels.values();
        for (Channels item : items) {
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
