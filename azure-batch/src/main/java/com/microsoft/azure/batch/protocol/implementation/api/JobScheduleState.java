/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobScheduleState.
 */
public enum JobScheduleState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value completed. */
    COMPLETED("completed"),

    /** Enum value disabled. */
    DISABLED("disabled"),

    /** Enum value terminating. */
    TERMINATING("terminating"),

    /** Enum value deleting. */
    DELETING("deleting");

    /** The actual serialized value for a JobScheduleState instance. */
    private String value;

    JobScheduleState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a JobScheduleState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a JobScheduleState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobScheduleState object, or null if unable to parse.
     */
    @JsonCreator
    public static JobScheduleState fromValue(String value) {
        JobScheduleState[] items = JobScheduleState.values();
        for (JobScheduleState item : items) {
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
