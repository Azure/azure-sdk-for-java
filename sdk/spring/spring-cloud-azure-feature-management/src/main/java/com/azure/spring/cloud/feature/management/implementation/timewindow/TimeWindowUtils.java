// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;

public class TimeWindowUtils {
    public static ZonedDateTime convertStringToDate(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        ZonedDateTime result;
        try {
            result = ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (final DateTimeParseException e) {
            result = ZonedDateTime.parse(timeStr, DateTimeFormatter.RFC_1123_DATE_TIME);
        }
        
        // If the parsed ZonedDateTime has a fixed offset zone (e.g., "-08:00" instead of 
        // "America/Los_Angeles"), check if this offset matches the system default zone's
        // offset at that instant. If so, convert to use the system zone to preserve DST info.
        // This handles cases where RFC_1123_DATE_TIME format loses region information but
        // the intent was to use the local timezone. It preserves cross-timezone scenarios
        // where the offset genuinely differs from the system timezone.
        if (result.getZone() instanceof java.time.ZoneOffset) {
            java.time.ZoneId systemZone = java.time.ZoneId.systemDefault();
            java.time.ZoneOffset systemOffset = systemZone.getRules().getOffset(result.toInstant());
            // Only convert if the fixed offset matches the system zone's offset at this instant
            if (result.getOffset().equals(systemOffset)) {
                result = result.withZoneSameLocal(systemZone);
            }
        }
        
        return result;
    }

    /**
     * Calculates the offset in days between two given days of the week.
     * @param today DayOfWeek enum of today
     * @param firstDayOfWeek the start day of the week
     * @return the number of days passed
     * */
    public static int getPassedWeekDays(DayOfWeek today, DayOfWeek firstDayOfWeek) {
        return (today.getValue() - firstDayOfWeek.getValue() + RecurrenceConstants.DAYS_PER_WEEK) % 7;
    }

    public static List<DayOfWeek> sortDaysOfWeek(List<DayOfWeek> daysOfWeek, DayOfWeek firstDayOfWeek) {
        final List<DayOfWeek> result = new ArrayList<>(daysOfWeek);

        Collections.sort(result, Comparator.comparingInt(a -> getPassedWeekDays(a, firstDayOfWeek)));
        return result;
    }
}
