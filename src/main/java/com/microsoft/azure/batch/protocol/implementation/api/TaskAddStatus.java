/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TaskAddStatus.
 */
public enum TaskAddStatus {
    /** Enum value success. */
    SUCCESS("success"),

    /** Enum value clienterror. */
    CLIENTERROR("clienterror"),

    /** Enum value servererror. */
    SERVERERROR("servererror"),

    /** Enum value unmapped. */
    UNMAPPED("unmapped");

    /** The actual serialized value for a TaskAddStatus instance. */
    private String value;

    TaskAddStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a TaskAddStatus instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a TaskAddStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TaskAddStatus object, or null if unable to parse.
     */
    @JsonCreator
    public static TaskAddStatus fromValue(String value) {
        TaskAddStatus[] items = TaskAddStatus.values();
        for (TaskAddStatus item : items) {
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
