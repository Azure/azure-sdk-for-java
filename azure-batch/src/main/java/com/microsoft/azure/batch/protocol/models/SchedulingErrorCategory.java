/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SchedulingErrorCategory.
 */
public enum SchedulingErrorCategory {
    /** Enum value usererror. */
    USERERROR("usererror"),

    /** Enum value servererror. */
    SERVERERROR("servererror"),

    /** Enum value unmapped. */
    UNMAPPED("unmapped");

    /** The actual serialized value for a SchedulingErrorCategory instance. */
    private String value;

    SchedulingErrorCategory(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a SchedulingErrorCategory instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a SchedulingErrorCategory instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SchedulingErrorCategory object, or null if unable to parse.
     */
    @JsonCreator
    public static SchedulingErrorCategory fromValue(String value) {
        SchedulingErrorCategory[] items = SchedulingErrorCategory.values();
        for (SchedulingErrorCategory item : items) {
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
