// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The recurrence range specifying how long the recurrence pattern repeats
 * */
public class RecurrenceRange {
    /**
     * The recurrence range type. Default value is "NoEnd"
     * */
    private String type = RecurrenceConstants.NO_END;

    /**
     * The date to stop applying the recurrence pattern
     * */
    private ZonedDateTime endDate;

    /**
     * The number of times to repeat the time window
     */
    private int numberOfRecurrences;

    /**
     * Time zone for the StartDate and EndDate
     */
    private String recurrenceTimeZone;

    /**
     * @return the recurrence range type
     * */
    public String getType() {
        return type;
    }

    /**
     * @param type the range type to be set
     * */
    public void setType(String type) {
        this.type = type;
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
        try {
            this.endDate = StringUtils.hasText(endDate)
                ? ZonedDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)
                : null;
        } catch (DateTimeParseException e) {
            this.endDate = StringUtils.hasText(endDate)
                ? ZonedDateTime.parse(endDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                : null;
        }
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
        this.numberOfRecurrences = numberOfRecurrences;
    }

    /**
     * @return the time zone for the StartDate and EndDate
     * */
    public String getRecurrenceTimeZone() {
        return recurrenceTimeZone;
    }

    /**
     * @param recurrenceTimeZone the time zone to be set
     * */
    public void setRecurrenceTimeZone(String recurrenceTimeZone) {
        this.recurrenceTimeZone = recurrenceTimeZone;
    }
}
