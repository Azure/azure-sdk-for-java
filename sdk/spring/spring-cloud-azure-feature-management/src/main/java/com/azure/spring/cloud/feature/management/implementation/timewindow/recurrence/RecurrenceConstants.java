// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

public final class RecurrenceConstants {
    private RecurrenceConstants() {
    }

    // Day of week
    public static final String SUNDAY = "Sunday";
    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";
    public static final String SATURDAY = "Saturday";

    // Index
    public static final String FIRST = "First";
    public static final String SECOND = "Second";
    public static final String THIRD = "Third";
    public static final String FOURTH = "Fourth";
    public static final String LAST = "Last";

    // Recurrence Pattern Type
    public static final String DAILY = "Daily";
    public static final String WEEKLY = "Weekly";
    public static final String ABSOLUTE_MONTHLY = "AbsoluteMonthly";
    public static final String RELATIVE_MONTHLY = "RelativeMonthly";
    public static final String ABSOLUTE_YEARLY = "AbsoluteYearly";
    public static final String RELATIVE_YEARLY = "RelativeYearly";

    // Recurrence Range Type
    public static final String END_DATE = "EndDate";
    public static final String NUMBERED = "Numbered";
    public static final String NO_END = "NoEnd";

    public static final int WEEK_DAY_NUMBER = 7;
    public static final int MIN_MONTH_DAY_NUMBER = 28;
    public static final int MIN_YEAR_DAY_NUMBER = 365;

}
