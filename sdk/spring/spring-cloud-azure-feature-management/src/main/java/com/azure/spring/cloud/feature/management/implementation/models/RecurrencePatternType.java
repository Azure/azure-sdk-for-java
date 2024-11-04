// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

/**
 * The type of {@link RecurrencePattern} specifying the frequency by which the time window repeats.
 * */
public enum RecurrencePatternType {
    /**
     * The pattern where the time window will repeat based on the number of days specified by interval between occurrences.
     */
    DAILY("Daily"),

    /**
     * The pattern where the time window will repeat on the same day or days of the week,
     * based on the number of weeks between each set of occurrences.
     */
    WEEKLY("Weekly");

    private final String type;

    RecurrencePatternType(final String type) {
        this.type = type;
    }
}
