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
    private String paramName;
    private String reason;
    private TimeWindowFilterSettings settings;

    public RecurrenceValidator(TimeWindowFilterSettings settings) {
        this.settings = settings;
    }

    public boolean validateSettings() {
        if (validateRecurrenceRequiredParameter()
            && validateRecurrencePattern()
            && validateRecurrenceRange()) {
            return true;
        } else {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }
    }

    private boolean validateRecurrenceRequiredParameter() {
        final Recurrence recurrence = settings.getRecurrence();
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

    private boolean validateRecurrencePattern() {
        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();

        if (patternType == RecurrencePatternType.DAILY) {
            return validateDailyRecurrencePattern();
        } else {
            return validateWeeklyRecurrencePattern();
        }
    }

    private boolean validateRecurrenceRange() {
        RecurrenceRangeType rangeType = settings.getRecurrence().getRange().getType();

        switch (rangeType) {
            case ENDDATE:
                return validateEndDate();
            default:
                return true;
        }
    }

    private boolean validateDailyRecurrencePattern() {
        // "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        return validateTimeWindowDuration(intervalDuration);
    }

    private boolean validateWeeklyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        if (!validateDaysOfWeek()) {
            return false;
        }

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.DAYS_PER_WEEK);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (!validateTimeWindowDuration(intervalDuration)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            settings.getStart().getDayOfWeek() == dayOfWeekStr)) {
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
    private boolean validateTimeWindowDuration(Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean validateDaysOfWeek() {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK);
        final List<DayOfWeek> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }

        return true;
    }

    private boolean validateEndDate() {
        if (settings.getRecurrence().getRange().getEndDate().isBefore(settings.getStart())) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
                RecurrenceConstants.RECURRENCE_RANGE_EDN_DATE);
            reason = RecurrenceConstants.OUT_OF_RANGE;
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
