// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

public final class RecurrenceConstants {
    private RecurrenceConstants() {
    }

    // Day of week
    public static final String SUNDAY = "sunday";
    public static final String MONDAY = "monday";
    public static final String TUESDAY = "tuesday";
    public static final String WEDNESDAY = "wednesday";
    public static final String THURSDAY = "thursday";
    public static final String FRIDAY = "friday";
    public static final String SATURDAY = "saturday";

    // Index
    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String THIRD = "third";
    public static final String FOURTH = "fourth";
    public static final String LAST = "last";

    // Recurrence Pattern Type
    public static final String DAILY = "daily";
    public static final String WEEKLY = "weekly";
    public static final String ABSOLUTE_MONTHLY = "absoluteMonthly";
    public static final String RELATIVE_MONTHLY = "relativeMonthly";
    public static final String ABSOLUTE_YEARLY = "absoluteYearly";
    public static final String RELATIVE_YEARLY = "relativeYearly";

    // Recurrence Range Type
    public static final String END_DATE = "EndDate";
    public static final String NUMBERED = "Numbered";
    public static final String NO_END = "NoEnd";

    public static final int WEEK_DAY_NUMBER = 7;
    public static final int MIN_MONTH_DAY_NUMBER = 28;
    public static final int MIN_YEAR_DAY_NUMBER = 365;

    // parameters
    public static final String RECURRENCE_PATTERN = "Pattern";
    public static final String RECURRENCE_PATTERN_TYPE = "Type";
    public static final String RECURRENCE_PATTERN_INTERVAL = "Interval";
    public static final String RECURRENCE_PATTERN_DAYS_OF_WEEK = "DaysOfWeek";
    public static final String RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK = "FirstDayOfWeek";
    public static final String RECURRENCE_PATTERN_DAY_OF_MONTH = "DayOfMoth";
    public static final String RECURRENCE_PATTERN_INDEX = "Index";
    public static final String RECURRENCE_PATTERN_MONTH = "Month";
    public static final String RECURRENCE_RANGE = "Range";
    public static final String RECURRENCE_RANGE_TYPE = "Type";
    public static final String RECURRENCE_RANGE_RECURRENCE_TIME_ZONE = "RecurrenceTimeZone";
    public static final String RECURRENCE_RANGE_EDN_DATE = "EndDate";

}
