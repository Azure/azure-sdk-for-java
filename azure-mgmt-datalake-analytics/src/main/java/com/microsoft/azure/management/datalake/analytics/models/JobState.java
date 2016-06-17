/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JobState.
 */
public enum JobState {
    /** Enum value Accepted. */
    ACCEPTED("Accepted"),

    /** Enum value Compiling. */
    COMPILING("Compiling"),

    /** Enum value Ended. */
    ENDED("Ended"),

    /** Enum value New. */
    NEW("New"),

    /** Enum value Queued. */
    QUEUED("Queued"),

    /** Enum value Running. */
    RUNNING("Running"),

    /** Enum value Scheduling. */
    SCHEDULING("Scheduling"),

    /** Enum value Starting. */
    STARTING("Starting"),

    /** Enum value Paused. */
    PAUSED("Paused"),

    /** Enum value WaitingForCapacity. */
    WAITING_FOR_CAPACITY("WaitingForCapacity");

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
