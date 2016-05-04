/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

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
     * Gets the serialized value for a JobState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a JobState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobState object, or null if unable to parse.
     */
    @JsonCreator
    public static JobState fromValue(String value) {
        JobState[] items = JobState.values();
        for (JobState item : items) {
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
