/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for StartTaskState.
 */
public enum StartTaskState {
    /** Enum value running. */
    RUNNING("running"),

    /** Enum value completed. */
    COMPLETED("completed");

    /** The actual serialized value for a StartTaskState instance. */
    private String value;

    StartTaskState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a StartTaskState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a StartTaskState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed StartTaskState object, or null if unable to parse.
     */
    @JsonCreator
    public static StartTaskState fromValue(String value) {
        StartTaskState[] items = StartTaskState.values();
        for (StartTaskState item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
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
