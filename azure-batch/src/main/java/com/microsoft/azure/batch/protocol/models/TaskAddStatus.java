/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

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
     * Parses a serialized value to a TaskAddStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TaskAddStatus object, or null if unable to parse.
     */
    @JsonCreator
    public static TaskAddStatus fromString(String value) {
        TaskAddStatus[] items = TaskAddStatus.values();
        for (TaskAddStatus item : items) {
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
