/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

/**
 * The recurrence pattern specifying how often the time window repeats
 * */
public class RecurrencePattern {
    /**
     * The recurrence pattern type
     */
    private RecurrencePatternType type;

    /**
     * The number of units between occurrences, where units can be in days, weeks, months, or years,
     * depending on the pattern type. Default value is 1
     */
    private Integer interval = 1;

    /**
     * The days of the week on which the time window occurs
     */
    private List<String> daysOfWeek;

    /**
     * The first day of the week
     * */
    private String firstDayOfWeek = DayOfWeek.SUNDAY.name();

    /**
     * @return the recurrence pattern type
     * */
    public RecurrencePatternType getType() {
        return type;
    }

    /**
     * @param type pattern type to be set
     * */
    public void setType(String type) {
        this.type = Arrays.stream(RecurrencePatternType.values())
            .filter(e -> e.name().equalsIgnoreCase(type)).findAny().orElse(null);
    }

    /**
     * @return the number of units between occurrences
     * */
    public Integer getInterval() {
        return interval;
    }

    /**
     * @param interval the time units to be set
     * */
    public void setInterval(Integer interval) {
        if (interval != null) {
            this.interval = interval;
        }
    }

    /**
     * @return the days of the week on which the time window occurs
     * */
    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * @param daysOfWeek the days that time window occurs
     * */
    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    /**
     * @return the first day of the week
     * */
    public String getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * @param firstDayOfWeek the first day of the week
     * */
    public void setFirstDayOfWeek(String firstDayOfWeek) {
        if (firstDayOfWeek != null) {
            this.firstDayOfWeek = firstDayOfWeek;
        }
    }
}
