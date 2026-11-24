// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The recurrence pattern specifying how often the time window repeats
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurrencePattern {
    /**
     * The recurrence pattern type
     */
    private RecurrencePatternType type = RecurrencePatternType.DAILY;

    /**
     * The number of units between occurrences, where units can be in days, weeks, months, or years, depending on the
     * pattern type. Default value is 1
     */
    private Integer interval = 1;

    /**
     * The days of the week on which the time window occurs
     */
    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    /**
     * The first day of the week
     */
    private DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

    /**
     * @return the recurrence pattern type
     */
    public RecurrencePatternType getType() {
        return type;
    }

    /**
     * @param type pattern type to be set
     * @return the updated RecurrencePattern object
     * @throws IllegalArgumentException if type is invalid
     */
    public RecurrencePattern setType(String type) throws IllegalArgumentException {
        try {
            this.type = RecurrencePatternType.valueOf(type.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.INVALID_VALUE, "Recurrence.Pattern.Type",
                    Arrays.toString(RecurrencePatternType.values())));
        }
        return this;
    }

    /**
     * @return the number of units between occurrences
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * @param interval the time units to be set
     * @return the updated RecurrencePattern object
     * @throws IllegalArgumentException if interval is invalid
     */
    public RecurrencePattern setInterval(Integer interval) throws IllegalArgumentException {
        if (interval == null || interval <= 0) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.OUT_OF_RANGE, "Recurrence.Pattern.Interval"));
        }
        this.interval = interval;
        return this;
    }

    /**
     * @return the days of the week on which the time window occurs
     */
    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * @param daysOfWeek the days that time window occurs
     * @return the updated RecurrencePattern object
     * @throws IllegalArgumentException if daysOfWeek is null/empty, or has invalid value
     */
    public RecurrencePattern setDaysOfWeek(List<String> daysOfWeek) throws IllegalArgumentException {
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.DaysOfWeek"));
        }

        try {
            for (String dayOfWeek : daysOfWeek) {
                this.daysOfWeek.add(DayOfWeek.valueOf(dayOfWeek.toUpperCase()));
            }
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.INVALID_VALUE, "Recurrence.Pattern.DaysOfWeek",
                    Arrays.toString(DayOfWeek.values())));
        }
        return this;
    }

    /**
     * @return the first day of the week
     */
    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * @param firstDayOfWeek the first day of the week
     * @return the updated RecurrencePattern object
     * @throws IllegalArgumentException if firstDayOfWeek is invalid
     */
    public RecurrencePattern setFirstDayOfWeek(String firstDayOfWeek) throws IllegalArgumentException {
        if (firstDayOfWeek == null) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.FirstDayOfWeek"));
        }

        try {
            this.firstDayOfWeek = DayOfWeek.valueOf(firstDayOfWeek.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.INVALID_VALUE, "Recurrence.Pattern.FirstDayOfWeek",
                    Arrays.toString(DayOfWeek.values())));
        }
        return this;
    }
}
