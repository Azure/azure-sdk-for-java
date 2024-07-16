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
    public static final String RECURRENCE_PATTERN_DAYS_OF_WEEK = "DaysOfWeek";
    public static final String RECURRENCE_RANGE = "Range";

    // Error Message
    public static final String OUT_OF_RANGE = "The value of parameter %s is out of the accepted range.";
    public static final String INVALID_VALUE = "The value of parameter %s should be one of [%s].";
    public static final String REQUIRED_PARAMETER = "Value cannot be null for required parameter: %s";
    public static final String NOT_MATCHED = "%s date is not a valid first occurrence.";
    public static final String TIME_WINDOW_DURATION_TEN_YEARS = "Time window duration cannot be longer than 10 years.";
    public static final String TIME_WINDOW_DURATION_OUT_OF_RANGE = "The time window between Start and End should be shorter than the minimum gap between %s";

}
