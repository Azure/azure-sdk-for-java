/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SchedulingState.
 */
public enum SchedulingState {
    /** Enum value enabled. */
    ENABLED("enabled"),

    /** Enum value disabled. */
    DISABLED("disabled");

    /** The actual serialized value for a SchedulingState instance. */
    private String value;

    SchedulingState(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a SchedulingState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SchedulingState object, or null if unable to parse.
     */
    @JsonCreator
    public static SchedulingState fromString(String value) {
        SchedulingState[] items = SchedulingState.values();
        for (SchedulingState item : items) {
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
