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
     * Parses a serialized value to a SchedulingErrorCategory instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SchedulingErrorCategory object, or null if unable to parse.
     */
    @JsonCreator
    public static SchedulingErrorCategory fromString(String value) {
        SchedulingErrorCategory[] items = SchedulingErrorCategory.values();
        for (SchedulingErrorCategory item : items) {
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
