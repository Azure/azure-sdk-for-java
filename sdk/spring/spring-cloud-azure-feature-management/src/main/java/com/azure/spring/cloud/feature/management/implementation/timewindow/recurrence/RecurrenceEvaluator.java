// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.*;

public class RecurrenceEvaluator {
    // Error Message
    public static final String OUT_OF_RANGE = "The value of parameter %s is out of the accepted range.";
    public static final String UNRECOGNIZED_VALUE = "The value of parameter %s is unrecognizable.";
    public static final String REQUIRED_PARAMETER = "Value cannot be null for required parameter: %s";
    public static final String NOT_MATCHED = "%s date is not a valid first occurrence.";
    private static String paramName;
    private static String reason;

    /**
     * Checks if a provided timestamp is within any recurring time window specified by the Recurrence section in the time window filter settings.
     * If the time window filter has an invalid recurrence setting, an exception will be thrown.
     * @param now time stamp of current time.
     * @param settings the parameters of time window filter
     * @return True if the time stamp is within any recurring time window, false otherwise.
     * */
    public static boolean matchRecurrence(ZonedDateTime now, TimeWindowFilterSettings settings) {
        if (!tryValidateSettings(settings)) {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }
        // todo do we really need this check?
        if (now.isBefore(settings.getStart())) {
            return false;
        }
        if (!tryGetPreviousOccurrence(now, settings)) {
            return false;
        }
        if (now <= previousOccurrence + (settings.getEnd() - settings.getStart())) {
            return true;
        }
        return false;
    }

    private static boolean tryValidateSettings(TimeWindowFilterSettings settings) {
        if (!tryValidateGeneralRequiredParameter(settings)) {
            return false;
        }
        if (!tryValidateRecurrencePattern(settings)) {
            return false;
        }
        if (!tryValidateRecurrenceRange(settings)) {
            return false;
        }
        return true;
    }

    private static boolean tryValidateGeneralRequiredParameter(TimeWindowFilterSettings settings) {
        final Recurrence recurrence = settings.getRecurrence();
        if (settings.getStart() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
                RecurrenceConstants.RECURRENCE_RANGE_TYPE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd().isBefore(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRecurrencePattern(TimeWindowFilterSettings settings) {
        if (!tryValidateInterval(settings)) {
            return false;
        }

        final String patternType = settings.getRecurrence().getPattern().getType();
        if (RecurrenceConstants.DAILY.equalsIgnoreCase(patternType)) {
            return tryValidateDailyRecurrencePattern(settings);
        } else if (RecurrenceConstants.WEEKLY.equalsIgnoreCase(patternType)) {
            return tryValidateWeeklyRecurrencePattern(settings);
        } else if (RecurrenceConstants.ABSOLUTE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteMonthlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.RELATIVE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeMonthlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.ABSOLUTE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteYearlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.RELATIVE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeYearlyRecurrencePattern(settings);
        } else {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
    }

    private static boolean tryValidateRecurrenceRange(TimeWindowFilterSettings settings) {
        if (!TryValidateRecurrenceTimeZone(settings)) {
            return false;
        }

        String rangeType = settings.getRecurrence().getRange().getType();
        if (RecurrenceConstants.NO_END.equalsIgnoreCase(rangeType)) {
            // No parameter is required
            return true;
        } else if (RecurrenceConstants.END_DATE.equalsIgnoreCase(rangeType)) {
            return tryValidateEndDate(settings);
        } else if (RecurrenceConstants.NUMBERED.equalsIgnoreCase(rangeType)) {
            return TryValidateNumberOfOccurrences(settings);
        }
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_TYPE);
        reason = UNRECOGNIZED_VALUE;
        return false;
    }

    private static Boolean TryValidateRecurrenceTimeZone(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_RECURRENCE_TIME_ZONE);
        if (settings.getRecurrence().getRange().getRecurrenceTimeZone() != null &&
            tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone()) != null) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateEndDate(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_EDN_DATE);
        if (settings.getRecurrence().getRange().getEndDate() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (settings.getRecurrence().getRange().getEndDate().isBefore(startWithSpecificTimeZone)) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateInterval(TimeWindowFilterSettings settings) {
        if (settings.getRecurrence().getPattern().getInterval() <= 0) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_INTERVAL);
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateDailyRecurrencePattern(TimeWindowFilterSettings settings) {
        // No required parameter for "Daily" pattern and "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        return tryValidateTimeWindowDuration(settings, intervalDuration);
    }

    private static boolean tryValidateWeeklyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.WEEK_DAY_NUMBER);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }
        if (!tryValidateFirstDayOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            startWithSpecificTimeZone.getDayOfWeek().getValue() == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(timeWindowDuration, pattern.getInterval(), pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    /**
     * Check whether the duration is shorter than the minimum gap between recurrence of days of week.
     * @param duration The duration of time window.
     * @param daysOfWeek The days of the week when the recurrence will occur.
     * @param firstDayOfWeek The first day of the week.
     * @return True if the duration is compliant with days of week, false otherwise.
     * */
    private static boolean isDurationCompliantWithDaysOfWeek(Duration duration, int interval, List<String> daysOfWeek, String firstDayOfWeek) {
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        // todo do we really need to %7?
        final int offset = remainingDaysOfWeek(today.getDayOfWeek().getValue(), convertToWeekDayNumber(firstDayOfWeek)) % 7;
        final ZonedDateTime firstDateOfWeek = today.plusDays(offset);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        // todo why need to initialize as first day of week?
        ZonedDateTime date = firstDateOfWeek;
        ZonedDateTime prevOccurrence = firstDateOfWeek;
        Duration minGap = Duration.ofDays(7);
        for (int i = 0; i < 6; i++) {
            date = date.plusDays(1);
            final ZonedDateTime finalDate = date;
            if (daysOfWeek.stream().anyMatch(dayOfWeek -> convertToWeekDayNumber(dayOfWeek) == finalDate.getDayOfWeek().getValue())) {
                final Duration currentGap = Duration.between(prevOccurrence, date);
                if (currentGap.compareTo(minGap) < 0) {
                    minGap = currentGap;
                }
                prevOccurrence = date;
            }
        }
        if (interval == 1) {
            // It may across weeks. Check the adjacent week
            date = date.plusDays(1);
            final Duration currentGap = Duration.between(prevOccurrence, date);
            if (currentGap.compareTo(minGap) < 0) {
                minGap = currentGap;
            }
        }
        return minGap.compareTo(duration) >= 0;
    }

    private static int remainingDaysOfWeek(int today, int firstDayOfWeek) {
        int remainingDays = today - firstDayOfWeek;
        if (remainingDays < 0) {
            return -remainingDays;
        } else {
            return RecurrenceConstants.WEEK_DAY_NUMBER - remainingDays;
        }
    }

    private static boolean tryValidateAbsoluteMonthlyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.MIN_MONTH_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateDayOfMonth(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (startWithSpecificTimeZone.getDayOfMonth() != pattern.getDayOfMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        return true;
    }

    private static boolean tryValidateDayOfMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN, RecurrenceConstants.RECURRENCE_PATTERN_DAY_OF_MONTH);
        reason = null;
        if (settings.getRecurrence().getPattern().getDayOfMonth() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getDayOfMonth() < 1 || settings.getRecurrence().getPattern().getDayOfMonth() > 31) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRelativeMonthlyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays(pattern.getInterval() * RecurrenceConstants.MIN_MONTH_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateIndex(settings)) {
            return false;
        }
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            startWithSpecificTimeZone.getDayOfWeek().getValue() == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }
        return true;
    }

    private static boolean tryValidateIndex(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_INDEX);

        final String index = settings.getRecurrence().getPattern().getIndex();
        if (index == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.FIRST, RecurrenceConstants.SECOND, RecurrenceConstants.THIRD,
            RecurrenceConstants.FOURTH, RecurrenceConstants.LAST};
        if (Arrays.stream(legalValues).noneMatch(index::equalsIgnoreCase)) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateAbsoluteYearlyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays(pattern.getInterval() * RecurrenceConstants.MIN_YEAR_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateMonth(settings)) {
            return false;
        }
        if (!tryValidateDayOfMonth(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (startWithSpecificTimeZone.getDayOfMonth() != pattern.getDayOfMonth() || startWithSpecificTimeZone.getMonthValue() != pattern.getMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        return true;
    }

    private static boolean tryValidateMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_MONTH);
        if (settings.getRecurrence().getPattern().getMonth() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getMonth() < 1 || settings.getRecurrence().getPattern().getMonth() > 12) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRelativeYearlyRecurrencePattern(TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays(pattern.getInterval() * RecurrenceConstants.MIN_YEAR_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateMonth(settings)) {
            return false;
        }
        if (!tryValidateIndex(settings)) {
            return false;
        }
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime startWithSpecificTimeZone = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (startWithSpecificTimeZone.getMonthValue() != pattern.getMonth() ||
            !pattern.DaysOfWeek.Any(day =>
                dayOfNthWeekInTheMonth(alignedStart, pattern.Index, day) == alignedStart.Date)) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }
        return true;
    }

    /**
     * Find the nth day of week in the month of the date time.
     * @param startDateTime start date time
     * @param index Specifies on which instance of the allowed days specified in daysOfsWeek the event occurs, counted from the first instance in the month
     * @param dayOfWeek Specifies on which day(s) of the week the event can occur
     * @return The data time of the day in nth week the month.
     * */
    private static ZonedDateTime dayOfNthWeekInTheMonth(ZonedDateTime startDateTime, String index, String dayOfWeek) {
        var date = new DateTime(startDateTime.Year, startDateTime.Month, 1);

        // Find the first day of week in the month
        while ((int)date.DayOfWeek != DayOfWeekNumber(dayOfWeek)) {
            date = date.AddDays(1);
        }

        if (date.AddDays(RecurrenceConstants.WEEK_DAY_NUMBER * (convertIndexToNumber(index) - 1)).Month == startDateTime.Month) {
            date = date.AddDays(RecurrenceConstants.WEEK_DAY_NUMBER * (convertIndexToNumber(index) - 1));
        } else { // There is no the 5th week in the month
            // Add 3 weeks to reach the fourth week in the month
            date = date.AddDays(RecurrenceConstants.WEEK_DAY_NUMBER * 3);
        }
        return date;
    }

    private static boolean tryValidateDaysOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK);
        final List<String> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        for (String dayOfWeek: daysOfWeek) {
            if (Arrays.stream(legalValues).noneMatch(dayOfWeek::equalsIgnoreCase)) {
                reason = UNRECOGNIZED_VALUE;
                return false;
            }
        }
        return true;
    }

    private static boolean tryValidateFirstDayOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK);
        final String firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        if (firstDayOfWeek == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        if (Arrays.stream(legalValues).noneMatch(firstDayOfWeek::equalsIgnoreCase)) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     * */
    private static boolean tryValidateTimeWindowDuration(TimeWindowFilterSettings settings, Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0)  {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            // todo do we need more clear error info? like "Time window duration must be shorter than how frequently it occurs"
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static ZoneId getRecurrenceTimeZoneId(TimeWindowFilterSettings settings) {
        ZoneId timeZoneId = settings.getStart().getZone();
        final ZoneId rangeZoneId = tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone());
        if (rangeZoneId != null) {
            timeZoneId = rangeZoneId;
        }
        return timeZoneId;
    }

    private static ZoneId tryParseTimeZone(String timeZoneStr) {
        // todo need to parse time zone string
        ZoneId timeZoneId = null;
        return timeZoneId;
    }

    private static int convertToWeekDayNumber(String str) {
        final String strLowerCase = str.toLowerCase();
        switch (strLowerCase) {
            case RecurrenceConstants.SUNDAY:
                return 0;
            case RecurrenceConstants.MONDAY:
                return 1;
            case RecurrenceConstants.TUESDAY:
                return 2;
            case RecurrenceConstants.WEDNESDAY:
                return 3;
            case RecurrenceConstants.THURSDAY:
                return 4;
            case RecurrenceConstants.FRIDAY:
                return 5;
            case RecurrenceConstants.SATURDAY:
                return 6;
            default:
                throw new IllegalArgumentException("Parameter DaysOfWeek not support " + str);
        }
    }

    public static int convertIndexToNumber(String str) {
        final String strLowerCase = str.toLowerCase();
        switch (strLowerCase) {
            case RecurrenceConstants.FIRST:
                return 1;
            case RecurrenceConstants.SECOND:
                return 2;
            case RecurrenceConstants.THIRD:
                return 3;
            case RecurrenceConstants.FOURTH:
                return 4;
            case RecurrenceConstants.LAST:
                return 5;
            default:
                throw new IllegalArgumentException("Parameter Index not support " + str);
        }
    }
}
