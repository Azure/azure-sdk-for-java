/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobResult.
 */
public enum JobResult {
    /** Enum value None. */
    NONE("None"),

    /** Enum value Succeeded. */
    SUCCEEDED("Succeeded"),

    /** Enum value Cancelled. */
    CANCELLED("Cancelled"),

    /** Enum value Failed. */
    FAILED("Failed");

    /** The actual serialized value for a JobResult instance. */
    private String value;

    JobResult(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a JobResult instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a JobResult instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobResult object, or null if unable to parse.
     */
    @JsonCreator
    public static JobResult fromValue(String value) {
        JobResult[] items = JobResult.values();
        for (JobResult item : items) {
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
