// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePatternType;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRangeType;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;

public class RecurrenceValidator {
    public static void validateSettings(TimeWindowFilterSettings settings) {
        validateSettings(settings, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Validates recurrence settings with a specified reference time.
     * Public for testing purposes to allow deterministic validation.
     * 
     * @param settings The time window filter settings to validate
     * @param referenceTime The reference time to use for "today" in validation calculations
     */
    public static void validateSettings(TimeWindowFilterSettings settings, ZonedDateTime referenceTime) {
        validateRecurrenceRequiredParameter(settings);
        validateRecurrencePattern(settings, referenceTime);
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

    private static void validateRecurrencePattern(TimeWindowFilterSettings settings, ZonedDateTime referenceTime) {
        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();

        if (patternType == RecurrencePatternType.DAILY) {
            validateDailyRecurrencePattern(settings);
        } else {
            validateWeeklyRecurrencePattern(settings, referenceTime);
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

    private static void validateWeeklyRecurrencePattern(TimeWindowFilterSettings settings, ZonedDateTime referenceTime) {
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
        if (!isDurationCompliantWithDaysOfWeek(settings, referenceTime)) {
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
        // Convert to UTC to ensure consistent duration calculation across DST transitions
        final Duration timeWindowDuration = Duration.between(
            settings.getStart().withZoneSameInstant(ZoneOffset.UTC), 
            settings.getEnd().withZoneSameInstant(ZoneOffset.UTC));
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
    /**
     * Validate if time window duration is shorter than the minimum gap between days of week
     * @param settings The settings to validate
     * @param referenceTime The reference time to use for "today" in gap calculations
     * @return True if the duration is compliant with days of week, false otherwise.
     */
    private static boolean isDurationCompliantWithDaysOfWeek(TimeWindowFilterSettings settings, ZonedDateTime referenceTime) {
        final List<DayOfWeek> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week using provided reference time
        final ZonedDateTime today = referenceTime;
        final DayOfWeek firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        final int offset = TimeWindowUtils.getPassedWeekDays(today.getDayOfWeek(), firstDayOfWeek);
        final ZonedDateTime firstDateOfWeek = today.minusDays(offset).truncatedTo(ChronoUnit.DAYS);
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(daysOfWeek, firstDayOfWeek);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        // Use calendar-based day counting to avoid DST-related issues (23/25 hour elapsed time)
        ZonedDateTime date;
        ZonedDateTime prevOccurrence = null;
        long minGapDays = RecurrenceConstants.DAYS_PER_WEEK;

        for (DayOfWeek day: sortedDaysOfWeek) {
            date = firstDateOfWeek.plusDays(TimeWindowUtils.getPassedWeekDays(day, firstDayOfWeek));
            if (prevOccurrence != null) {
                // Use ChronoUnit.DAYS to count calendar days, not elapsed time
                // This ensures Sunday-Monday is always 1 day (24 hours) regardless of DST
                final long currentGapDays = ChronoUnit.DAYS.between(prevOccurrence, date);
                if (currentGapDays < minGapDays) {
                    minGapDays = currentGapDays;
                }
            }
            prevOccurrence = date;
        }

        if (settings.getRecurrence().getPattern().getInterval() == 1) {
            // It may across weeks. Check the adjacent week
            date = firstDateOfWeek.plusDays(RecurrenceConstants.DAYS_PER_WEEK)
                .plusDays(TimeWindowUtils.getPassedWeekDays(sortedDaysOfWeek.get(0), firstDayOfWeek));
            // Use ChronoUnit.DAYS to count calendar days, not elapsed time
            final long currentGapDays = ChronoUnit.DAYS.between(prevOccurrence, date);
            if (currentGapDays < minGapDays) {
                minGapDays = currentGapDays;
            }
        }

        final Duration minGap = Duration.ofDays(minGapDays);

        // Convert to UTC to ensure consistent duration calculation across DST transitions
        final Duration timeWindowDuration = Duration.between(
            settings.getStart().withZoneSameInstant(ZoneOffset.UTC), 
            settings.getEnd().withZoneSameInstant(ZoneOffset.UTC));
        return minGap.compareTo(timeWindowDuration) >= 0;
    }
}
