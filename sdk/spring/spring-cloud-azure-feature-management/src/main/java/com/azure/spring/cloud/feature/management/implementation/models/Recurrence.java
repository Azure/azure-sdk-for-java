// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A recurrence definition describing how time window recurs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recurrence {
    /**
     * The recurrence pattern specifying how often the time window repeats
     */
    private RecurrencePattern pattern;

    /**
     * The recurrence range specifying how long the recurrence pattern repeats
     */
    private RecurrenceRange range;

    /**
     * @return the recurrence pattern specifying how often the time window repeats
     */
    public RecurrencePattern getPattern() {
        return pattern;
    }

    /**
     * @param pattern the recurrence pattern to be set
     * @return the updated Recurrence object
     */
    public Recurrence setPattern(RecurrencePattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * @return the recurrence range specifying how long the recurrence pattern repeats
     */
    public RecurrenceRange getRange() {
        return range;
    }

    /**
     * @param range the recurrence range to be set
     * @return the updated Recurrence object
     */
    public Recurrence setRange(RecurrenceRange range) {
        this.range = range;
        return this;
    }
}
