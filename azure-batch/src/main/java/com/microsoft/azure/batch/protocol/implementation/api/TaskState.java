/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TaskState.
 */
public enum TaskState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value preparing. */
    PREPARING("preparing"),

    /** Enum value running. */
    RUNNING("running"),

    /** Enum value completed. */
    COMPLETED("completed");

    /** The actual serialized value for a TaskState instance. */
    private String value;

    TaskState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a TaskState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a TaskState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TaskState object, or null if unable to parse.
     */
    @JsonCreator
    public static TaskState fromValue(String value) {
        TaskState[] items = TaskState.values();
        for (TaskState item : items) {
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
