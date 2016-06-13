/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobType.
 */
public enum JobType {
    /** Enum value USql. */
    USQL("USql"),

    /** Enum value Hive. */
    HIVE("Hive");

    /** The actual serialized value for a JobType instance. */
    private String value;

    JobType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a JobType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a JobType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed JobType object, or null if unable to parse.
     */
    @JsonCreator
    public static JobType fromValue(String value) {
        JobType[] items = JobType.values();
        for (JobType item : items) {
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
