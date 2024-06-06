// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePatternType;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRangeType;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

public class RecurrenceValidator {
    public static void validateSettings(TimeWindowFilterSettings settings) {
        validateRecurrenceRequiredParameter(settings);
        validateRecurrencePattern(settings);
        validateRecurrenceRange(settings);
    }

    private static void validateRecurrenceRequiredParameter(TimeWindowFilterSettings settings) {
        final Recurrence recurrence = settings.getRecurrence();
        String paramName = "";
        String reason = "";
        if (recurrence.getPattern() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
        }
        if (recurrence.getRange() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
        }
        if (!settings.getEnd().isAfter(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
        }
        if (settings.getEnd().isAfter(settings.getStart().plusDays(RecurrenceConstants.TEN_YEARS))) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.TIME_WINDOW_DURATION_TEN_YEARS;
        }

        if (!paramName.isEmpty()) {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }
    }

    private static void validateRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();

        if (patternType == RecurrencePatternType.DAILY) {
            validateDailyRecurrencePattern(settings);
        } else {
            validateWeeklyRecurrencePattern(settings);
        }
    }

    private static void validateRecurrenceRange(TimeWindowFilterSettings settings) {
        RecurrenceRangeType rangeType = settings.getRecurrence().getRange().getType();
        if (RecurrenceRangeType.ENDDATE.equals(rangeType)) {
            validateEndDate(settings);
        }
    }

    private static void validateDailyRecurrencePattern(TimeWindowFilterSettings settings) {
        // "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        validateTimeWindowDuration(settings);
    }

    private static void validateWeeklyRecurrencePattern(TimeWindowFilterSettings settings) {
        validateDaysOfWeek(settings);

        // Check whether "Start" is a valid first occurrence
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            settings.getStart().getDayOfWeek() == dayOfWeekStr)) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.NOT_MATCHED, TIME_WINDOW_FILTER_SETTING_START));
        }

        // Time window duration must be shorter than how frequently it occurs
        validateTimeWindowDuration(settings);

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(settings)) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.TIME_WINDOW_DURATION_OUT_OF_RANGE, "Recurrence.Pattern.DaysOfWeek"));
        }
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     */
    private static void validateTimeWindowDuration(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final Duration intervalDuration = RecurrencePatternType.DAILY.equals(pattern.getType())
            ? Duration.ofDays(pattern.getInterval())
            : Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.DAYS_PER_WEEK);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.TIME_WINDOW_DURATION_OUT_OF_RANGE, "Recurrence.Pattern.Interval"));
        }
    }

    private static void validateDaysOfWeek(TimeWindowFilterSettings settings) {
        final List<DayOfWeek> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.DaysOfWeek"));
        }
    }

    private static void validateEndDate(TimeWindowFilterSettings settings) {
        if (settings.getRecurrence().getRange().getEndDate().isBefore(settings.getStart())) {
            throw new IllegalArgumentException("The Recurrence.Range.EndDate should be after the Start");
        }
    }

    /**
     * Check whether the duration is shorter than the minimum gap between recurrence of days of week.
     *
     * @param settings time window filter settings
     * @return True if the duration is compliant with days of week, false otherwise.
     */
    private static boolean isDurationCompliantWithDaysOfWeek(TimeWindowFilterSettings settings) {
        final List<DayOfWeek> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        final DayOfWeek firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        final int offset = TimeWindowUtils.getPassedWeekDays(today.getDayOfWeek(), firstDayOfWeek);
        final ZonedDateTime firstDateOfWeek = today.minusDays(offset).truncatedTo(ChronoUnit.DAYS);
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(daysOfWeek, firstDayOfWeek);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        ZonedDateTime date;
        ZonedDateTime prevOccurrence = null;
        Duration minGap = Duration.ofDays(RecurrenceConstants.DAYS_PER_WEEK);

        for (DayOfWeek day: sortedDaysOfWeek) {
            date = firstDateOfWeek.plusDays(TimeWindowUtils.getPassedWeekDays(day, firstDayOfWeek));
            if (prevOccurrence != null) {
                final Duration currentGap = Duration.between(prevOccurrence, date);
                if (currentGap.compareTo(minGap) < 0) {
                    minGap = currentGap;
                }
            }
            prevOccurrence = date;
        }

        if (settings.getRecurrence().getPattern().getInterval() == 1) {
            // It may across weeks. Check the adjacent week
            date = firstDateOfWeek.plusDays(RecurrenceConstants.DAYS_PER_WEEK)
                .plusDays(TimeWindowUtils.getPassedWeekDays(sortedDaysOfWeek.get(0), firstDayOfWeek));
            final Duration currentGap = Duration.between(prevOccurrence, date);
            if (currentGap.compareTo(minGap) < 0) {
                minGap = currentGap;
            }
        }

        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        return minGap.compareTo(timeWindowDuration) >= 0;
    }
}
