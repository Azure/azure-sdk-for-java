 /*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models;

/**
 * The type of {@link RecurrenceRange}, specifying the date range over which the time window repeats.
 * */
public enum RecurrenceRangeType {
    /**
     * The time window repeats on all the days that fit the corresponding {@link RecurrencePattern}.
     */
    NO_END("NoEnd"),

    /**
     * The time window repeats on all the days that fit the corresponding {@link RecurrencePattern}
     * before or on the end date specified in EndDate of {@link RecurrenceRange}.
     */
    END_DATE("EndDate"),

    /**
     * The time window repeats for the number specified in the NumberOfOccurrences of {@link RecurrenceRange}
     * that fit the corresponding {@link RecurrencePattern}.
     */
    NUMBERED("Numbered");

    private final String type;

    RecurrenceRangeType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
