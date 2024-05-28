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
    private TimeWindowFilterSettings settings;

    public RecurrenceValidator(TimeWindowFilterSettings settings) {
        this.settings = settings;
    }

    public void validateSettings() {
        validateRecurrenceRequiredParameter();
        validateRecurrencePattern();
        validateRecurrenceRange();
    }

    private void validateRecurrenceRequiredParameter() {
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
        if (settings.getEnd().isBefore(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
        }
        if (settings.getEnd().isAfter(settings.getStart().plusDays(RecurrenceConstants.TEN_YEARS))) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.TIME_WINDOW_DURATION_OUT_OF_RANGE;
        }

        if (!paramName.isEmpty()) {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }
    }

    private void validateRecurrencePattern() {
        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();

        if (patternType == RecurrencePatternType.DAILY) {
            validateDailyRecurrencePattern();
        } else {
            validateWeeklyRecurrencePattern();
        }
    }

    private void validateRecurrenceRange() {
        RecurrenceRangeType rangeType = settings.getRecurrence().getRange().getType();
        if (RecurrenceRangeType.ENDDATE.equals(rangeType)) {
            validateEndDate();
        }
    }

    private void validateDailyRecurrencePattern() {
        // "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        validateTimeWindowDuration(intervalDuration);
    }

    private void validateWeeklyRecurrencePattern() {
        validateDaysOfWeek();

        // Check whether "Start" is a valid first occurrence
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            settings.getStart().getDayOfWeek() == dayOfWeekStr)) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.NOT_MATCHED, TIME_WINDOW_FILTER_SETTING_START));
        }

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.DAYS_PER_WEEK);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        validateTimeWindowDuration(intervalDuration);

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(timeWindowDuration, pattern.getInterval(), pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek())) {
            throw new IllegalArgumentException("The time window between Start and End should be shorter than the minimum gap between Recurrence.Pattern.DaysOfWeek");
        }
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     */
    private void validateTimeWindowDuration(Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0) {
           throw new IllegalArgumentException("The time window between Start and End should be shorter than the Interval");
        }
    }

    private void validateDaysOfWeek() {
        final List<DayOfWeek> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.REQUIRED_PARAMETER, "Recurrence.Pattern.DaysOfWeek"));
        }
    }

    private void validateEndDate() {
        if (settings.getRecurrence().getRange().getEndDate().isBefore(settings.getStart())) {
            throw new IllegalArgumentException("The Recurrence.Range.EndDate should be after the Start");
        }
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
    private boolean isDurationCompliantWithDaysOfWeek(Duration duration, int interval, List<DayOfWeek> daysOfWeek, DayOfWeek firstDayOfWeek) {
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        final int offset = TimeWindowUtils.daysPassedWeekStart(today.getDayOfWeek(), firstDayOfWeek);
        final ZonedDateTime firstDateOfWeek = today.minusDays(offset).truncatedTo(ChronoUnit.DAYS);
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(daysOfWeek, firstDayOfWeek);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        ZonedDateTime date = firstDateOfWeek;
        ZonedDateTime prevOccurrence = null;
        Duration minGap = Duration.ofDays(RecurrenceConstants.DAYS_PER_WEEK);

        for (DayOfWeek day: sortedDaysOfWeek) {
            date = firstDateOfWeek.plusDays(TimeWindowUtils.daysPassedWeekStart(day, firstDayOfWeek));
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
                .plusDays(TimeWindowUtils.daysPassedWeekStart(sortedDaysOfWeek.get(0), firstDayOfWeek));
            final Duration currentGap = Duration.between(prevOccurrence, date);
            if (currentGap.compareTo(minGap) < 0) {
                minGap = currentGap;
            }
        }
        return minGap.compareTo(duration) >= 0;
    }
}
