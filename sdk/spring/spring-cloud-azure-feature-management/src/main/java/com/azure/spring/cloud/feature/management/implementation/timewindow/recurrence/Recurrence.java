// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

/**
 * A recurrence definition describing how time window recurs
 * */
public class Recurrence {
    /**
     * The recurrence pattern specifying how often the time window repeats
     * */
    private RecurrencePattern pattern;
    /**
     * The recurrence range specifying how long the recurrence pattern repeats
     * */
    private RecurrenceRange range = new RecurrenceRange();

    /**
     * @return the recurrence pattern specifying how often the time window repeats
     * */
    public RecurrencePattern getPattern() {
        return pattern;
    }

    /**
     * @param pattern the recurrence pattern to be set
     * */
    public void setPattern(RecurrencePattern pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the recurrence range specifying how long the recurrence pattern repeats
     * */
    public RecurrenceRange getRange() {
        return range;
    }

    /**
     * @param range the recurrence range to be set
     * */
    public void setRange(RecurrenceRange range) {
        if (range != null) {
            this.range = range;
        }
    }

    @Override
    public String toString() {
        return "Recurrence{"
            + "pattern=" + pattern
            + ", range=" + range
            + '}';
    }
}
