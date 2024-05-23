// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * The recurrence range specifying how long the recurrence pattern repeats
 * */
public class RecurrenceRange {
    /**
     * The recurrence range type. Default value is "NoEnd"
     * */
    private RecurrenceRangeType type = RecurrenceRangeType.NOEND;

    /**
     * The date to stop applying the recurrence pattern
     * */
    private ZonedDateTime endDate = Instant.ofEpochMilli(Integer.MAX_VALUE).atZone(ZoneOffset.UTC);

    /**
     * The number of times to repeat the time window
     */
    private int numberOfRecurrences = Integer.MAX_VALUE;

    /**
     * @return the recurrence range type
     * */
    public RecurrenceRangeType getType() {
        return type;
    }

    /**
     * @param type the range type to be set
     * */
    public void setType(String type) {
        // `RecurrenceRangeType.valueOf` may throw IllegalArgumentException if value is invalid
        this.type = RecurrenceRangeType.valueOf(type.toUpperCase());
    }

    /**
     * @return the date to stop applying the recurrence pattern
     * */
    public ZonedDateTime getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the end date to be set
     * */
    public void setEndDate(String endDate) {
        this.endDate = TimeWindowUtils.convertStringToDate(endDate);
    }

    /**
     * @return the number of times to repeat the time window
     * */
    public int getNumberOfRecurrences() {
        return numberOfRecurrences;
    }

    /**
     * @param numberOfRecurrences the repeat times to be set
     * */
    public void setNumberOfRecurrences(int numberOfRecurrences) {
        if (numberOfRecurrences < 1) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.OUT_OF_RANGE, "Recurrence.Range.NumberOfOccurrences"));
        }
        this.numberOfRecurrences = numberOfRecurrences;
    }
}
