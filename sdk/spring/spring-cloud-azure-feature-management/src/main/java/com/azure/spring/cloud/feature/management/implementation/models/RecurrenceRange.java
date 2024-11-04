// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * The recurrence range specifying how long the recurrence pattern repeats
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurrenceRange {
    /**
     * The recurrence range type. Default value is "NoEnd"
     * */
    private RecurrenceRangeType type = RecurrenceRangeType.NOEND;

    /**
     * The date to stop applying the recurrence pattern
     * */
    private ZonedDateTime endDate;

    /**
     * The number of times to repeat the time window
     */
    private int numberOfOccurrences;

    /**
     * @return the recurrence range type
     * */
    public RecurrenceRangeType getType() {
        return type;
    }

    /**
     * @param type the range type to be set
     * @throws IllegalArgumentException if type is invalid
     * */
    public void setType(String type) throws IllegalArgumentException {
        try {
            this.type = RecurrenceRangeType.valueOf(type.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.INVALID_VALUE, "Recurrence.Range.Type", Arrays.toString(RecurrenceRangeType.values())));
        }
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
    public int getNumberOfOccurrences() {
        return numberOfOccurrences;
    }

    /**
     * @param numberOfOccurrences the repeat times to be set
     * @throws IllegalArgumentException if numberOfOccurrences is invalid
     * */
    public void setNumberOfOccurrences(int numberOfOccurrences) throws IllegalArgumentException {
        if (numberOfOccurrences < 1) {
            throw new IllegalArgumentException(
                String.format(RecurrenceConstants.OUT_OF_RANGE, "Recurrence.Range.NumberOfOccurrences"));
        }
        this.numberOfOccurrences = numberOfOccurrences;
    }
}
