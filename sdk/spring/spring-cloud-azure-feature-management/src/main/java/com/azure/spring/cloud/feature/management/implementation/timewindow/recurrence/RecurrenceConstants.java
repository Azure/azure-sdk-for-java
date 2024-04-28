// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;


public final class RecurrenceConstants {
    private RecurrenceConstants() {
    }
    public static final int DAYS_PER_WEEK = 7;
    public static final int TEN_YEARS = 3650;

    // parameters
    public static final String RECURRENCE_PATTERN = "Pattern";
    public static final String RECURRENCE_PATTERN_TYPE = "Type";
    public static final String RECURRENCE_PATTERN_INTERVAL = "Interval";
    public static final String RECURRENCE_PATTERN_DAYS_OF_WEEK = "DaysOfWeek";
    public static final String RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK = "FirstDayOfWeek";
    public static final String RECURRENCE_PATTERN_DAY_OF_MONTH = "DayOfMonth";
    public static final String RECURRENCE_PATTERN_INDEX = "Index";
    public static final String RECURRENCE_PATTERN_MONTH = "Month";
    public static final String RECURRENCE_RANGE = "Range";
    public static final String RECURRENCE_RANGE_TYPE = "Type";
    public static final String RECURRENCE_RANGE_RECURRENCE_TIME_ZONE = "RecurrenceTimeZone";
    public static final String RECURRENCE_RANGE_NUMBER_OF_OCCURRENCES = "NumberOfOccurrences";
    public static final String RECURRENCE_RANGE_EDN_DATE = "EndDate";

    // Error Message
    public static final String OUT_OF_RANGE = "The value of parameter %s is out of the accepted range.";
    public static final String UNRECOGNIZED_VALUE = "The value of parameter %s is unrecognizable.";
    public static final String REQUIRED_PARAMETER = "Value cannot be null for required parameter: %s";
    public static final String NOT_MATCHED = "%s date is not a valid first occurrence.";
    public static final String TIME_WINDOW_DURATION_OUT_OF_RANGE = "Time window duration cannot be longer than how frequently it occurs or be longer than 10 years.";

}
