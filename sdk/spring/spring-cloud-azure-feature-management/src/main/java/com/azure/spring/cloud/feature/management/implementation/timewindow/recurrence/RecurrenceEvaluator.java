// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;

import java.time.ZonedDateTime;

public class RecurrenceEvaluator {
    // Error Message
    public static final String OUT_OF_RANGE = "The value is out of the accepted range.";
    public static final String UNRECOGNIZED_VALUE = "The value is unrecognizable.";
    public static final String REQUIRED_PARAMETER = "Value cannot be null.";
    public static final String NOT_MATCHED = "Start date is not a valid first occurrence.";

    public static boolean matchRecurrence(ZonedDateTime now, TimeWindowFilterSettings settings) {
        return false;
    }
}
