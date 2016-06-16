/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobState.
 */
public enum JobState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value disabling. */
    DISABLING("disabling"),

    /** Enum value disabled. */
    DISABLED("disabled"),

    /** Enum value enabling. */
    ENABLING("enabling"),

    /** Enum value terminating. */
    TERMINATING("terminating"),

    /** Enum value completed. */
    COMPLETED("completed"),

    /** Enum value deleting. */
    DELETING("deleting");

    /** The actual serialized value for a JobState instance. */
    private String value;

    JobState(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a JobState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobState object, or null if unable to parse.
     */
    @JsonCreator
    public static JobState fromString(String value) {
        JobState[] items = JobState.values();
        for (JobState item : items) {
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
