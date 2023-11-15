// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import java.util.List;

/**
 * The recurrence pattern specifying how often the time window repeats
 * */
public class RecurrencePattern {
    /**
     * The recurrence pattern type
     */
    private String type;

    /**
     * The number of units between occurrences, where units can be in days, weeks, months, or years,
     * depending on the pattern type. Default value is 1
     */
    private int interval = 1;

    /**
     * The days of the week on which the time window occurs
     */
    private List<String> daysOfWeek;

    /**
     * The first day of the week
     * */
    private String firstDayOfWeek = RecurrenceConstants.SUNDAY;

    /**
     * Specifies on which instance of the allowed days specified in DaysOfWeek the time window occurs,
     * counted from the first instance in the month
     */
    private String index = RecurrenceConstants.FIRST;

    /**
     * The day of the month on which the time window occurs
     * */
    private Integer dayOfMonth;

    /**
     * The month on which the time window occurs
     * */
    private Integer month;

    /**
     * @return the recurrence pattern type
     * */
    public String getType() {
        return type;
    }

    /**
     * @param type pattern type to be set
     * */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the number of units between occurrences
     * */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the time units to be set
     * */
    public void setInterval(int interval) {
        this.interval = interval;
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
        this.firstDayOfWeek = firstDayOfWeek;
    }

    /**
     * @return the index of week that the event occurs
     * */
    public String getIndex() {
        return index;
    }

    /**
     * @param index the index to be set
     * */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @return the day of the month on which the time window occurs
     * */
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * @param dayOfMonth the day of month to be set
     * */
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * @return the month on which the time window occurs
     * */
    public Integer getMonth() {
        return month;
    }

    /**
     * @param month the month to be set
     */
    public void setMonth(Integer month) {
        this.month = month;
    }
}
