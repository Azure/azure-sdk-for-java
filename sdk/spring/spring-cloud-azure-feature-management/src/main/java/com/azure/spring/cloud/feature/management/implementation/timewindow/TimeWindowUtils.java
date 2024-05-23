// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow;

import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeWindowUtils {
    public static ZonedDateTime convertStringToDate(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (final DateTimeParseException e) {
            return ZonedDateTime.parse(timeStr, DateTimeFormatter.RFC_1123_DATE_TIME);
        }
    }

    /**
     * Calculate the number of days that have passed since the start of the week
     * @param today DayOfWeek enum of today
     * @param firstDayOfWeek the start day of the week
     * @return the number of days passed
     * */
    public static int daysPassedWeekStart(DayOfWeek today, DayOfWeek firstDayOfWeek) {
        int passedDays = (convertToWeekDayNumber(today) - convertToWeekDayNumber(firstDayOfWeek));
        if (passedDays < 0) {
            return passedDays + RecurrenceConstants.DAYS_PER_WEEK;
        } else {
            return passedDays;
        }
    }

    public static int convertToWeekDayNumber(DayOfWeek dateTime) {
        return dateTime.getValue() % 7;
    }

    public static List<DayOfWeek> sortDaysOfWeek(List<DayOfWeek> daysOfWeek, DayOfWeek firstDayOfWeek) {
        final List<DayOfWeek> result = new ArrayList<>(daysOfWeek);

        final int firstDayNum = TimeWindowUtils.convertToWeekDayNumber(firstDayOfWeek);
        Collections.sort(result, (a, b) -> {
            int aIndex = (TimeWindowUtils.convertToWeekDayNumber(a) - firstDayNum + RecurrenceConstants.DAYS_PER_WEEK) % 7;
            int bIndex = (TimeWindowUtils.convertToWeekDayNumber(b) - firstDayNum + RecurrenceConstants.DAYS_PER_WEEK) % 7;
            return aIndex - bIndex;
        });
        return result;
    }
}
