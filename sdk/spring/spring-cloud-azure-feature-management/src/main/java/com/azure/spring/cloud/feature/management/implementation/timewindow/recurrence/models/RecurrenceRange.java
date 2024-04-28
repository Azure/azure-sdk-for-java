/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;

import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * The recurrence range specifying how long the recurrence pattern repeats
 * */
public class RecurrenceRange {
    /**
     * The recurrence range type. Default value is "NoEnd"
     * */
    private RecurrenceRangeType type = RecurrenceRangeType.NO_END;

    /**
     * The date to stop applying the recurrence pattern
     * */
    private ZonedDateTime endDate;

    /**
     * The number of times to repeat the time window
     */
    private Integer numberOfRecurrences;

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
        this.type = Arrays.stream(RecurrenceRangeType.values())
            .filter(e -> e.name().equalsIgnoreCase(type)).findAny().orElse(null);
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
    public Integer getNumberOfRecurrences() {
        return numberOfRecurrences;
    }

    /**
     * @param numberOfRecurrences the repeat times to be set
     * */
    public void setNumberOfRecurrences(Integer numberOfRecurrences) {
        this.numberOfRecurrences = numberOfRecurrences;
    }
}
