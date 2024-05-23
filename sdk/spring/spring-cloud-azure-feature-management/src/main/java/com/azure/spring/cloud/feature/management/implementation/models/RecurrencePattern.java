// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * The recurrence pattern specifying how often the time window repeats
 * */
public class RecurrencePattern {
    /**
     * The recurrence pattern type
     */
    private RecurrencePatternType type = RecurrencePatternType.DAILY;

    /**
     * The number of units between occurrences, where units can be in days, weeks, months, or years,
     * depending on the pattern type. Default value is 1
     */
    private Integer interval = 1;

    /**
     * The days of the week on which the time window occurs
     */
    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    /**
     * The first day of the week
     * */
    private DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

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
        // `RecurrencePatternType.valueOf` may throw IllegalArgumentException if value is invalid
        this.type = RecurrencePatternType.valueOf(type.toUpperCase());
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
        if (interval == null || interval <= 0) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.OUT_OF_RANGE, "Recurrence.Pattern.Interval"));
        }
        this.interval = interval;
    }

    /**
     * @return the days of the week on which the time window occurs
     * */
    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * @param daysOfWeek the days that time window occurs
     * */
    public void setDaysOfWeek(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.DaysOfWeek"));
        }

        for (String dayOfWeek : daysOfWeek) {
            // `DayOfWeek.valueOf` may throw IllegalArgumentException if value is invalid
            this.daysOfWeek.add(DayOfWeek.valueOf(dayOfWeek.toUpperCase()));
        }
    }

    /**
     * @return the first day of the week
     * */
    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * @param firstDayOfWeek the first day of the week
     * */
    public void setFirstDayOfWeek(String firstDayOfWeek) {
        if (firstDayOfWeek == null) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.FirstDayOfWeek"));
        }

        // `DayOfWeek.valueOf` may throw IllegalArgumentException if value is invalid
        this.firstDayOfWeek = DayOfWeek.valueOf(firstDayOfWeek.toUpperCase());
    }
}
