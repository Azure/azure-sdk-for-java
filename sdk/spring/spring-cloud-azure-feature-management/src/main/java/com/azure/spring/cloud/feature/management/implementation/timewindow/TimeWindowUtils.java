// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow;

import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public static int passingDaysOfWeek(DayOfWeek today, String firstDayOfWeek) {
        int remainingDays = (convertToWeekDayNumber(today) - convertToWeekDayNumber(firstDayOfWeek));
        if (remainingDays < 0) {
            return remainingDays + RecurrenceConstants.DAYS_PER_WEEK;
        } else {
            return remainingDays;
        }
    }

    public static int convertToWeekDayNumber(String str) {
        final String strUpperCase = str.toUpperCase();
        return DayOfWeek.valueOf(strUpperCase).getValue() % 7;
    }

    public static int convertToWeekDayNumber(DayOfWeek dateTime) {
        return dateTime.getValue() % 7;
    }

    public static List<DayOfWeek> sortDaysOfWeek(List<String> daysOfWeek, String firstDayOfWeek) {
        final List<DayOfWeek> result = daysOfWeek.stream()
            .map(str -> DayOfWeek.valueOf(str.toUpperCase()))
            .collect(Collectors.toList());

        final int firstDayNum = TimeWindowUtils.convertToWeekDayNumber(firstDayOfWeek);
        Collections.sort(result, (a, b) -> {
            int aIndex = (TimeWindowUtils.convertToWeekDayNumber(a) - firstDayNum) % 7;
            int bIndex = (TimeWindowUtils.convertToWeekDayNumber(b) - firstDayNum) % 7;
            return aIndex - bIndex;
        });
        return result;
    }
}
