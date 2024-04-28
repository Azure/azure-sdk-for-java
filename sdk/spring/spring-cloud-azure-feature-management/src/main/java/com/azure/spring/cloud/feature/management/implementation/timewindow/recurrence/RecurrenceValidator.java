// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrencePatternType;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrenceRangeType;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

public class RecurrenceValidator {
    private static String paramName;
    private static String reason;

    public static boolean validateSettings(TimeWindowFilterSettings settings) {
        return validateRecurrenceRequiredParameter(settings) &&
            validateRecurrencePattern(settings) &&
            validateRecurrenceRange(settings);
    }

    private static boolean validateRecurrenceRequiredParameter(TimeWindowFilterSettings settings) {
        final Recurrence recurrence = settings.getRecurrence();
        if (settings.getStart() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd().isBefore(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        if (settings.getEnd().isAfter(settings.getStart().plusDays(RecurrenceConstants.TEN_YEARS))) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.TIME_WINDOW_DURATION_OUT_OF_RANGE;
            return false;
        }

        return true;
    }

    private static boolean validateRecurrencePattern(TimeWindowFilterSettings settings) {
        if (!validateInterval(settings)) {
            return false;
        }

        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();
        if (patternType == RecurrencePatternType.DAILY) {
            return validateDailyRecurrencePattern(settings);
        } else if (patternType == RecurrencePatternType.WEEKLY) {
            return validateWeeklyRecurrencePattern(settings);
        } else {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
    }

    private static boolean validateRecurrenceRange(TimeWindowFilterSettings settings) {
        RecurrenceRangeType rangeType = settings.getRecurrence().getRange().getType();
        if (rangeType == RecurrenceRangeType.END_DATE) {
            return tryValidateEndDate();
        } else if (rangeType == RecurrenceRangeType.NUMBERED) {
            return tryValidateNumberOfOccurrences();
        } else {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
                RecurrenceConstants.RECURRENCE_RANGE_TYPE);
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private static boolean validateInterval(TimeWindowFilterSettings settings) {
        if (settings.getRecurrence().getPattern().getInterval() <= 0) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_INTERVAL);
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean validateDailyRecurrencePattern(TimeWindowFilterSettings settings) {
        // No required parameter for "Daily" pattern and "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        return validateTimeWindowDuration(settings, intervalDuration);
    }

    private static boolean validateWeeklyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        if (!validateDaysOfWeek(settings)) {
            return false;
        }
        if (!validateFirstDayOfWeek(settings)) {
            return false;
        }

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.DAYS_PER_WEEK);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (!validateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            settings.getStart().getDayOfWeek() == DayOfWeek.valueOf(dayOfWeekStr.toUpperCase()))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(timeWindowDuration, pattern.getInterval(), pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     */
    private static boolean validateTimeWindowDuration(TimeWindowFilterSettings settings, Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean validateDaysOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK);
        final List<String> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }

        for (String dayOfWeek : daysOfWeek) {
            try {
                DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            } catch (IllegalArgumentException e) {
                reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
                return false;
            }
        }

        return true;
    }

    private static boolean validateFirstDayOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK);

        final String firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        try {
            DayOfWeek.valueOf(firstDayOfWeek.toUpperCase());
        } catch (IllegalArgumentException e) {
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    /**
     * Check whether the duration is shorter than the minimum gap between recurrence of days of week.
     *
     * @param duration       The duration of time window.
     * @param interval       The number of weeks between each occurrence.
     * @param daysOfWeek     The days of the week when the recurrence will occur.
     * @param firstDayOfWeek The first day of the week.
     * @return True if the duration is compliant with days of week, false otherwise.
     */
    private static boolean isDurationCompliantWithDaysOfWeek(Duration duration, int interval, List<String> daysOfWeek, String firstDayOfWeek) {
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        final int offset = TimeWindowUtils.passingDaysOfWeek(today.getDayOfWeek(), firstDayOfWeek);
        final ZonedDateTime firstDateOfWeek = today.minusDays(offset).truncatedTo(ChronoUnit.DAYS);
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(daysOfWeek, firstDayOfWeek);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        ZonedDateTime date = firstDateOfWeek;
        ZonedDateTime prevOccurrence = null;
        Duration minGap = Duration.ofDays(RecurrenceConstants.DAYS_PER_WEEK);

        for (DayOfWeek day: sortedDaysOfWeek) {
            date = firstDateOfWeek.plusDays(TimeWindowUtils.passingDaysOfWeek(day, firstDayOfWeek));
            if (prevOccurrence != null) {
                final Duration currentGap = Duration.between(prevOccurrence, date);
                if (currentGap.compareTo(minGap) < 0) {
                    minGap = currentGap;
                }
            }
            prevOccurrence = date;
        }

        if (interval == 1) {
            // It may across weeks. Check the adjacent week
            date = firstDateOfWeek.plusDays(RecurrenceConstants.DAYS_PER_WEEK)
                .plusDays(TimeWindowUtils.passingDaysOfWeek(sortedDaysOfWeek.get(0), firstDayOfWeek));
            final Duration currentGap = Duration.between(prevOccurrence, date);
            if (currentGap.compareTo(minGap) < 0) {
                minGap = currentGap;
            }
        }
        return minGap.compareTo(duration) >= 0;
    }
}
